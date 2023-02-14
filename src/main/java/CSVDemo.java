import java.io.FileWriter;
import java.io.IOException;

/**
 * @author aakash
 */
public class CSVDemo {

  public static void main(String[] args) {
    String fileName = "trial1.csv";
    boolean append = true;

    try {
      FileWriter fileWriter = new FileWriter("trial2.csv");
//      CSVWriter writer = new CSVWriter(fileWriter);

      // Write data
      fileWriter.write("Start Time,");
      fileWriter.write(" End Time,");
      fileWriter.write(" Latency,");
      fileWriter.write(" Response Code,");
      fileWriter.write(" Request Type");
      fileWriter.write(System.getProperty( "line.separator" ));
      fileWriter.flush();

      for(int i = 0; i < 500000; i++) {

//        fileWriter.write("trial");
        fileWriter.write("Start Time,");
        fileWriter.write(" End Time,");
        fileWriter.write(" Latency,");
        fileWriter.write(" Response Code,");
        fileWriter.write(" Request Type");
        fileWriter.write(System.getProperty( "line.separator" ));

        fileWriter.flush();

      }

//      writer.close();
      fileWriter.flush();
      fileWriter.close();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
