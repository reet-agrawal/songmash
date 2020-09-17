package edu.brown.cs.filewriting;

import edu.brown.cs.songmash.filereading.CSVReader;
import edu.brown.cs.songmash.filewriting.SongDataCreator;
import edu.brown.cs.songmash.song.Song;
import edu.brown.cs.songmash.song.SpotifyAPI;
import edu.brown.cs.songmash.song.SpotifyAPITest;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

public class SongDataCreatorTest {
  private static final String PLAYLISTS_CSV_PATH = "./data/Spotify_Genre_Playlists.csv";

  // contains Without Me by Halsey and Lucid Dreams by Juice WRLD
  private static final String TEST_PLAYLIST_1_ID = "2HGIGmraGwvlVvJFoXkQi9";

  // contains Without Me by Halsey and Wow. by Post Malone
  private static final String TEST_PLAYLIST_2_ID = "4bXcetCyMv4QS3OdXJxdTX";

  // contains all the songs from the previous playlists + I Like Me Better by Lauv but duplicated
  private static final String DUPE_PLAYLIST_ID = "13AdKl2hmb4MlVjoUZHlMW";

  private static SpotifyAPI myAPI;
  private String[] playlistIDs;

  private static final int ID_COLUMN = 1;

  @Before
  public void setup() {
    myAPI = new SpotifyAPI(
        SpotifyAPITest.credentials_file,
        SpotifyAPITest.tokens_file,
        SpotifyAPITest.tokens_file);

    try {
      CSVReader reader = new CSVReader(PLAYLISTS_CSV_PATH);
      List<String[]> csvData = reader.getData();

      assertTrue(csvData.size() > 0);

      playlistIDs = new String[csvData.size()];

      for (int i = 0; i < csvData.size(); i++) {
        playlistIDs[i] = csvData.get(i)[ID_COLUMN];
      }


    } catch (FileNotFoundException e) {
      System.err.println("File for playlists not found.");
      e.printStackTrace();
    }
  }

  @Test
  public void testCreateSongsFromSinglePlaylist() {
    SongDataCreator creator = new SongDataCreator(myAPI);

    String[] playlistSubset = Arrays.copyOfRange(playlistIDs, 0, 1);

    List<Song> songs = creator.getSongDataFromPlaylists(playlistSubset);

    for (Song s : songs) {
      int[] plVector = s.getPlaylistVector();

      // only 1 playlist was used as a source, so the playlist vector should
      // only have 1 index, corresponding to that playlist
      assertEquals(1, plVector.length);
      assertEquals(1, plVector[0]);
    }
  }

  @Test
  public void testCreateSongsFromMultiplePlaylists() {
    SongDataCreator creator = new SongDataCreator(myAPI);

    String[] playlistSubset = new String[]{TEST_PLAYLIST_1_ID, TEST_PLAYLIST_2_ID};

    List<Song> songs = creator.getSongDataFromPlaylists(playlistSubset);

    assertEquals(3, songs.size());

    for (Song s : songs) {
      // length of 2 for each playlist examined
      assertEquals(2, s.getPlaylistVector().length);
    }

    assertEquals("Halsey", songs.get(0).getArtist());
    assertEquals("Juice WRLD", songs.get(1).getArtist());
    assertEquals("Post Malone", songs.get(2).getArtist());

    Song withoutMe = songs.get(0);
    Song lucidDreams = songs.get(1);
    Song wow = songs.get(2);


    // Without Me by Halsey appears in both playlists. Vector should reflect
    // that
    assertEquals(1, withoutMe.getPlaylistVector()[0]);
    assertEquals(1, withoutMe.getPlaylistVector()[1]);

    // Lucid Dreams by Juice WRLD appears ONLY in playlist 1.
    assertEquals(1, lucidDreams.getPlaylistVector()[0]);
    assertEquals(0, lucidDreams.getPlaylistVector()[1]);

    // Wow. by Post Malone appears ONLY in playlist 1.
    assertEquals(0, wow.getPlaylistVector()[0]);
    assertEquals(1, wow.getPlaylistVector()[1]);
  }

