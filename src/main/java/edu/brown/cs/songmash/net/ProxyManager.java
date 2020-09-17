package edu.brown.cs.songmash.net;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.songmash.filewriting.SongFeaturesCSVTool;
import edu.brown.cs.songmash.main.Main;
import edu.brown.cs.songmash.song.Song;
import edu.brown.cs.songmash.song.SongMashProxy;

//make sure to call this whenever changing settings, adding rooms

public abstract class ProxyManager {
  static int playlistSize = 25;
  static List<Song> songs = initializeSongs();
  static List<String> blacklist = new ArrayList<>();
  static List<SongMashProxy> proxies = new ArrayList<>();

  static private List<Song> initializeSongs() {
    try {
      return new SongFeaturesCSVTool(Main.api)
          .getSongsFromCSV(Main.DEST_SONG_PROPS_W_VECTOR_CSV);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return new ArrayList<Song>();
    }
  }

  static public SongMashProxy buildProxy() {
    SongMashProxy proxy = new SongMashProxy(songs);
    proxy.removeByIds(blacklist);
    proxies.add(proxy);
    return proxy;
  }

  public static void filterOutSongs(List<String> badIds) {
    System.out.println("filtered out songs:");
    System.out.println(badIds.size());
    System.out.println(badIds);
    System.out.println("----------");
    blacklist.addAll(badIds);
    for (SongMashProxy p : proxies) {
      p.removeByIds(badIds);
    }
  }

  public static void setPlaylistSize(int size) {
    playlistSize = size;
  }

  public static int getPlaylistSize() {
    return playlistSize;
  }


}
