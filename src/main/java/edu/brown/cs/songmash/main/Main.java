package edu.brown.cs.songmash.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import edu.brown.cs.songmash.net.ProxyManager;
import edu.brown.cs.songmash.net.SongMashWebSocket;
import edu.brown.cs.songmash.song.SongMashProxy;
import edu.brown.cs.songmash.song.SpotifyAPI;
import freemarker.template.Configuration;
import spark.ExceptionHandler;
import spark.ModelAndView;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.template.freemarker.FreeMarkerEngine;

public class Main {
  private static final int DEFAULT_PORT = 4567;

  // Spotify Credentials
  public static final String SPOTIFY_CREDENTIALS = "./data/spotify_credentials.txt";
  public static final String SPOTIFY_TOKENS = "./data/spotify_token.txt";
  public static final String callback_url = "http://localhost:4567/auth";

  // CSV Files
  private static final String PLAYLISTS_CSV_PATH = "./data/Spotify_Genre_Playlists.csv";
  private static final String SOURCE_SONG_QUERIES_CSV = "./data/893_essential_songs.csv";
  private static final String DEST_SONG_PROPS_CSV = "./data/song_props_new.csv";
  public static final String DEST_SONG_PROPS_W_VECTOR_CSV = "./data/song_props_new_w_vectors.csv";
  private static final int PLAYLIST_ID_COLUMN = 1;
  public static boolean excludeSongs = false;

  public static SpotifyAPI api = new SpotifyAPI(
      SPOTIFY_CREDENTIALS,
      SPOTIFY_TOKENS,
      getHerokuAssignedSpotifyCallBackURL());

  private static SongMashProxy proxy;
  private static final Gson GSON = new Gson();
  public static List<String> playlist = new ArrayList<>();

  private static final int DEFAULT_PLAYLIST_SIZE = 20;

  public Main() {
    proxy = ProxyManager.buildProxy();
    ProxyManager.setPlaylistSize(DEFAULT_PLAYLIST_SIZE);
  }

  public static void main(String[] args) {
    new Main().run();
  }

