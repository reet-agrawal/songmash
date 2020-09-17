package edu.brown.cs.songmash.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import com.google.gson.GsonBuilder;
import com.wrapper.spotify.model_objects.specification.Playlist;
import edu.brown.cs.songmash.song.SpotifyAPI;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.brown.cs.songmash.main.Main;
import edu.brown.cs.songmash.song.SongMashProxy;

@WebSocket
public class SongMashWebSocket {
  private static final Gson GSON = new Gson();
  private static  Queue<Session> sessions;

  private static ConcurrentMap<Integer, Set<String>> rooms;
  private static ConcurrentMap<String, Boolean> userDone;
  private static ConcurrentMap<Integer, SongMashProxy> roomProxy;

  private static final String DEFAULT_DESCRIPTION_STUB =
      "Song helped created this playlist. Curated by ";

  // user ID -> spotify api connection
  private static Map<String, SpotifyAPI> userSpotify = new ConcurrentHashMap<>();

  public SongMashWebSocket(){
    sessions = new ConcurrentLinkedQueue<>();
    rooms = new ConcurrentHashMap<>();
    userSpotify = new ConcurrentHashMap<>();
    roomProxy = new ConcurrentHashMap<>();
    userDone = new ConcurrentHashMap<>();
  }

  private enum MESSAGE_TYPE {
    CONNECT,
    UPDATEROOM,
    GETPEOPLE,
    BEGIN,
    BEGINALL,
    END,
    FINISHED,
    SELECTED,
    USER_DONE,
  }

  public static Map<String, SpotifyAPI> getUserSpotify() {
    return userSpotify;
  }

  public static Map<Integer, Set<String>> getRooms() {
    return rooms;
  }

  public static Map<String, Boolean> getUserDone() {
    return userDone;
  }

  @OnWebSocketConnect
  public void connected(Session session) throws IOException {
    // Add the session to the queue
    System.out.println(session.getIdleTimeout());

    session.setIdleTimeout(0L);
    sessions.add(session);

    // Build the CONNECT message
    JsonObject payload = new JsonObject();
    JsonObject message = new JsonObject();
    message.addProperty("type", MESSAGE_TYPE.CONNECT.ordinal());
    message.add("payload", payload);

    // Send the CONNECT message
    session.getRemote().sendString(GSON.toJson(message));
    System.out.println("someone connected to socket.");
  }

  private void setupNewUser(int roomNum, String spotifyID){
    userDone.put(spotifyID, false);
    if (rooms.get(roomNum) == null) {
      // create proxy for room
      SongMashProxy proxy = ProxyManager.buildProxy();
      roomProxy.put(roomNum, proxy);

      Set<String> ids = new HashSet<>();
      ids.add(spotifyID);
      rooms.put(roomNum, ids);
    } else {
      Set<String> ids = rooms.get(roomNum);
      ids.add(spotifyID);
      rooms.put(roomNum, ids);
    }



  }

  private List<String> getAllUsersInCurrentRoom(Set<String> userIDs){
    List<String> peopleList = new ArrayList<>();

    System.out.println(userIDs);
    System.out.println(userSpotify);

    for(String currSpotifyUserID : userIDs){
      SpotifyAPI currAPI = userSpotify.get(currSpotifyUserID);

      if(currAPI != null){
        String name = currAPI.getCurrentUserName();
        peopleList.add(name);
      }

    }

    return peopleList;
  }

  private void updateRoom(JsonObject payload) throws IOException {
    int room = payload.get("room").getAsInt();
    String spotifyID = payload.get("userid").getAsString();
    String authcode = payload.get("authcode").getAsString();

    System.out.println("room = " + room);
    System.out.println("authcode = " + authcode);
    System.out.println("spotify id = " + spotifyID);

    // add new user to hash maps
    setupNewUser(room, spotifyID);

    //send the display names of current users to the current room
    JsonObject msg = new JsonObject();

    msg.addProperty("type", MESSAGE_TYPE.GETPEOPLE.ordinal());

    // create a list of users but only for current room
    List<String> peopleList = getAllUsersInCurrentRoom(rooms.get(room));

    msg.addProperty("people", peopleList.toString());
    msg.addProperty("room", room);

    //send data
    sendMessageToAll(GSON.toJson(msg));
  }

  private Map<String, String> createPlaylistForAllUsers(int roomNum, String[] songIDs) {
    Map<String, String> userPlaylistLinks = new HashMap<>();
    Set<String> peopleInRoom = rooms.get(roomNum);
    StringBuilder curators = new StringBuilder();

    for (String currSpotifyID : peopleInRoom) {
      // user is in this room
      SpotifyAPI api = userSpotify.get(currSpotifyID);

      curators.append(api.getCurrentUserName());
      curators.append(" ");

      Date d = new Date();

      Playlist pl = api.createPlaylistOnBehalfOfUser(
          currSpotifyID,
          String.format("SongMash - %s", d.toString()),
          DEFAULT_DESCRIPTION_STUB + curators.toString(),
          true,
          false);

      api.addTrackToPlaylist(pl.getId(), songIDs);
      String playlistLink = api.getPublicPlaylistLink(pl);
      userPlaylistLinks.put(currSpotifyID, playlistLink);
    }
    return userPlaylistLinks;
  }

