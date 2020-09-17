package edu.brown.cs.songmash.song;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class SpotifyCredentialsTest {
  // Michael Freeman's Spotify developer API credentials
  // dashboard URL: https://developer.spotify.com/dashboard/applications/2b4e890519954a35aae9db3e3f640eee
  private static final String clientID = "08a3d653cc064e2d90785205df53c9ef";
  private static final String clientSecret = "f364c908d98e40ef9bdd977233e40019";

  @Test
  public void testAuthCompleteHardCoded() {
//    SpotifyCredentials credsHardCoded = new SpotifyCredentials(
//        clientID,
//        clientSecret,
//        "",
//        SpotifyAPITest.callback_url);
//    assertTrue(credsHardCoded.isAuthorized());
  }

  @Test
  public void testAuthCompleteFromFile() {
    SpotifyCredentials credsFromFile = new SpotifyCredentials(
        SpotifyAPITest.credentials_file,
        SpotifyAPITest.tokens_file,
        SpotifyAPITest.callback_url
    );
    assertTrue(credsFromFile.isAuthorized());
  }

}
