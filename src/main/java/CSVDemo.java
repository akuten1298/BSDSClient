import com.opencsv.CSVWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author aakash
 */
public class CSVDemo {

  public static void main(String[] args) {
    String fileName = "trial1.csv";
    boolean append = true;

    try {
      FileWriter fileWriter = new FileWriter("joma.csv");
      BufferedWriter bf = new BufferedWriter(fileWriter);
      CSVWriter writer = new CSVWriter(bf);

      List<String[]> lines = new ArrayList<>();

      for(int i = 0; i < 500000; i++) {

        String[] data = {String.valueOf(i), " start time", "end time", "latency", "response", "request"};
        lines.add(data);

//        fileWriter.write("trial");
//        meta.append("Start Time,");
//        meta.append(" End Time,");
//        meta.append(" Latency,");
//        meta.append(" Response Code,");
//        meta.append(" Request Type");
//        meta.append(System.getProperty( "line.separator" ));

      }

      writer.writeAll(lines);

      writer.close();
      bf.close();
      fileWriter.close();

//      fileWriter.write(String.valueOf(meta));
//      writer.close();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
