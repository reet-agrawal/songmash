package edu.brown.cs.songmash.song;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class SongTest {
  private static SpotifyAPI myAPI;

  // Without Me by Halsey
  private static final String halsey_track_id = "5p7ujcrUXASCNwRaWNHR1C";

  @Before
  public void setup() {
    myAPI = new SpotifyAPI(
        SpotifyAPITest.credentials_file,
        SpotifyAPITest.tokens_file,
        SpotifyAPITest.callback_url);
  }

  @Test
  public void testCSVSongObjectConversion() {
    String[] ids = new String[]{halsey_track_id};

    List<Song> song = myAPI.getSongsFromIds(ids);

    Song withoutMe = song.get(0);

    String[] withoutMeCSVRow = withoutMe.toCSVRow();

    Song reParse = new Song(withoutMeCSVRow);

    assertEquals(withoutMe, reParse);
  }

}
