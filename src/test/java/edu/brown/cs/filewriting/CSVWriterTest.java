package edu.brown.cs.filewriting;

import edu.brown.cs.songmash.filereading.CSVReader;
import edu.brown.cs.songmash.filewriting.CSVWriter;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class CSVWriterTest {
  private static final String TEST_CSV_PATH = "./data/test_csv.csv";

  @Test
  public void createSimpleCSV() {
    List<String[]> testData = new ArrayList<>();

    testData.add(new String[]{"header1", "header2", "header3", "header4"});
    testData.add(new String[]{"test1", "test2", "test3", "test4"});
    testData.add(new String[]{"test5", "test6", "test7", "test8"});

    File testFile = CSVWriter.writeCSV(TEST_CSV_PATH, testData);

    assertTrue(testFile.exists());

    try {
      CSVReader reader = new CSVReader(TEST_CSV_PATH);
      List<String[]> csvData = reader.getData();

      assertEquals(csvData.get(0)[0], testData.get(1)[0]);
      assertEquals(csvData.get(1)[0], testData.get(2)[0]);

    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.err.println("could not find file.");
    }
  }


}