  @Test
  public void testCreateSongsFromThreePlaylists() {
    SongDataCreator creator = new SongDataCreator(myAPI);

    String[] playlistSubset = new String[]{TEST_PLAYLIST_1_ID, TEST_PLAYLIST_2_ID, DUPE_PLAYLIST_ID};

    List<Song> songs = creator.getSongDataFromPlaylists(playlistSubset);

    // each song should be unique; Lauv's I like me better should only be counted once
    assertEquals(4, songs.size());

    for (Song s : songs) {
      assertEquals(3, s.getPlaylistVector().length);
    }

    assertEquals("Halsey", songs.get(0).getArtist());
    assertEquals("Juice WRLD", songs.get(1).getArtist());
    assertEquals("Lauv", songs.get(2).getArtist());
    assertEquals("Post Malone", songs.get(3).getArtist());

    Song withoutMe = songs.get(0);
    Song lucidDreams = songs.get(1);
    Song iLikeMeBetter = songs.get(2);
    Song wow = songs.get(3);

    // Without Me by Halsey appears in ALL playlists.
    String withoutMeVector = Arrays.toString(withoutMe.getPlaylistVector());
    assertEquals("[1, 1, 1]", withoutMeVector);

    String[] withoutMeCSVRow = withoutMe.toCSVRow();

    int baseNumRows = 12;
    int totalPlaylists = 3;
    assertEquals(baseNumRows + totalPlaylists, withoutMeCSVRow.length);

    // basic details
    assertEquals(withoutMe.getId(), withoutMeCSVRow[0]);
    assertEquals(withoutMe.getTitle(), withoutMeCSVRow[1]);
    assertEquals(withoutMe.getArtist(), withoutMeCSVRow[2]);

    // dimensions
    assertEquals(withoutMe.getDuration(), Double.parseDouble(withoutMeCSVRow[3]));
    assertEquals(withoutMe.getAcousticness(), Double.parseDouble(withoutMeCSVRow[4]));
    assertEquals(withoutMe.getEnergy(), Double.parseDouble(withoutMeCSVRow[5]));
    assertEquals(withoutMe.getInstrumentalness(), Double.parseDouble(withoutMeCSVRow[6]));
    assertEquals(withoutMe.getLiveness(), Double.parseDouble(withoutMeCSVRow[7]));
    assertEquals(withoutMe.getLoudness(), Double.parseDouble(withoutMeCSVRow[8]));
    assertEquals(withoutMe.getSpeechiness(), Double.parseDouble(withoutMeCSVRow[9]));
    assertEquals(withoutMe.getValence(), Double.parseDouble(withoutMeCSVRow[10]));
    assertEquals(withoutMe.getDanceability(), Double.parseDouble(withoutMeCSVRow[11]));

    // playlist vector
    assertEquals(1, Integer.parseInt(withoutMeCSVRow[12]));
    assertEquals(1, Integer.parseInt(withoutMeCSVRow[13]));
    assertEquals(1, Integer.parseInt(withoutMeCSVRow[14]));

    // Lucid Dreams by Juice WRLD appears in 2 playlists (playlist 1 and 3)
    String lucidDreamsVector = Arrays.toString(lucidDreams.getPlaylistVector());
    assertEquals("[1, 0, 1]", lucidDreamsVector);


    // I Like Me Better by Lauv appears in 1 playlist only (playlist 3)
    String iLikeMeBetterVector = Arrays.toString(iLikeMeBetter.getPlaylistVector());
    assertEquals("[0, 0, 1]", iLikeMeBetterVector);

    // Lucid Dreams by Juice WRLD appears in 2 playlists (playlist 2 and 3)
    String wowVector = Arrays.toString(wow.getPlaylistVector());
    assertEquals("[0, 1, 1]", wowVector);
  }

//  @Test
  public void testCreateSongsFromAllPlaylists() {
    SongDataCreator creator = new SongDataCreator(myAPI);

    List<Song> songs = creator.getSongDataFromPlaylists(playlistIDs);
    assertTrue(songs.size() > 0);

    Set<String> uniqueIds = new HashSet<>();

    // this is difficult to test, as these playlists are public and may change
    for (Song s : songs) {
      assertNotNull(s);
      assertNotNull(s.getPlaylistVector());
      assertNotNull(s.getDimensions());
      assertTrue(s.getId() != null && !s.getId().equals(""));
      assertTrue(s.getArtist() != null && !s.getArtist().equals(""));
      assertTrue(s.getTitle() != null && !s.getTitle().equals(""));
      assertEquals(playlistIDs.length, s.getPlaylistVector().length);

      uniqueIds.add(s.getId());
    }


    // every song in the set of songs should be unique
    assertEquals(uniqueIds.size(), songs.size());

    System.out.println("Number of unique songs pulled = " + songs.size());
  }
}