  private List<String> getBannedSongIDs(Set<String> userIDs){
    List<String> bannedIDs = new ArrayList<>();
    for(String currentUserSpotifyID : userIDs){
      SpotifyAPI api = userSpotify.get(currentUserSpotifyID);
      bannedIDs.addAll(api.getUserSavedTracksIDs());
    }

    return bannedIDs;
  }

  private Map<String, String> getUnfinishedUsers(Set<String> userIDs){
    Map<String, String> unfinishedUsers = new HashMap<>();
    for(String spotifyID : userIDs){
      if(!userDone.get(spotifyID)){
        unfinishedUsers.put(spotifyID, userSpotify.get(spotifyID).getCurrentUserName());
      }
    }
    return unfinishedUsers;
  }


  @OnWebSocketMessage
  public void onMessage(String message) throws IOException {
    JsonObject received = GSON.fromJson(message, JsonObject.class);

    System.out.println(received);

    //UPDATE ROOM MESSAGE
    if (received.get("type").getAsInt() == MESSAGE_TYPE.UPDATEROOM.ordinal()) {
      JsonObject payload = received.get("payload").getAsJsonObject();
      this.updateRoom(payload);
      System.out.println(rooms);
    }

    // START THE GAME
    if (received.get("type").getAsInt() == MESSAGE_TYPE.BEGIN.ordinal()) {
      System.out.println("BEGIN ran!");

      int room = received.get("room").getAsInt();

      //send ids to respective room
      JsonObject msg = new JsonObject();
      msg.addProperty("type", MESSAGE_TYPE.BEGINALL.ordinal());
      msg.addProperty("room", room);


      if (Main.excludeSongs){
        // filter the user has already seen from the proxy
        List<String> bannedIDs = getBannedSongIDs(rooms.get(room));
        ProxyManager.filterOutSongs(bannedIDs);
      }

      // send message to all sockets
      sendMessageToAll(GSON.toJson(msg));
    }

    // END THE GAME
    if (received.get("type").getAsInt() == MESSAGE_TYPE.END.ordinal()) {
      // get data payload
      String spotifyUserID = received.get("userid").getAsString();
      int room = received.get("room").getAsInt();

      System.out.println("Someone clicked done! " + spotifyUserID);
      userDone.put(spotifyUserID, true);

      //check if everyone in the room has finished
      Map<String, String> unfinishedUsers = getUnfinishedUsers(rooms.get(room));
      boolean allDone = unfinishedUsers.keySet().size() == 0;

      JsonObject msg = new JsonObject();
      Gson gson = new GsonBuilder().create();

      if (allDone) {
        System.out.println("everyone is done.");

        // create spotify playlists for all in room
        Main.playlist = roomProxy.get(room).getPlaylist(ProxyManager.getPlaylistSize());
        String[] songIDs = Main.playlist.toArray(new String[0]);
        Map<String, String> playLists = createPlaylistForAllUsers(room, songIDs);

        // send signal that everyone is done
        msg.addProperty("type", MESSAGE_TYPE.FINISHED.ordinal());
        msg.addProperty("room", room);
        msg.addProperty("playlistLinks", gson.toJson(playLists));
      } else {
        msg.addProperty("type", MESSAGE_TYPE.USER_DONE.ordinal());
        msg.addProperty("room", room);
        msg.addProperty("unfinished_users", gson.toJson(unfinishedUsers));
      }

      sendMessageToAll(GSON.toJson(msg));
    }

    // SELECTED A SONG CHOICE
    if (received.get("type").getAsInt() == MESSAGE_TYPE.SELECTED.ordinal()) {
      String id1 = received.get("id1").getAsString();
      String id2 = received.get("id2").getAsString();
      int room = received.get("room").getAsInt();
      // potential concurrent modification exception
      roomProxy.get(room).updateScores(id1, id2);
    }
  }

  private void sendMessageToAll(String msg) throws IOException {
    for(Session s: sessions){
      try {
        s.getRemote().sendString(msg);
      } catch (Exception e){
        System.err.println(e.toString());
      }
    }
  }

  @OnWebSocketClose
  public void closed(Session session, int statusCode, String reason) {
    // Remove the session from the queue
    System.out.println("socket closed.");
    System.out.println(rooms);

    sessions.remove(session);

    // TODO: update people in the room
  }

  @OnWebSocketError
  public void onError(Throwable t) {
    t.printStackTrace();
    System.out.println("Web Socket Error: " + t.getMessage());
  }
}
