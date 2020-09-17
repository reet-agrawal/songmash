package edu.brown.cs.songmash.song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.special.SnapshotResult;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.SavedTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.data.library.CheckUsersSavedTracksRequest;
import com.wrapper.spotify.requests.data.library.GetUsersSavedTracksRequest;
import com.wrapper.spotify.requests.data.playlists.AddTracksToPlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.CreatePlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsTracksRequest;
import com.wrapper.spotify.requests.data.search.simplified.
    SearchPlaylistsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.requests.data.tracks.
    GetAudioFeaturesForSeveralTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetSeveralTracksRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;


/**
 * This class handles data retrieval from a user's Spotify account.
 */
public class SpotifyAPI {
  private static final int MAX_BATCH_SONG_QUERY = 50;
  private static final int SLEEP_MS = 50;
  private SpotifyCredentials creds;
  private String flag = "";

  /**
   * Constructor for API object.
   *
   * @param credentialPath the path the file where the credentials will be
   *                       stored
   * @param tokenPath      the path to the file where tokens will be stored
   * @param callbackURL    the URL to which the Spotify authorization prompt
   *                       should go after the auth handshake is successful.
   *                       This must match the callback url in your Spotify
   *                       developer dashboard.
   */
  public SpotifyAPI(String credentialPath,
                    String tokenPath,
                    String callbackURL) {
    this.creds = new SpotifyCredentials(
        credentialPath, tokenPath, callbackURL
    );
  }

  /**
   * Constructor for API object for when client credentials are not stored in
   * persistent storage (text files).
   *
   * @param clientId     the client ID of our application. Always same for
   *                     any given app.
   * @param clientSecret the client secret of our application. Always the
   *                     same for any given app.
   * @param callbackURL  the URL to which the Spotify authorization prompt
   *                     should go after the auth handshake is successful.
   *                     This must match the callback url in your Spotify
   *                     developer dashboard.
   * @param pAuthCode    the authorization code to edit and read a user's
   *                     Spotify data on their behalf. This is different for
   *                     every user.
   */
  public SpotifyAPI(String clientId, String clientSecret,
                    String callbackURL, String pAuthCode) {
    this.creds = new SpotifyCredentials(clientId,
        clientSecret, pAuthCode, callbackURL);
  }


  /**
   * On success returns true.
   *
   * @param code the authorization code returned by Spotify's API.
   * @return true if authorization was successful, false otherwise.
   */
  public boolean setAuthCode(String code, long timestamp, boolean writeToDisk) {
    this.creds.setAuthCode(code, timestamp, writeToDisk);
    return creds.isAuthorized();
  }

  /**
   * @return the URL to which the user must go to authorize our application's
   * access to their account.
   */
  public String getAuthURL() {
    return this.creds.getAuthURL();
  }

  public void setFlag(String pFlag) {
    System.out.println("flag was set to " + pFlag);
    this.flag = pFlag;
  }


