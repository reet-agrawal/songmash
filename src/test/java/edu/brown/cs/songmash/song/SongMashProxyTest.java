//package test.java.edu.brown.cs.songmash.song;
//
//import java.io.FileNotFoundException;
//import java.util.ArrayList;
//import java.util.List;
//
//import edu.brown.cs.songmash.filewriting.SongFeaturesCSVTool;
//import edu.brown.cs.songmash.main.Main;
//import edu.brown.cs.songmash.song.Song;
//import edu.brown.cs.songmash.song.SongMashProxy;
//import org.junit.Test;
//
//import static org.junit.Assert.assertEquals;
//
//public class SongMashProxyTest {
//
//  @Test
//  public void completeTest() {
//    List<Song> songs;
//    try {
//      songs = new SongFeaturesCSVTool(Main.api)
//          .getSongsFromCSV(Main.DEST_SONG_PROPS_W_VECTOR_CSV);
//    } catch (FileNotFoundException e) {
//      e.printStackTrace();
//      songs = new ArrayList<Song>();
//    }
//    SongMashProxy proxy = new SongMashProxy(songs);
//    List<String> removalIds = new ArrayList<String>();
//    removalIds.add("72jCZdH0Lhg93z6Z4hBjgj");
//    removalIds.add("1sgOeFTCjl8Mi2bLZOMqIY");
//
//    int initialSize = proxy.getSize();
//    proxy.removeByIds(removalIds);
//    assertEquals(initialSize - 2, proxy.getSize());
//    assertEquals(proxy.getNewSongs(), 2);
//    assertEquals(proxy.getPlaylist(20).size(), 20);
//  }
//
//}
