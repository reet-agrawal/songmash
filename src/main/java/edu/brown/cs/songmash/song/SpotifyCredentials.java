package edu.brown.cs.songmash.song;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.
    AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.
    AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.
    AuthorizationCodeUriRequest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Class for handling all the API authentication logic.
 */
public class SpotifyCredentials {
  private static final int TIMEOUT = 3600 * 100; // 1 hour in ms
  private static final String CLIENT_SCOPE_READ_ALL =
      "user-library-read playlist-read-private playlist-modify-public playlist-modify-private";
  private static final String DEFAULT_TOKEN_LOCATION =
      "./data/spotify_token.txt";
  private static final String DEFAULT_CREDENTIALS_LOCATION =
      "./data/spotify_credentials.txt";

  private String authCode;
  private String credentialsFilePath, tokenFilePath;
  private Long lastFetch;
  private SpotifyApi apiConnection;
  private boolean writeToDisk = false;

  SpotifyCredentials(String pClientID,
                     String pClientSecret,
                     String pAuthCode,
                     String pCallbackURL) {
    this.authCode = pAuthCode;

    // set defaults
    this.lastFetch = 0L;
    this.tokenFilePath = DEFAULT_TOKEN_LOCATION;
    this.credentialsFilePath = DEFAULT_CREDENTIALS_LOCATION;

    this.apiConnection = createWrapper(
        pClientID,
        pClientSecret,
        pCallbackURL
    );

    this.parseTokensFromFile();
    this.reAuth();
  }

  SpotifyCredentials(String pathToCredentials,
                     String pathToTokens,
                     String pCallBackURL) {
    this.lastFetch = 0L;
    this.credentialsFilePath = pathToCredentials;
    this.tokenFilePath = pathToTokens;

    String[] codes = this.parseCredentialsFromFile(pathToCredentials);

    if (codes != null) {
      // the credentials must be present in order for any requests to be made
      this.apiConnection = createWrapper(
          codes[0],
          codes[1],
          pCallBackURL
      );
      this.authCode = codes[2];

      this.parseTokensFromFile();
      this.reAuth();
    }
  }

  /**
   * After the user received an authorization code from the URL returned by
   * getAuthURL, use this authorization code to request a refresh token and
   * access token from Spotify. We need these tokens to query from the API
   * and access user information.
   * <p>
   * Boilerplate code from: https://github.com/thelinmichael/...
   * spotify-web-api-java/blob/master/examples/authorization/...
   * authorization_code/AuthorizationCodeExample.java
   * <p>
   * Access token are valid for 1 hour.
   * <p>
   * Access token are valid for 1 hour.
   *
   * @return true if the authorization using the given code was successful,
   * false otherwise.
   */
  private AuthorizationCodeCredentials getAccessTokens() {
    System.out.println("getting refresh credentials");

    assert (this.authCode != null && !this.authCode.equals(""));

    AuthorizationCodeRequest authorizationCodeRequest =
        this.apiConnection.authorizationCode(this.authCode)
            .build();

    String uri = this.apiConnection.getRedirectURI().toString();
    System.out.println(uri);

    try {
      final AuthorizationCodeCredentials authorizationCodeCredentials =
          authorizationCodeRequest.execute();

      System.out.println("Expires in: " + authorizationCodeCredentials.
          getExpiresIn());

      // request for getting an access/refresh token succeeded
      return authorizationCodeCredentials;
    } catch (IOException | SpotifyWebApiException e) {
      // request for getting an access/refresh token failed
      // auth code expired
      System.err.println(e.getMessage());

      String errMsg = e.getMessage().trim();
      if (errMsg.equals("Authorization code expired")
          || errMsg.equals("Invalid authorization code")) {
        System.err.println(String.format("Request a new authcode here: %s",
            this.getAuthURL()));
      }

      return null;
    }
  }

  private void resetTokens(AuthorizationCodeCredentials tokens) {
    this.apiConnection.setAccessToken(tokens.getAccessToken());
    this.apiConnection.setRefreshToken(tokens.getRefreshToken());
  }