  /**
   * We need this in order to prevent the API from throwing the exception of
   * overloading the API with too many requests. Experimentally determined to be
   * about 50 ms.
   */
  private void sleep() {
    try {
      // prevent api rate exceeded exception
      Thread.sleep(SLEEP_MS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }


  /**
   * @return returns true if the API authorized to get data.
   */
  public boolean isAuthorized() {
    return this.creds.isAuthorized();
  }

  /**
   * Given a Track object, return a string of the artists. This is necessary
   * because Tracks contain an array of objects representing each object.
   *
   * @param t the Track for which we want to get their artists as a string.
   * @return A string containing the artists who created this track separated
   * by a space.
   */
  public static String getArtistStringFromTrackProperty(Track t) {
    ArtistSimplified[] artists = t.getArtists();

    StringBuilder artistsString = new StringBuilder();
    for (int i = 0; i < artists.length; i++) {
      artistsString.append((artists[i].getName()));

      if (i != artists.length - 1) {
        // add a space between artists
        artistsString.append(" ");
      }
    }

    return artistsString.toString();
  }

  private Song createSongFromTrackFeatures(Track t, AudioFeatures f) {
    return new Song(
        t.getId(),
        t.getName().replace(",", " "),
        getArtistStringFromTrackProperty(t),
        new double[]
            {
                f.getDurationMs(),       // 0
                f.getAcousticness(),     // 1
                f.getEnergy(),           // 2
                f.getInstrumentalness(), // 3
                f.getLiveness(),         // 4
                f.getLoudness(),         // 5
                f.getSpeechiness(),      // 6
                f.getValence(),          // 7
                f.getDanceability(),     // 8
            });
  }


  /**
   * Gets a single track from a query for songs.
   *
   * @param query the query to use to search for a song on Spotify
   * @return the top result for the track on spotify if a result exists.
   */
  public Track getSingleTrackByQuery(String query) {
    SearchTracksRequest searchTracksRequest =
        this.creds.getConnection().searchTracks(query).build();
    try {
      final Paging<Track> trackPaging = searchTracksRequest.execute();

      Track[] tracks = trackPaging.getItems();

      if (tracks.length > 0) {
        return tracks[0];
      }
    } catch (IOException | SpotifyWebApiException e) {
      System.out.println("Error: " + e.getMessage());
    }

    return null;
  }

  /**
   * Return a List of Tracks by querying them.
   *
   * @param queries a list of string queries for multiple tracks. e.g. 'Halsey
   *                Without me', 'Ticket to Ride by the Beatles'. Does not
   *                need to be exact.
   * @return a List of Track objects for each query. If a query does not
   * return any Track in the Spotify API, it is omitted from the list that is
   * returned. Therefore, the size of the returned list can differ from the
   * length of the queries array.
   */
  public List<Track> getMultipleTracksByQuery(String[] queries) {
    List<Track> tracks = new ArrayList<>();
    for (String query : queries) {
      Track result = getSingleTrackByQuery(query);

      if (result != null & (result != null ? result.getId() : null) != null) {
        System.out.println(String.format(
            "getMultipleTracksByQuery result = %s", result.getName()));

        // add tracks to list
        tracks.add(getSingleTrackByQuery(query));
      } else {
        System.err.println(
            String.format(
                "Could not find a track in spotify with query: \"%s\"",
                query));
      }
    }

    return tracks;
  }

  private List<Track> getMultipleTracksByID(String[] ids) {
    List<Track> tracks = new ArrayList<>();

    if (ids.length > MAX_BATCH_SONG_QUERY) {
      // need to iterate in batches
      for (int i = 0;
           i < Math.ceil(ids.length / (double) MAX_BATCH_SONG_QUERY);
           i++) {

        int endIndex = (i * MAX_BATCH_SONG_QUERY) + MAX_BATCH_SONG_QUERY;

        if (endIndex > ids.length) {
          // prevent index out of bounds
          endIndex = ids.length;
        }

        int startIndex = i * MAX_BATCH_SONG_QUERY;

        String[] batch = Arrays.copyOfRange(ids, startIndex, endIndex);
        Track[] subSetTracks = getBatchOfTracksByID(batch);

        if (subSetTracks != null) {
          tracks.addAll(Arrays.asList(subSetTracks));
        }
      }
    } else {
      // can do it all in one go
      Track[] newSongs = getBatchOfTracksByID(ids);
      if (newSongs != null) {
        tracks.addAll(Arrays.asList(newSongs));
      }
    }

    return tracks;
  }


  /**
   * Get a list of Songs from a list of IDs.
   *
   * @param ids an array of String IDs for each song to return. These IDs are
   *            assigned to songs by Spotify.
   * @return a List of Song objects containing the data for the song of the
   * given ID.
   */
  public List<Song> getSongsFromIds(String[] ids) {
    List<Track> tracks = getMultipleTracksByID(ids);
    List<AudioFeatures> features = getAudioFeatureForSeveralTracksByID(ids);

    List<Song> allSongs = new ArrayList<>();

    int i = 0;
    for (Track t : tracks) {
      if (t != null) {
        allSongs.add(createSongFromTrackFeatures(t, features.get(i)));
      }
      i++;
    }

    return allSongs;
  }

  /**
   * Get multiple tracks by their IDs. This method is required because the API
   * can only handle requests for tracks 50 at a time.
   * <p>
   * Boilerplate: https://github.com/thelinmichael/spotify-web-api-java/blob/
   * master/examples/data/tracks/GetSeveralTracksExample.java
   *
   * @param ids the IDs of tracks to lookup in the API.
   * @return track objects corresponding to the IDs.
   */
  private Track[] getBatchOfTracksByID(String[] ids) {
    assert ids.length <= MAX_BATCH_SONG_QUERY;

    this.sleep();

    GetSeveralTracksRequest getSeveralTracksRequest =
        creds.getConnection().getSeveralTracks(ids)
            .build();
    try {
      return getSeveralTracksRequest.execute();
    } catch (IOException | SpotifyWebApiException e) {
      System.out.println("Error: " + e.getMessage());
    }

    return null;
  }

  /**
   * Get the audio features e.g. acousticness, dancability, instrumentalness,
   * etc. for a song given its ID.
   * <p>
   * Boilerplate code from: https://github.com/thelinmichael/spotify-web-api-
   * java/blob/master/examples/data/tracks/GetAudioFeaturesForSeveral
   * TracksExample.java
   *
   * @param ids ids of songs for which to get audio features
   * @return a List of AudioFeatures objects corresponding to each track ID.
   */
  public List<AudioFeatures> getAudioFeatureForSeveralTracksByID(String[] ids) {
    List<AudioFeatures> features = new ArrayList<>();

    if (ids.length > MAX_BATCH_SONG_QUERY) {
      for (int i = 0;
           i < Math.ceil(ids.length / (double) MAX_BATCH_SONG_QUERY);
           i++) {
        int endIndex = (i * MAX_BATCH_SONG_QUERY) + MAX_BATCH_SONG_QUERY;

        if (endIndex > ids.length) {
          // prevent index out of bounds
          endIndex = ids.length;
        }

        String[] batch = Arrays.copyOfRange(ids,
            i * MAX_BATCH_SONG_QUERY, endIndex);

        AudioFeatures[] subSetfeatures = getBatchOfAudioFeatures(batch);

        if (subSetfeatures != null) {
          features.addAll(Arrays.asList(subSetfeatures));
        }
      }
    } else {
      AudioFeatures[] newFeatures = getBatchOfAudioFeatures(ids);
      if (newFeatures != null) {
        features.addAll(Arrays.asList(newFeatures));
      }
    }

    return features;
  }

  private AudioFeatures[] getBatchOfAudioFeatures(String[] ids) {
    assert (ids.length <= MAX_BATCH_SONG_QUERY);

    this.sleep();

    // API wrapper can only batch up to 100 request at a time.
    GetAudioFeaturesForSeveralTracksRequest
        getAudioFeaturesForSeveralTracksRequest = creds.getConnection()
        .getAudioFeaturesForSeveralTracks(ids)
        .build();

    try {
      return getAudioFeaturesForSeveralTracksRequest.execute();
    } catch (SpotifyWebApiException e) {
      System.err.println("Spotify API threw exception.");
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("Spotify API threw IOexception.");
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Get the songs contained in a public playlist.
   * <p>
   * Boilerplate code from: https://github.com/thelinmichael/
   * spotify-web-api-java/blob/master/examples/data/playlists/
   * GetPlaylistsTracksExample.java
   *
   * @param playlistId the ID of the playlist whose tracks we want to get.
   * @return an Array of PlaylistTrack objects representing the tracks in that
   * playlist.
   */
  public PlaylistTrack[] getPlaylistTracks(String playlistId) {
    GetPlaylistsTracksRequest getPlaylistsTracksRequest = creds.getConnection()
        .getPlaylistsTracks(playlistId)
        .build();

    try {
      PlaylistTrack[] tracks = getPlaylistsTracksRequest.execute().getItems();

      if (tracks != null) {
        // remove tracks that have no title, they are unplayable
        return Arrays.stream(tracks).filter(
            t -> t != null && t.getTrack() != null
                && t.getTrack().getName() != null
                && !t.getTrack().getName().equals("")
        ).toArray(PlaylistTrack[]::new);
      }

    } catch (IOException | SpotifyWebApiException e) {
      System.out.println("Error: " + e.getMessage());
    }

    return null;
  }

  /**
   * Boilerplate code from: https://github.com/thelinmichael/...
   * spotify-web-api-java/blob/master/examples/data/search/simplified/...
   * SearchTracksExample.java
   *
   * @param query the string to use to search for tracks.
   * @return the query set of all tracks found on spotify matching the query.
   */
  public Track[] getTracksByQuery(String query) {
    SearchTracksRequest searchTracksRequest =
        this.creds.getConnection().searchTracks(query)
            .build();
    try {
      return searchTracksRequest.execute().getItems();
    } catch (IOException | SpotifyWebApiException e) {
      System.out.println("Error: " + e.getMessage());
    }

    return null;
  }

  /**
   * Get a single playlist from a query. This works like searching for a
   * playlist on Spotify and then returning the result.
   * <p>
   * Boilerplate code from:
   * https://github.com/thelinmichael/spotify-web-api-java/blob/
   * master/examples/data/search/simplified/SearchPlaylistsExample.java
   *
   * @param query the query to use to search for the playlist
   * @return an array of PlaylistSimplified objects that were returned by
   * the query. Limited to a single playlist (the first result).
   */
  public PlaylistSimplified[] getSinglePlaylistByQuery(String query) {
    SearchPlaylistsRequest searchPlaylistsRequest =
        creds.getConnection().searchPlaylists(query)
            .limit(1)
            .build();
    try {
      Paging<PlaylistSimplified> playlistSimplifiedPaging =
          searchPlaylistsRequest.execute();
      return playlistSimplifiedPaging.getItems();

    } catch (IOException | SpotifyWebApiException e) {
      System.out.println("Error: " + e.getMessage());
    }
    return null;
  }


  /**
   * Return multiple playlists by queries.
   *
   * @param queries String queries for which we want to get the first result
   *                of the playlist return.
   * @return a List of PlaylistSimplified objects, each playlist index
   * corresponds to that of the query that was used to find it. The object
   * at the corresponding index is null if no results were found for the
   * query.
   */
  public List<PlaylistSimplified> getMultiplePlaylistsByQuery(
      String[] queries) {

    List<PlaylistSimplified> playlists = new ArrayList<>();

    for (String s : queries) {
      playlists.add(getSinglePlaylistByQuery(s)[0]);
    }

    return playlists;
  }


  /**
   * Return the tracks saved by the user. Typically this means retrieving their
   * whole library.
   * <p>
   * Boilerplate: https://github.com/thelinmichael/spotify-web-api-java/
   * blob/master/examples/data/library/GetUsersSavedTracksExample.java
   *
   * @return an array of SavedTrack objects, one for each saved song in the
   * current user's library. Which user's library is returns depends on the
   * authorization code contained in the SpotifyCredentials object.
   */
  public SavedTrack[] getUserSavedTracks() {
    GetUsersSavedTracksRequest getUsersSavedTracksRequest = this.creds
        .getConnection()
        .getUsersSavedTracks()
        .build();
    try {

      Paging<SavedTrack> savedTrackPaging =
          getUsersSavedTracksRequest.execute();

      System.out.println("Total: " + savedTrackPaging.getTotal());

      return savedTrackPaging.getItems();
    } catch (IOException | SpotifyWebApiException e) {
      System.out.println("Error: " + e.getMessage());
    }

    // return empty array
    return new SavedTrack[]{};
  }

  /**
   * @return a List of the strings of tracks a user has saved.
   */
  public List<String> getUserSavedTracksIDs() {
    SavedTrack[] tracks = getUserSavedTracks();

    List<String> ids = new ArrayList<>();
    for (SavedTrack t : tracks) {
      ids.add(t.getTrack().getId());
    }

    return ids;
  }

  /**
   * Check if a user has tracks in their library given their IDs.
   *
   * @param ids the IDs of tracks to check if the user has saved.
   * @return an Array of booleans, each boolean corresponds to each ID
   * of the tracks input. The boolean will be true if the track at the
   * corresponding index is saved by the user, false otherwise.
   */
  public Boolean[] checkUsersSavedTracks(String[] ids) {
    CheckUsersSavedTracksRequest checkUsersSavedTracksRequest =
        this.creds.getConnection().checkUsersSavedTracks(ids)
            .build();
    try {
      final Boolean[] booleans = checkUsersSavedTracksRequest.execute();

      System.out.println("Length: " + booleans.length);

      return booleans;

    } catch (IOException | SpotifyWebApiException e) {
      System.out.println("Error: " + e.getMessage());
    }

    return new Boolean[]{};
  }

  public String getPublicPlaylistLink(Playlist pl) {
    return "https://open.spotify.com/playlist/" + pl.getId();
  }

  public Playlist createPlaylistOnBehalfOfUser(String userID,
                                               String playlistName,
                                               String description,
                                               boolean isPublic,
                                               boolean isCollaborative) {
    CreatePlaylistRequest createPlaylistRequest =
        this.creds.getConnection().createPlaylist(userID, playlistName)
            .collaborative(isCollaborative)
            .public_(isPublic)
            .description(description)
            .build();
    try {
      return createPlaylistRequest.execute();
      //      System.out.println("Name: " + createPlaylistRequest.execute().getName());
    } catch (IOException | SpotifyWebApiException e) {
      System.out.println("Error: " + e.getMessage());
    }

    return null;
  }

  private String createSpotifyURIFromID(String pID) {
    return String.format("spotify:track:%s", pID);
  }

  public void addTrackToPlaylist(String playlistID, String[] songIds) {
    String[] uris = new String[songIds.length];
    int i = 0;
    for (String id : songIds) {
      uris[i++] = createSpotifyURIFromID(id);
    }

    AddTracksToPlaylistRequest addTracksToPlaylistRequest =
        this.creds.getConnection()
            .addTracksToPlaylist(playlistID, uris)
            .build();

    try {
      SnapshotResult snapshotResult = addTracksToPlaylistRequest.execute();

      System.out.println("Snapshot ID: " + snapshotResult.getSnapshotId());


    } catch (IOException | SpotifyWebApiException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  public String getCurrentUserName() {
    User user = this.getCurrentUsersProfile();

    if (user != null) {
      String name = user.getDisplayName();

      if (name == null || name.equals("null")) {
        return "Unknown Display Name";
      } else {
        return name;
      }

    } else {
      return "Not authorized.";
    }
  }

  public String getCurrentUserID() {
    User user = this.getCurrentUsersProfile();

    if (user != null) {
      return user.getId();
    } else {
      return "Not authorized.";
    }
  }


  public User getCurrentUsersProfile() {
    GetCurrentUsersProfileRequest getCurrentUsersProfileRequest =
        this.creds.getConnection()
            .getCurrentUsersProfile()
            .build();
    try {
      return getCurrentUsersProfileRequest.execute();
    } catch (IOException | SpotifyWebApiException e) {
      System.out.println("Error: " + e.getMessage());
    }
    return null;
  }

  @Override
  public String toString() {
    return "Spotify API Object, Flag = " + this.flag + ", Authcode = " + this.getAuthCode();
  }

  public String getAuthCode() {
    return this.creds.getAuthCode();
  }

  public void setWriteToDisk(boolean toWrite) {
    this.creds.setWriteToDisk(toWrite);
  }
}
