package edu.brown.cs.songmash.filewriting;

import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import edu.brown.cs.songmash.song.Song;
import edu.brown.cs.songmash.song.SpotifyAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SongDataCreator {

  private static SpotifyAPI api;

  public SongDataCreator(SpotifyAPI pApi) {
    api = pApi;
  }


  public List<Song> getSongDataFromPlaylists(String[] playlistIDs) {
    Map<String, int[]> songVector = createSongDataFromPlaylists(playlistIDs);

    String[] ids = getSongIDsFromMap(songVector);
    List<Song> allSongs = api.getSongsFromIds(ids);

    for (Song s : allSongs) {
      s.setPlaylistVector(songVector.get(s.getId()));
    }

    return allSongs;
  }

  private String[] getSongIDsFromMap(Map<String, int[]> map) {
    String[] queryset = new String[map.keySet().size()];

    int i = 0;
    for (String t : map.keySet()) {
      queryset[i++] = t;
    }

    return queryset;
  }


  private Map<String, int[]> createSongDataFromPlaylists(String[] playlistIDs) {

    Map<String, int[]> songVectors = new HashMap<>();

    for (int i = 0; i < playlistIDs.length; i++) {

      PlaylistTrack[] tracks = api.getPlaylistTracks(playlistIDs[i]);

      if (tracks != null) {
        for (PlaylistTrack t : tracks) {

          int[] vector = songVectors.get(t.getTrack().getId());

          if (vector != null) {
            // song already exists in map
            vector[i] = 1;
            songVectors.put(t.getTrack().getId(), vector);
          } else {
            // song has never been seen
            int[] newVector = new int[playlistIDs.length];

            // arrays populated by 0 by default
            newVector[i] = 1;
            songVectors.put(t.getTrack().getId(), newVector);
          }
        }
      }
    }

    return songVectors;
  }
}
