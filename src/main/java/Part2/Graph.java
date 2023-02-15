package Part2;

import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;

/**
 * @author aakash
 */
public class Graph {

  public static void main(String[] args) {
    Reader reader = null;
    CSVReader csvReader = null;
    Map<Long, Integer> requestTimeMap = new HashMap<>();

    try {
      reader = Files.newBufferedReader(Paths.get("records.csv"));
      csvReader = new CSVReader(reader);
      String[] nextLine;
      boolean firstTimeRecord = false;
      Timestamp startTime = null;
      while ((nextLine = csvReader.readNext()) != null) {
        if(nextLine[0].equals("Start Time"))
          continue;

        Timestamp timestamp = Timestamp.valueOf(nextLine[0]);
        if(!firstTimeRecord)  {
          startTime = timestamp;
          firstTimeRecord = true;
        }

        long relativeTime = (timestamp.getTime() - startTime.getTime()) / 1000;
        requestTimeMap.merge(relativeTime, 1, Integer::sum);
      }

      File file = new File("plotThroughput.csv");
      if(file.exists()) {
        file.delete();
      }
      PrintWriter printWriter = null;
      try {
        printWriter = new PrintWriter(file);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }

      for (Map.Entry<Long, Integer> entry : requestTimeMap.entrySet()) {
        Long key = entry.getKey();
        Integer value = entry.getValue();
        printWriter.printf("%s, %s \n", key, value);
      }

      csvReader.close();
      reader.close();
      printWriter.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }


}
