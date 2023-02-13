import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author aakash
 */
public class CSVCustomWriter implements Runnable{
    public static AtomicInteger atomicInteger = new AtomicInteger(0);
    private BlockingQueue<CSVRecord> queue;

    final PrintWriter printWriter;

    public CSVCustomWriter(BlockingQueue<CSVRecord> queue, PrintWriter printWriter) {
      this.queue = queue;
      this.printWriter = printWriter;
    }
    @Override
    public void run() {
      CSVRecord value = new CSVRecord();

      while (!value.isPoisonPill()) {
        try {
          value = queue.take();
          if(value.getStartTime() != null) {
            printWriter.printf("%s, %s, %s, %s, %s \n",
                value.getStartTime(), value.getEndTime(), value.getLatency(),
                value.getResponseCode(), value.getRequestType());
            atomicInteger.getAndIncrement();
//          System.out.println(atomicInteger.get());
          } else {
            System.out.println("Poison Caught");
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

    }
}
