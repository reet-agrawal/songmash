package edu.brown.cs.net;

import edu.brown.cs.songmash.net.ProxyManager;
import edu.brown.cs.songmash.song.SongMashProxy;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProxyManagerTest {

  @Test
  public void completeTest() {
    SongMashProxy proxy1 = ProxyManager.buildProxy();
    int initialSize = proxy1.getSize();
    List<String> removalIds = new ArrayList<String>();
    removalIds.add("72jCZdH0Lhg93z6Z4hBjgj");
    removalIds.add("1sgOeFTCjl8Mi2bLZOMqIY");

    ProxyManager.filterOutSongs(removalIds);
    assertEquals(initialSize - 2, proxy1.getSize());

        //filtered out songs will also affect newly built proxy
    SongMashProxy proxy2 = ProxyManager.buildProxy();
    assertEquals(initialSize - 2, proxy2.getSize());

    //setting playlist size
    assertEquals(ProxyManager.getPlaylistSize(), 25);
    ProxyManager.setPlaylistSize(20);
    assertEquals(ProxyManager.getPlaylistSize(), 20);
  }

}