  public void setWriteToDisk(boolean toWrite){
    this.writeToDisk = toWrite;
  }

  private void reAuth() {

    System.out.println("starting reauth process.");

    if (this.getAuthCode() != null
        && this.getClientID() != null
        && this.getClientSecret() != null) {
      if (!this.accessTokenIsValid()) {

        System.out.println("attempting to fetch new access tokens.");

        // need to get a new one
        AuthorizationCodeCredentials myTokens = getAccessTokens();
        if (myTokens != null) {
          this.resetTokens(myTokens);

          if(this.writeToDisk){
            this.writeTokensToDisk(myTokens);
          }

          Date now = new Date();
          this.lastFetch = now.getTime();

          String successMsg = String.format("Spotify Authorization Successful.\n"
                  + "Authcode: %s\n"
                  + "Client ID: %s\n"
                  + "Client Secret: %s\n"
                  + "Refresh Token: %s\n"
                  + "Access Token: %s\n",
              this.authCode,
              this.apiConnection.getClientId(),
              this.apiConnection.getClientSecret(),
              this.apiConnection.getAccessToken(),
              this.apiConnection.getRefreshToken());

//          System.out.println(successMsg);
        }
      }
    } else {
      System.err.println("Spotify Auth Credentials invalid.");
      System.out.println(String.format(
          "Request a new authorization code here: %s", this.getAuthURL()));
    }
  }

  /**
   * Create a URL to direct a user of our application to begin the Spotify
   * authorization process.
   * <p>
   * Boilerplate code from: https://github.com/thelinmichael/...
   * spotify-web-api-java/blob/master/examples/authorization/...
   * authorization_code/AuthorizationCodeUriExample.java
   *
   * @return a URL to the location a user should go to being the Spotify API
   * authorization process. Here they will be asked if they want to allow
   * our application to access their data.
   */
  public String getAuthURL() {
    AuthorizationCodeUriRequest authReq =
        this.apiConnection.authorizationCodeUri()
            .scope(CLIENT_SCOPE_READ_ALL)
            .build();
    final URI uri = authReq.execute();

    return uri.toString();
  }

  private SpotifyApi createWrapper(String clientId,
                                   String clientSecret,
                                   String callbackURL) {

    System.out.println("redirect uri = " + callbackURL);
    return new SpotifyApi.Builder()
        .setClientId(clientId)
        .setClientSecret(clientSecret)
        .setRedirectUri(SpotifyHttpManager.makeUri(callbackURL))
        .build();
  }

  /**
   * File Format:
   * 1. clientID
   * 2. clientSecret
   * 3. authCode
   */
  private String[] parseCredentialsFromFile(String pCredentialsFilePath) {
    File file = new File(pCredentialsFilePath);
    Scanner sc;

    try {
      sc = new Scanner(file);
      String clientID = sc.nextLine();
      String clientSecret = sc.nextLine();
      String pAuthCode = "";

      try {
        pAuthCode = sc.nextLine();
      } catch (NoSuchElementException e) {
        System.err.println("Cannot find auth code.");
      }

      return new String[]{clientID, clientSecret, pAuthCode};

    } catch (FileNotFoundException e) {
      // there is no file at this location
      System.err.println("Cannot parse credentials from credentials file."
          + " Access disabled.");
    }

    return null;
  }

  private void parseTokensFromFile() {
    File file = new File(this.tokenFilePath);
    Scanner sc;

    try {
      sc = new Scanner(file);
      String timeStamp = sc.nextLine();
      String accessToken = "";

      try {
        accessToken = sc.nextLine();
      } catch (NoSuchElementException e) {
        System.err.println("Could not find access token.");
      }

      String refreshToken = "";

      try {
        refreshToken = sc.nextLine();
      } catch (NoSuchElementException e) {
        // no refresh token exists
        System.err.println("Could not find refresh token.");
      }

      // set the data
      this.lastFetch = Long.parseLong(timeStamp);
      this.apiConnection.setAccessToken(accessToken);
      this.apiConnection.setRefreshToken(refreshToken);
    } catch (FileNotFoundException e) {
      // there is no tokens file
      System.err.println("Cannot parse tokens from tokens file."
          + " Access disabled.");
    }
  }

