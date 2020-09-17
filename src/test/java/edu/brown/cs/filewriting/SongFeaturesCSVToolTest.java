package edu.brown.cs.filewriting;

import edu.brown.cs.songmash.filewriting.SongFeaturesCSVTool;
import edu.brown.cs.songmash.song.Song;
import edu.brown.cs.songmash.song.SpotifyAPI;
import edu.brown.cs.songmash.song.SpotifyAPITest;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;

public class SongFeaturesCSVToolTest {
  private static SpotifyAPI myAPI;

  // file paths
  private static final String TEN_SONGS = "./data/10_essential_songs.csv";
  private static final String FIFTY_SONGS = "./data/50_essential_songs.csv";
  private static final String HUNDRED_SONGS = "./data/100_essential_songs.csv";
  private static final String ALL_SONGS = "./data/893_essential_songs.csv";
  private static final String HUNDRED_SONG_PROPS = "./data/song_props_hundred_test.csv";

  private static final int ARTIST_INDEX = 2;

  private static final String[] CSV_HEADER = new String[]
      {
          "id",
          "title",
          "artist",
          "duration",
          "acousticness",
          "energy",
          "instrumentalness",
          "liveness",
          "loudness",
          "speechiness",
          "valence",
          "danceability"
      };

  @Before
  public void setup() {
    myAPI = new SpotifyAPI(
        SpotifyAPITest.credentials_file,
        SpotifyAPITest.tokens_file,
        SpotifyAPITest.callback_url);
  }

  @Test
  public void testSetup() {
    assertTrue(myAPI.isAuthorized());
  }

  @Test
  public void testCSVWriteForTenSongs() {
    SongFeaturesCSVTool gen = new SongFeaturesCSVTool(myAPI);
    try {

      List<String[]> songs = gen.createCSVDataForManyTracks(TEN_SONGS);

      // 10 songs + header for CSV write
      assertEquals(11, songs.size());

    } catch (FileNotFoundException e) {
      System.err.println("10 Song CSV File not found.");
      e.printStackTrace();
    }
  }

  //  @Test
  public void testCSVWriteForFiftySongs() {
    SongFeaturesCSVTool gen = new SongFeaturesCSVTool(myAPI);
    try {

      List<String[]> songs = gen.createCSVDataForManyTracks(FIFTY_SONGS);

      assertArrayEquals(CSV_HEADER, songs.get(0));

      // data format:
      // id, title, artist, duration, acousticness, energy,
      // instrumentalness, liveness, loudness, speechiness, valence,
      // dancability

      // first artist should be 'Prince'
      assertEquals("Prince", songs.get(1)[ARTIST_INDEX]);

      // 25th artist should be 'The Band'
      assertEquals("The Band", songs.get(25)[ARTIST_INDEX]);

      // 30th artist should be 'The Beatles'
      assertEquals("The Beatles", songs.get(30)[ARTIST_INDEX]);

      // 31st artist should be 'Radiohead'
      assertEquals("Radiohead", songs.get(31)[ARTIST_INDEX]);

      // 32nd artist should be 'The Beatles'
      assertEquals("The Beatles", songs.get(32)[ARTIST_INDEX]);

      /*
      I used this test to discover that there are songs not available in
      Spotify that were causing issues. One such song is

      "A Change is Going to Come" by Sam Cooke

      We must remove it from the csvs.

      Thus, the 50 essentials songs CSV has been reduce to 49 essential songs.
       */

      // with Sam Cooke removed, the 49th artist should be Fleetwood Mac
      assertEquals("Fleetwood Mac", songs.get(49)[ARTIST_INDEX]);

      // 49 songs + header for CSV write
      assertEquals(50, songs.size());

    } catch (FileNotFoundException e) {
      System.err.println("50 Song CSV File not found.");
      e.printStackTrace();
    }
  }

