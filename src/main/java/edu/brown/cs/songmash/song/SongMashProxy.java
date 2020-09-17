package edu.brown.cs.songmash.song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.brown.cs.songmash.kmeans.KMeans;

public class SongMashProxy {
  List<Song> songs;
  Map<String, Song> ids;
  Map<Song, Double> scores;
  KMeans<Song> cluster;
  final int NUMBER_CLUSTERS = 10;
  final int NUMBER_ITERATIONS = 2000;

  int userID = (int) (Math.random() * 10000); // what is this for?

  public SongMashProxy(List<Song> inputSongs) {
    this.songs = new ArrayList<Song>(new HashSet<Song>(inputSongs));

    this.ids = new HashMap<String, Song>();
    for (Song s : this.songs) {
      ids.put(s.getId(), s);
    }

    this.scores = new HashMap<Song, Double>();
    for (Song s : this.songs) {
      scores.put(s, 0.0);
    }

    this.cluster = new KMeans<Song>(this.songs, NUMBER_CLUSTERS,
        NUMBER_ITERATIONS);
    this.cluster.calculateMeans();
    this.cluster.findClusters();
  }

  /**
   * Removes a list of ids
   * @param removeIds
   *          the list of ids.
   */
  public void removeByIds(List<String> removeIds) {
    for (String id : removeIds) {
      if (ids.containsKey(id)) {
        Song s = ids.get(id);
        songs.remove(s);
        scores.remove(s);
        ids.remove(s);
      }
    }
  }

  public String[] getNewSongs() {
    Random rand = new Random();
    int valA = 0;
    int valB = 0;
    while (valA == valB) {
      valA = rand.nextInt(this.songs.size());
      valB = rand.nextInt(this.songs.size());
    }

    Song songA = this.songs.get(valA);
    Song songB = this.songs.get(valB);
    String idA = songA.getId();
    String idB = songB.getId();
    return new String[] {
        idA, idB
    };
  }

  public void updateScores(String goodId, String badId) {
    int[] goodPlaylists = ids.get(goodId).getPlaylistVector();
    int[] badPlaylists = ids.get(badId).getPlaylistVector();
    int goodCluster = cluster.classify(ids.get(goodId));
    int badCluster = cluster.classify(ids.get(badId));
    for (Song s : songs) {
      double increment = 0;
      if (cluster.classify(s) == goodCluster) {
        increment += 0.1;
      }
      if (cluster.classify(s) == badCluster) {
        increment -= 0.1;
      }
      
      
      
      for (int i = 0; i < goodPlaylists.length; i++) {
        if (goodPlaylists[i] == 1 && s.getPlaylistVector()[i] == 1) {
          increment += 1;
        }
        if (badPlaylists[i] == 1 && s.getPlaylistVector()[i] == 1) {
          increment -= 1;
        }
      }
      scores.replace(s, scores.get(s) + increment);
    }

  }

  /**
   * 
   * @param size
   *          the number of songs in the playlist
   * @return list of song id's
   */
  public List<String> getPlaylist(int size) {
    assert size > 0;

    List<String> playlist = new ArrayList<>();
    List<Double> tracker = new ArrayList<>();

    for(String id : ids.keySet()){
      Song thisSong = ids.get(id);
      double score = scores.get(thisSong);

      if(tracker.size() == 0) {
        tracker.add(score);
        playlist.add(id);
      }else if(tracker.size() < size){
        for(int i = 0; i < tracker.size(); i++){
          if(score > tracker.get(i)){
            tracker.add(i, score);
            playlist.add(i, id);
            break;
          }
          if(i == tracker.size() - 1){
            tracker.add(score);
            playlist.add(id);
            break;
          }
        }
      }else{
        for(int i = 0; i < size; i++){
          if(score > tracker.get(i)){
            tracker.set(i, score);
            playlist.set(i, id);
            break;
          }
        }
      }
    }

    assert playlist.size() == size;
    return playlist;
  }

    public int getSize() {
      return songs.size();
    }
}