  static int getHerokuAssignedPort() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    if (processBuilder.environment().get("PORT") != null) {
      return Integer.parseInt(processBuilder.environment().get("PORT"));
    }
    return DEFAULT_PORT; //return default port if heroku-port isn't set (i.e. on localhost)
  }

  static String getHerokuAssignedSpotifyCallBackURL() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    if (processBuilder.environment().get("SPOTIFY_CALLBACK_URL") != null) {
      System.out.println("process builder got variable.");
      System.out.println(processBuilder.environment().get("SPOTIFY_CALLBACK_URL"));
      return (processBuilder.environment().get("SPOTIFY_CALLBACK_URL"));
    }
    return callback_url; //return default port if heroku-port isn't set (i.e. on localhost)
  }

  private void run() {
    runSparkServer();
  }

  private void runSparkServer() {
    Spark.port(getHerokuAssignedPort());
    Spark.externalStaticFileLocation("src/main/resources/static");
    Spark.exception(Exception.class, new ExceptionPrinter());

    FreeMarkerEngine freeMarker = createEngine();
    Spark.webSocket("/socket", new SongMashWebSocket());

    // basic pages
    Spark.get("/home", new HomeHandler(), freeMarker);
    Spark.get("/auth", new AuthSuccessHandler(), freeMarker);
    Spark.get("/logout", new LogoutHandler());
    Spark.get("/getuserid", new GetUserIDHandler());

    // socket routes
    Spark.get("/room/:id", new RoomHandler(), freeMarker);
    Spark.get("/room/:id/choose", new ChooseHandler(), freeMarker);
    Spark.get("/room/:id/results", new ResultsHandler(), freeMarker);
    Spark.post("/exitroom", new ExitRoomHandler());


    Spark.post("/newresults", new ButtonClickedHandler());
    Spark.post("/playlist", new GetPlaylistHandler());
    Spark.get("/displaysettings", new SettingsDisplayHandler(), freeMarker);
    Spark.post("/settings", new SettingsHandler());
    Spark.post("/choices", new FirstChoiceHandler());
  }

  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration();
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n",
          templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  private static class HomeHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {
      String authcode = request.cookie("authcode");
      String authtimeStr = request.cookie("authtime");
      String userIDstr = request.cookie("userid");

      boolean isAuthorized = false;

      Map<String, SpotifyAPI> spotConnections = SongMashWebSocket.getUserSpotify();

      // do not create new auth instance
      if (userIDstr != null && !userIDstr.equals("") && authcode != null && authtimeStr != null) {
        // authorization code exists in cookie
        if (spotConnections.containsKey(userIDstr)) {
          SpotifyAPI myAPI = spotConnections.get(userIDstr);

          long timeout = Long.parseLong(authtimeStr);
          myAPI.setAuthCode(authcode, timeout, false);
          isAuthorized = spotConnections.get(userIDstr).isAuthorized();
        }
      }

      System.out.println(isAuthorized);
      String authURL = api.getAuthURL();

      Map<String, Object> variables = ImmutableMap.of(
          "title", "SongMash: Home",
          "content", "",
          "authURL", authURL,
          "isAuthorized", isAuthorized);
      return new ModelAndView(variables, "home.ftl");
    }
  }

  private static class ChooseHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws Exception {
      String roomID = req.params(":id");
      QueryParamsMap qm = req.queryMap();
      Map<String, Object> variables = ImmutableMap.of("title", "SongMash: Choose");
      return new ModelAndView(variables, "choose.ftl");
    }
  }

  private static class RoomHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {
      String roomID = request.params(":id");
      //we want to add id to some server which represents that the room is taken
      Map<String, Object> variables = ImmutableMap.of("title", "SongMash: Friends!");
      return new ModelAndView(variables, "room.ftl");
    }
  }

  private static class ResultsHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws Exception {
      //will return top 25 songs for now
      String roomID = req.params(":id");
      //List<String> playlist = proxy.getPlaylist(ProxyManager.getPlaylistSize());

      Map<String, Object> variables = ImmutableMap.of("title", "SongMash: Results");
      return new ModelAndView(variables, "results.ftl");
    }
  }

  private static class FirstChoiceHandler implements Route {
    @Override
    public String handle(Request req, Response res) {
      String[] songIDs = proxy.getNewSongs();
      Map<String, Object> variables = ImmutableMap.of("songOne", songIDs[0], "songTwo", songIDs[1]);
      return GSON.toJson(variables);
    }
  }

  private static class ButtonClickedHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws Exception {
      QueryParamsMap qm = req.queryMap();
      String chosenSong = qm.value("songPicked");
      String notChosenSong = qm.value("songNotPicked");

      //updates user preferences
      proxy.updateScores(chosenSong, notChosenSong);
      //decides new songs for user to listen to
      String[] songIDs = proxy.getNewSongs();
      Map<String, Object> variables = ImmutableMap.of("songOne", songIDs[0], "songTwo", songIDs[1]);

      return GSON.toJson(variables);
    }
  }

  private static class LogoutHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws Exception {
      QueryParamsMap qm = req.queryMap();
      String spotifyUserID = qm.value("userid");

      if (spotifyUserID != null && !spotifyUserID.equals("")) {
        Map<String, SpotifyAPI> spotConnections = SongMashWebSocket.getUserSpotify();
        spotConnections.remove(spotifyUserID);
      }

      Map<String, Object> variables = ImmutableMap.of("isRemoved", spotifyUserID != null);

      return GSON.toJson(variables);
    }
  }

  private static class ExitRoomHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws Exception {
      QueryParamsMap qm = req.queryMap();
      String userSpotifyID = qm.value("userid");
      String userRoomID = qm.value("roomid");

      System.out.println("userSpotifyID = " + userSpotifyID);
      System.out.println("userRoomID = " + userRoomID);

      // don't delete spotify instance
      // SongMashWebSocket.getUserSpotify().remove(userSpotifyID);

      if (userRoomID != null && !userRoomID.equals("")) {
        Set<String> idsInRoom = SongMashWebSocket.getRooms().get(Integer.parseInt(userRoomID));
        idsInRoom.remove(userSpotifyID);
        SongMashWebSocket.getUserDone().remove(userSpotifyID);
      }

      Map<String, Object> variables = ImmutableMap.of("success", "removed");

      return GSON.toJson(variables);
    }
  }

  private static class SettingsDisplayHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws Exception {
      QueryParamsMap qm = req.queryMap();
      Map<String, Object> variables = ImmutableMap.of("title", "SongMash: Settings");
      return new ModelAndView(variables, "settings.ftl");
    }
  }

  private static class SettingsHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws Exception {
      QueryParamsMap qm = req.queryMap();
      String radio = qm.value("obscurity");
      String numSongs = qm.value("numSongs");
      if (radio != null && numSongs != null){
        if (Integer.parseInt(numSongs) > 0) {
          ProxyManager.setPlaylistSize(Integer.parseInt(numSongs));
        }
        //excludeSongs = !(radio.equals("notObscure"));
      }

      return null;
    }
  }

  private static class GetPlaylistHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws Exception {
      Map<String, Object> variables = ImmutableMap.of("playlist", playlist);
      return GSON.toJson(variables);
    }
  }

  private static class GetUserIDHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws Exception {
      String authcode = req.cookie("authcode");
      String authtimeStr = req.cookie("authtime");

      Map<String, SpotifyAPI> spotConnections = SongMashWebSocket.getUserSpotify();

      SpotifyAPI myAPI;
      String userID = "";

      // if the authorization code and its timestamp exist in the user cookie
      if (authcode != null && !authcode.equals("") && authtimeStr != null && !authtimeStr.equals("")) {
        System.out.println("authcode exists.");

        for (SpotifyAPI api : spotConnections.values()) {
          if (api.getAuthCode().equals(authcode)) {
            userID = api.getCurrentUserID();
            break;
          }
        }
      }

      Map<String, Object> variables = ImmutableMap.of("userID", userID);
      return GSON.toJson(variables);
    }
  }

  private static SpotifyAPI createConnectionForNewUser(String authCode) {
    SpotifyAPI newConnection = new SpotifyAPI(
        Main.SPOTIFY_CREDENTIALS,
        Main.SPOTIFY_TOKENS,
        getHerokuAssignedSpotifyCallBackURL()
    );

    newConnection.setWriteToDisk(false);
    newConnection.setAuthCode(authCode, 0L, false);
    newConnection.setFlag(newConnection.getCurrentUserName());

    return newConnection;
  }

  private static class AuthSuccessHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {

      String[] values = request.queryParamsValues("code");

      String authCode = "";
      if (values != null) {
        authCode = request.queryParamsValues("code")[0];
      }

      // set the authorization code, return true if handshake successful.
      Map<String, SpotifyAPI> auths = SongMashWebSocket.getUserSpotify();
      boolean authSuccess = false;
      String userID = "Unknown";

      if (authCode != null && !authCode.equals("")) {
        // authorization successful, create new spotify object
        SpotifyAPI newConnection = createConnectionForNewUser(authCode);

        authSuccess = newConnection.isAuthorized();

        if (authSuccess) {
          userID = newConnection.getCurrentUserID();

          if (!auths.containsKey(userID)) {
            // new connection instance
            auths.put(userID, newConnection);
          }
        }

      }

      System.out.println(authSuccess);
      Map<String, Object> variables = ImmutableMap.of(
          "title", "Spotify Authorization",
          "success", authSuccess,
          "userID", userID);

      return new ModelAndView(variables, "authsuccess.ftl");
    }
  }

  /**
   * Display an error page when an exception occurs in the server.
   */
  private static class ExceptionPrinter implements ExceptionHandler {
    @Override
    public void handle(Exception e, Request req, Response res) {
      res.status(500);
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }
  }

}