  @Test
  public void testCSVWriteForOneHundredSongs() {
    SongFeaturesCSVTool gen = new SongFeaturesCSVTool(myAPI);
    try {

      List<String[]> songs = gen.createCSVDataForManyTracks(HUNDRED_SONGS);

      assertArrayEquals(CSV_HEADER, songs.get(0));

      // first artist should be 'Prince'
      assertEquals("Prince", songs.get(1)[ARTIST_INDEX]);

      // with Sam Cooke removed, the 49th artist should be Fleetwood Mac
      assertEquals("Fleetwood Mac", songs.get(49)[ARTIST_INDEX]);

      // the 99th artist should be The Beatles
      assertEquals("The Beatles", songs.get(99)[ARTIST_INDEX]);

      // 99 songs + header for CSV write
      assertEquals(100, songs.size());

    } catch (FileNotFoundException e) {
      System.err.println("100 Song CSV File not found.");
      e.printStackTrace();
    }
  }

  //  @Test
  public void testCSVWriteForAllSongs() {
    SongFeaturesCSVTool gen = new SongFeaturesCSVTool(myAPI);
    try {
      List<String[]> songs = gen.createCSVDataForManyTracks(ALL_SONGS);

      assertArrayEquals(CSV_HEADER, songs.get(0));

      // first artist should be 'Prince'
      assertEquals("Prince", songs.get(1)[ARTIST_INDEX]);

      // with Sam Cooke removed, the 49th artist should be Fleetwood Mac
      assertEquals("Fleetwood Mac", songs.get(49)[ARTIST_INDEX]);

      // the 99th artist should be The Beatles
      assertEquals("The Beatles", songs.get(99)[ARTIST_INDEX]);

      // 893 - 1 = 892, songs + header for CSV write
      int totalSongs = 893;

      // in the list of 'essential' songs there are 7 that cannot be found
      // on Spotify
      int cantFind = 7;

      assertEquals(totalSongs - cantFind, songs.size());

    } catch (FileNotFoundException e) {
      System.err.println("100 Song CSV File not found.");
      e.printStackTrace();
    }
  }

  @Test
  public void testParseSongsFromCSV() {
    SongFeaturesCSVTool csvTool = new SongFeaturesCSVTool(myAPI);

    try {
      List<Song> songs = csvTool.getSongsFromCSV(HUNDRED_SONG_PROPS);

      // 100 songs - the 1 song not available in Spotify
      assertEquals(99, songs.size());

      for (Song s : songs) {
        assertNotNull(s.getDimensions());
        assertTrue(!s.getId().equals(""));
        assertTrue(!s.getTitle().equals(""));
        assertTrue(!s.getArtist().equals(""));
      }

      Song purpleRain = songs.get(0);
      assertEquals("Prince", purpleRain.getArtist());
      assertEquals("Purple Rain", purpleRain.getTitle());
      assertEquals("54X78diSLoUDI3joC2bjMz", purpleRain.getId());
      assertEquals(520787.0, purpleRain.getDuration());
      assertEquals(0.03530000150203705, purpleRain.getAcousticness());
      assertEquals(0.4519999921321869, purpleRain.getEnergy());
      assertEquals(0.0022799998987466097, purpleRain.getInstrumentalness());
      assertEquals(0.6890000104904175, purpleRain.getLiveness());
      assertEquals(-10.42199993133545, purpleRain.getLoudness());
      assertEquals(0.030700000002980232, purpleRain.getSpeechiness());
      assertEquals(0.1889999955892563, purpleRain.getValence());
      assertEquals(0.367000013589859, purpleRain.getDanceability());

      Song strawberryFields = songs.get(98);
      assertEquals("The Beatles", strawberryFields.getArtist());
      assertEquals("Strawberry Fields Forever - Remastered 2009", strawberryFields.getTitle());
      assertEquals("3Am0IbOxmvlSXro7N5iSfZ", strawberryFields.getId());
      assertEquals(247320.0, strawberryFields.getDuration());
      assertEquals(0.335999995470047, strawberryFields.getAcousticness());
      assertEquals(0.5019999742507935, strawberryFields.getEnergy());

      // scientific notation works for doubles
      assertEquals(1.3800000306218863E-4, strawberryFields.getInstrumentalness());

      assertEquals(0.07129999995231628, strawberryFields.getLiveness());
      assertEquals(-12.277000427246094, strawberryFields.getLoudness());
      assertEquals(0.17800000309944153, strawberryFields.getSpeechiness());
      assertEquals(0.289000004529953, strawberryFields.getValence());
      assertEquals(0.38999998569488525, strawberryFields.getDanceability());

    } catch (FileNotFoundException e) {
      System.err.println("Cannot find file at " + HUNDRED_SONG_PROPS);
    }
  }
}

