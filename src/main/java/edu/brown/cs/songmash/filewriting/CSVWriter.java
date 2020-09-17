package edu.brown.cs.songmash.filewriting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class CSVWriter {

  public CSVWriter() {

  }

  public static File writeCSV(String filename, List<String[]> data) {
    File newFile = new File(filename);
    try (PrintWriter writer = new PrintWriter(newFile)) {

      StringBuilder sb = new StringBuilder();

      for (String[] row : data) {
        for (String col : row) {
          sb.append(col);
          sb.append(',');
        }
        // new row
        sb.append('\n');
      }

      writer.write(sb.toString());

    } catch (FileNotFoundException e) {
      System.err.println("The given file was not found.");
    }

    return newFile;
  }
}
