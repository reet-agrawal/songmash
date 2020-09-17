package edu.brown.cs.songmash.song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import edu.brown.cs.songmash.kmeans.KMeanable;

public class Song implements KMeanable {

  private String id;
  private String title;
  private String artist;
  private double duration;
  private double acousticness;
  private double danceability;
  private double energy;
  private double instrumentalness;
  private double liveness;
  private double loudness;
  private double speechiness;
  private double valence;
  private int[] playlistVector;

  private static final int EXPECTED_CSV_ROW_MIN = 12;

  public Song(String pId, String title, String artist, double[] data) {
    // FORMAT: id, title, artist, duration, acousticness, energy,
    // instrumentalness, liveness, loudness, speechiness, valence,
    // dancability
    this.id = pId;
    this.title = title;
    this.artist = artist;
    this.duration = data[0];
    this.acousticness = data[1];
    this.energy = data[2];
    this.instrumentalness = data[3];
    this.liveness = data[4];
    this.loudness = data[5];
    this.speechiness = data[6];
    this.valence = data[7];
    this.danceability = data[8];

    // empty playlist vector
    this.playlistVector = null;
  }

  public Song(String pId, String title, String artist, double[] data,
              int[] pPlaylistVector) {
    // FORMAT: id, title, artist, duration, acousticness, energy,
    // instrumentalness, liveness, loudness, speechiness, valence, dancability
    // playlist vector
    this.title = title;
    this.artist = artist;
    this.id = pId;
    this.duration = data[0];
    this.acousticness = data[1];
    this.energy = data[2];
    this.instrumentalness = data[3];
    this.liveness = data[4];
    this.loudness = data[5];
    this.speechiness = data[6];
    this.valence = data[7];
    this.danceability = data[8];
    this.playlistVector = pPlaylistVector;
  }

  public Song(String[] csvRow){
    // data must be at least this long
    assert(csvRow.length >= EXPECTED_CSV_ROW_MIN);

    // basic details
    this.id = csvRow[0];
    this.title = csvRow[1];
    this.artist = csvRow[2];

    // dimensions
    this.duration = Double.parseDouble(csvRow[3]);
    this.acousticness = Double.parseDouble(csvRow[4]);
    this.energy = Double.parseDouble(csvRow[5]);
    this.instrumentalness = Double.parseDouble(csvRow[6]);
    this.liveness = Double.parseDouble(csvRow[7]);
    this.loudness = Double.parseDouble(csvRow[8]);
    this.speechiness = Double.parseDouble(csvRow[9]);
    this.valence = Double.parseDouble(csvRow[10]);
    this.danceability = Double.parseDouble(csvRow[11]);

    if(csvRow.length > EXPECTED_CSV_ROW_MIN) {

      int[] pVector = new int[csvRow.length - EXPECTED_CSV_ROW_MIN];

      for (int i = 0; i < csvRow.length - EXPECTED_CSV_ROW_MIN; i++) {
        // need to parse playlist vector
        try{
          pVector[i] = Integer.parseInt(csvRow[EXPECTED_CSV_ROW_MIN + i]);
        } catch (NumberFormatException ignored){

        }
      }

      this.playlistVector = pVector;
    } else {
      // no playlist vector was specified
      this.playlistVector = null;
    }
  }

  public void setPlaylistVector(int[] playlistVector) {
    this.playlistVector = playlistVector;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public double getDuration() {
    return duration;
  }

  public double getAcousticness() {
    return acousticness;
  }

  public double getDanceability() {
    return danceability;
  }

  public double getEnergy() {
    return energy;
  }

  public double getInstrumentalness() {
    return instrumentalness;
  }

  public double getLiveness() {
    return liveness;
  }

  public double getLoudness() {
    return loudness;
  }

  public double getSpeechiness() {
    return speechiness;
  }

  public double getValence() {
    return valence;
  }


  public String getArtist() {
    return this.artist;
  }

  public int[] getPlaylistVector() {
    return this.playlistVector;
  }

  @Override
  public double[] getDimensions() {
    return new double[]{
        this.duration,
        this.acousticness,
        this.energy,
        this.instrumentalness,
        this.liveness,
        this.loudness,
        this.speechiness,
        this.valence,
        this.danceability
    };
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Song song = (Song) o;
    return Double.compare(song.duration, duration) == 0 &&
        Double.compare(song.acousticness, acousticness) == 0 &&
        Double.compare(song.danceability, danceability) == 0 &&
        Double.compare(song.energy, energy) == 0 &&
        Double.compare(song.instrumentalness, instrumentalness) == 0 &&
        Double.compare(song.liveness, liveness) == 0 &&
        Double.compare(song.loudness, loudness) == 0 &&
        Double.compare(song.speechiness, speechiness) == 0 &&
        Double.compare(song.valence, valence) == 0 &&
        Arrays.equals(song.playlistVector, playlistVector) &&
        Objects.equals(id, song.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        duration,
        acousticness,
        danceability,
        energy,
        instrumentalness,
        liveness,
        loudness,
        speechiness,
        valence,
        playlistVector
    );
  }

  @Override
  public String toString() {
    return String.format("SONG OBJECT: %s by %s (%s): [%s]", this.getTitle(),
        this.getArtist(), this.getId(), Arrays.toString(this.getDimensions()));
  }

  public String[] toCSVRow() {
    String[] basicProperties = new String[]{
        this.id,
        this.title.replace(",", ""),
        this.artist.replace(",", ""),
        Double.toString(this.duration),
        Double.toString(this.acousticness),
        Double.toString(this.energy),
        Double.toString(this.instrumentalness),
        Double.toString(this.liveness),
        Double.toString(this.loudness),
        Double.toString(this.speechiness),
        Double.toString(this.valence),
        Double.toString(this.danceability)
    };

    if(this.playlistVector != null){
      List<String> totalProps = new ArrayList<>(Arrays.asList(basicProperties));
      for(int s: playlistVector){
        totalProps.add(Integer.toString(s));
      }
      return totalProps.toArray(new String[0]);
    } else {
      return basicProperties;
    }
  }
}
