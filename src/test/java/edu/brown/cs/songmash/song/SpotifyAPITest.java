package edu.brown.cs.songmash.song;

import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import com.wrapper.spotify.model_objects.specification.Track;

import java.util.List;

public class SpotifyAPITest {
  // Test variables
  private static final String ESSENTIAL_SONGS_PATH = "./data/893_essential_songs.csv";
  private static final String SONG_PROPS_CSV_PATH = "./data/song_props.csv";

  // THIS IS VERY ANNOYING, this URL is updated in the dashboard but the API
  // throws an invalid URI error unless it is the same URL we started with
  private static final String callback_url_old = "http://localhost:8001/";

  public static final String callback_url = "http://localhost:4567/auth";
  public static final String credentials_file = "./data/spotify_credentials.txt";
  public static final String tokens_file = "./data/spotify_token.txt";

  private static SpotifyAPI myAPI;

  // Without Me by Halsey
  private static final String halsey_track_id = "5p7ujcrUXASCNwRaWNHR1C";

  // Rockstar by Post Malone
  private static final String post_track_id = "6L0bHTV6hf9UL6uCezlJCC";

  // Lucid Dreams by Juice WRLD
  private static final String juice_wrld_track_id = "285pBltuF7vW8TeWk8hdRR";

  // Playlist IDs
  private static final String RAP_CAVIAR_ID = "37i9dQZF1DX0XUsuxWHRQd";
  private static final String TRAP_NATION_ID = "0NCspsyf0OS4BsPgGhkQXM";

  @Before
  public void setup() {
    myAPI = new SpotifyAPI(credentials_file, tokens_file, callback_url);
    //    System.out.println(String.format("Auth URL: %s", myAPI.getAuthURL()));
  }

  @Test
  public void testAuthComplete() {
    assertTrue(myAPI.isAuthorized());
  }

  @Test
  public void testAuthCompleteWhenNoTokensExist() {
    SpotifyAPI otherAPI = new SpotifyAPI(credentials_file, "./data/spotify_token_alt.txt", callback_url);
    // this will work but a new Authcode must be requested from the API
    // assertTrue(otherAPI.isAuthorized());
  }

  @Test
  public void testGetCredentialsByFile() {
    SpotifyAPI credentialsAPI = new SpotifyAPI(credentials_file, tokens_file, callback_url);
    assertTrue(credentialsAPI.isAuthorized());
  }

  @Test
  public void testGetCredentialsByBadFile() {
    SpotifyAPI credentialsAPI = new SpotifyAPI("", "", callback_url);
    assertFalse(credentialsAPI.isAuthorized());
  }

  /**
   * File deletion code from:
   * https://www.geeksforgeeks.org/delete-file-using-java/
   */
  //  @Test
  //  public void testAuthWhenTokenDoesNoteExist() {
  //    // delete tokens file
  ////    try {
  ////      Files.deleteIfExists(Paths.get(SPOTIFY_TOKEN_FILE));
  ////    } catch (NoSuchFileException e) {
  ////      System.out.println("No such file/directory exists");
  ////    } catch (DirectoryNotEmptyException e) {
  ////      System.out.println("Directory is not empty.");
  ////    } catch (IOException e) {
  ////      System.out.println("Invalid permissions.");
  ////    }
  //
  //    // deletion successful, try to authorize
  //    SpotifyAPI authTester = new SpotifyAPI(credentials_file, callback_url);
  //
  //    // the interface should be able to authorize even if tokens file does not
  //    // exist.
  //    assertTrue(authTester.isAuthorized());
  //  }
  @Test
  public void getSongInformationByTitle() {
    String search_query = "Halsey Without Me";
    Track t = myAPI.getSingleTrackByQuery(search_query);
    assertNotNull(t);
    assertEquals(halsey_track_id, t.getId());
  }

  @Test
  public void getMultipleTracks() {
    String[] queries = new String[]{"Halsey Without Me", "Post Malone Rock Star"};

    List<Track> tracks = myAPI.getMultipleTracksByQuery(queries);

    assertEquals(halsey_track_id, tracks.get(0).getId());
    assertEquals(post_track_id, tracks.get(1).getId());
  }

