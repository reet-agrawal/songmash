package edu.brown.cs.songmash.filewriting;

import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.model_objects.specification.Track;
import edu.brown.cs.songmash.filereading.CSVReader;
import edu.brown.cs.songmash.song.Song;
import edu.brown.cs.songmash.song.SpotifyAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class SongFeaturesCSVTool {
  private static SpotifyAPI api;
  private static final String[] SONG_CSV_HEADER = new String[]
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

  public SongFeaturesCSVTool(SpotifyAPI pApi) {
    api = pApi;
  }

  public List<Song> getSongsFromCSV(String sourceFilePath)
      throws FileNotFoundException {
    CSVReader reader = new CSVReader(sourceFilePath);

    List<String[]> csvData = reader.getData();

    List<Song> songs = new ArrayList<>();
    for (String[] row : csvData) {
      songs.add(new Song(row));
    }

    return songs;
  }

  private String[] getSearchQueriesFromCSV(String source)
      throws FileNotFoundException {

    CSVReader reader = new CSVReader(source);

    List<String[]> essential_songs = reader.getData();

    String artist;
    String song;

    String[] search_queries = new String[essential_songs.size()];
    int i = 0;

    for (String[] data : essential_songs) {
      artist = data[1].replace("\"", "");
      song = data[2].replace("\"", "");

      search_queries[i++] = (String.format("%s %s", artist, song));
    }

    return search_queries;
  }

  private List<AudioFeatures> getAudioFeaturesForTracks(List<Track> tracks) {
    String[] ids = new String[tracks.size()];

    for (int i = 0; i < tracks.size(); i++) {
      ids[i] = tracks.get(i).getId();
    }

    return api.getAudioFeatureForSeveralTracksByID(ids);
  }

  public File createAndWriteCSVDataForManyTracks(String source_csv_path,
                                                 String dest_csv_path)
      throws FileNotFoundException {
    List<String[]> csvData = createCSVDataForManyTracks(source_csv_path);

    return CSVWriter.writeCSV(dest_csv_path, csvData);
  }


  public List<String[]> createCSVDataForManyTracks(String source_csv_path)
      throws FileNotFoundException {

    String[] queries = getSearchQueriesFromCSV(source_csv_path);

    // fetch songs from API
    List<Track> tracks = api.getMultipleTracksByQuery(queries);
    List<AudioFeatures> features = this.getAudioFeaturesForTracks(tracks);

    // ensure that the track IDs match the features IDs
    for (int i = 0; i < tracks.size(); i++) {
      assert(tracks.get(i) != null && features.get(i) != null);
      assert (tracks.get(i).getId().equals(features.get(i).getId()));
    }

    // lists must be the same size for data to be written correctly
    assert (tracks.size() == features.size());

    List<String[]> csvData = new ArrayList<>();

    // write the CSV header
    csvData.add(SONG_CSV_HEADER);

    for (int i = 0; i < tracks.size(); i++) {
      // FORMAT: id, title, artist, duration, acousticness, energy,
      // instrumentalness, liveness, loudness, speechiness, valence,
      // dancability

      Track t = tracks.get(i);
      AudioFeatures f = features.get(i);

      // add data to CSV data, remove commas
      csvData.add(new String[]{
          t.getId(),
          t.getName().replace(",", ""),
          SpotifyAPI.getArtistStringFromTrackProperty(t).replace(
              ",", ""),
          Double.toString(f.getDurationMs()),
          Double.toString(f.getAcousticness()),
          Double.toString(f.getEnergy()),
          Double.toString(f.getInstrumentalness()),
          Double.toString(f.getLiveness()),
          Double.toString(f.getLoudness()),
          Double.toString(f.getSpeechiness()),
          Double.toString(f.getValence()),
          Double.toString(f.getDanceability())
      });
    }

    // return data that would be written to CSV
    return csvData;
  }


  public File createCSVFromSongs(List<Song> songs, String dest_file_path) {
    List<String[]> csvData = new ArrayList<>();

    csvData.add(SONG_CSV_HEADER);

    for (Song s : songs) {
      csvData.add(s.toCSVRow());
    }

    return CSVWriter.writeCSV(dest_file_path, csvData);
  }
}
