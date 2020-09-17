package edu.brown.cs.songmash.filereading;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Reads data from csv files.
 *
 * @author geoffreyglass
 *
 */
public class CSVReader {
  private String[] fields;
  private ArrayList<String[]> data;

  /**
   * Constructs a new CSVReader.
   * @param filename
   *          the file to read from
   * @throws FileNotFoundException
   *           the exception when the filename is not found in the project
   *           directory
   */
  public CSVReader(String filename) throws FileNotFoundException {
    File f = new File(filename);
    scanFields(f);
    scanData(f);
  }

  private void scanFields(File f) throws FileNotFoundException {
    Scanner input = new Scanner(f);
    this.fields = input.next().split(",");
    input.close();
  }

  private void scanData(File f) throws FileNotFoundException {
    this.data = new ArrayList<String[]>();
    Scanner input = new Scanner(f);
    input.useDelimiter("\n");
    if (input.hasNext()) {
      input.next(); // skip the header line
    }
    while (input.hasNext()) {
      data.add(input.next().split(","));
    }
  }

  /**
   * Gets the data from the csv file.
   * @return an arraylist where each element is a "row" of strings
   */
  public ArrayList<String[]> getData() {
    return this.data;
  }

}