  @Test
  public void testTrackQuery() {
    Track[] queryResults = myAPI.getTracksByQuery("Halsey Without Me");

    assertNotNull(queryResults);
    assertTrue(queryResults.length > 0);
  }

  @Test
  public void getTracksFromPlaylist() {
    PlaylistTrack[] tracks = myAPI.getPlaylistTracks(RAP_CAVIAR_ID);

    assertNotNull(tracks);
    assertTrue(tracks.length > 0);
  }

  @Test
  public void getSongsByIDs() {
    List<Song> songs = myAPI.getSongsFromIds(new String[]
        {halsey_track_id, post_track_id, juice_wrld_track_id});

    assertEquals(3, songs.size());
    assertEquals(halsey_track_id, songs.get(0).getId());
    assertEquals(post_track_id, songs.get(1).getId());
    assertEquals(juice_wrld_track_id, songs.get(2).getId());

    // assume the songs got their dimensional data
    for (Song s : songs) {
      assertNotNull(s.getDimensions());
    }
  }

  @Test
  public void testGetAudioFeatures() {
    String[] ids = new String[]{
        "54X78diSLoUDI3joC2bjMz",
        "7tFiyTwD0nx5a1eklYtX2J",
        "7Jh1bpe76CNTCgdgAdBw4Z",
        "7pKfPomDEeI4TPT6EOYjn9",
        "1yo16b3u0lptm6Cs7lx4AD",
        "3Am0IbOxmvlSXro7N5iSfZ", // strawberry fields
    };

    List<AudioFeatures> features = myAPI.getAudioFeatureForSeveralTracksByID(ids);

    assertEquals(ids.length, features.size());

    for (int i = 0; i < ids.length; i++) {
      assertEquals(ids[i], features.get(i).getId());
    }
  }

  @Test
  public void testLookupMysterySong() {
    // What is this mysterious song?
    // 7jfiiktFwSM2YWTTnCFtWL
    List<Song> song = myAPI.getSongsFromIds(new String[]{"7jfiiktFwSM2YWTTnCFtWL"});

    // this is an extremely odd edge case
    // see album:
    // https://open.spotify.com/album/0kfe6NsSLDG2KiM37dRXWg?si=6OxMvFA9TvK6hULy6nL24g

    // none of the songs in this album have any titles or information about them
    Song mystery = song.get(0);

    assertTrue(mystery.getId() != null && !mystery.getId().equals(""));

    System.out.println(song);
  }

  @Test
  public void getSinglePlaylistFromQuery() {
    PlaylistSimplified[] playlist = myAPI.getSinglePlaylistByQuery("Trap Nation");

    PlaylistSimplified trapNation = playlist[0];

    assertEquals("Trap Nation", trapNation.getName());

    // trap nation playlist ID, the 2.1m follower one
    assertEquals(TRAP_NATION_ID, trapNation.getId());
  }


  @Test
  public void getMultiplePlaylistFromQuery() {
    List<PlaylistSimplified> playlists = myAPI.getMultiplePlaylistsByQuery(
        new String[]{"Trap Nation", "Rap Caviar"});

    PlaylistSimplified trapNation = playlists.get(0);
    PlaylistSimplified rapCaviar = playlists.get(1);

    assertEquals("Trap Nation", trapNation.getName());
    assertEquals(TRAP_NATION_ID, trapNation.getId());
    assertEquals("RapCaviar", rapCaviar.getName());
    assertEquals(RAP_CAVIAR_ID, rapCaviar.getId());
  }

  @Test
  public void testUserDidSaveTrack() {
    String[] ids = {halsey_track_id};
    Boolean[] savedIDs = myAPI.checkUsersSavedTracks(ids);

    System.out.println(savedIDs[0]);
  }

  @Test
  public void testGetSavedTracks() {
    List<String> savedIDs = myAPI.getUserSavedTracksIDs();

    System.out.println(savedIDs);

    //    assertTrue(savedIDs.contains(halsey_track_id));
  }

  @Test
  public void testSavePlaylist() {

  }

  @Test
  public void getUserDisplayName() {
    String userName = myAPI.getCurrentUserName();
    assertTrue(!userName.isEmpty());
  }
}