  void setAuthCode(String pAuthCode, long timeout, boolean writeToDisk) {
    if (pAuthCode != null && !pAuthCode.equals("")) {
      this.authCode = pAuthCode;
      this.lastFetch = timeout;

      if (writeToDisk) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(this.credentialsFilePath),
            StandardCharsets.UTF_8))) {

          // write the client ID
          writer.write(this.apiConnection.getClientId() + "\n");

          // write the client secret
          writer.write(this.apiConnection.getClientSecret() + "\n");

          // write the authcode
          writer.write(this.authCode + "\n");

        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
          System.err.println("File at " + credentialsFilePath
              + " has unsupported encoding.");
        } catch (FileNotFoundException e) {
          e.printStackTrace();
          System.err.println("No file at " + credentialsFilePath + " exists.");
        } catch (IOException e) {
          e.printStackTrace();
          System.err.println("IO Exception for file at " + credentialsFilePath);
        }
      }
    }

    if (!this.isAuthorized()) {
      this.reAuth();
    }

  }

  private void writeTokensToDisk(AuthorizationCodeCredentials creds) {
    System.out.println("writing token to disk...");

    Date now = new Date();

    try (Writer writer = new BufferedWriter(new OutputStreamWriter(
        new FileOutputStream(this.tokenFilePath), StandardCharsets.UTF_8))) {

      // write the current time stamp
      writer.write(now.getTime() + "\n");

      // write the access token
      writer.write(creds.getAccessToken() + "\n");

      // write the refresh token
      if (creds.getRefreshToken() != null
          && !creds.getRefreshToken().equals("null")) {
        writer.write(creds.getRefreshToken() + "\n");
      }

    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      System.err.println("File at " + this.tokenFilePath
          + " has unsupported encoding.");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.err.println("No file at " + this.tokenFilePath + " exists.");

      // create the file

    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("IO Exception for file at " + this.tokenFilePath);
    }
  }

  private String nullOrEmptyStringCheck(String myString) {
    if (myString == null || myString.equals("")) {
      return null;
    } else {
      return myString;
    }
  }

  private String getClientID() {
    if (this.apiConnection == null) {
      return null;
    } else {
      return nullOrEmptyStringCheck(this.apiConnection.getClientId());
    }
  }

  private String getClientSecret() {
    if (this.apiConnection == null) {
      return null;
    } else {
      return nullOrEmptyStringCheck(this.apiConnection.getClientSecret());
    }
  }

  public String getAuthCode() {
    if (this.apiConnection == null) {
      return null;
    } else {
      return nullOrEmptyStringCheck(this.authCode);
    }
  }

  /**
   * Check if the credentials are fit to make a request.
   *
   * @return True if a request can safely be made using the current credentials.
   */
  public boolean isAuthorized() {
    String authCode = getAuthCode();
    String clientSecret = this.getClientSecret();
    String clientID = this.getClientID();

    boolean valid = accessTokenIsValid();

//    System.out.println("SPOTIFY CREDENTIALS SAYS ----");
//    System.out.println("authcode = " + authCode);
//
//    System.out.println("clientSecret = " + clientSecret);
//
//    System.out.println("clientID = " + clientID);
//
//    System.out.println("isValid = " + valid);

    return
        this.getAuthCode() != null
            && this.getClientSecret() != null
            && this.getClientID() != null
            && accessTokenIsValid();
  }

  private boolean accessTokenIsValid() {
    Date now = new Date();
    return (now.getTime() - this.lastFetch) < TIMEOUT;
  }

  /**
   * Handles refreshing API tokens when the API is called.
   *
   * @return the API connection that should have proper credentials for
   * fetching data.
   */
  SpotifyApi getConnection() {
    if (!this.isAuthorized()) {
      this.reAuth();
    }
    return this.apiConnection;
  }

}
