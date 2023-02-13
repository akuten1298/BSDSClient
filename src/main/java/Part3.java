import com.opencsv.CSVWriter;
import io.swagger.client.ApiException;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author aakash
 */
public class Part3 {

  private static final String LEFT = "left";
  private static final String RIGHT = "right";
  private static final int SWIPEE_MAX = 100000;
  private static final int SWIPER_MAX = 5000;
  private static final int COMMENTS_MAX_CHAR_LIMIT = 256;
  private static final int LOWER_CASE_ALPHABET_CHAR_LIMIT = 26;
  private static final String EC2_SERVER_PATH = "http://54.70.221.86:8080/BSDSServer_war/";

  private static final String LOCAL_SERVER_PATH = "http://localhost:8082/BSDSServer_war_exploded/";
  private static final int NUM_THREADS = 100;

  private static final int NUM_CONSUMER_THREADS = 1;
  private static final int TASKS_PER_THREAD = 5000;

  private static final String REQUEST_TYPE = "POST";
  private static final String CSV_FILED_SEPERATOR = ",";

  public static AtomicInteger failedCount;
  public static AtomicInteger totalCount;

  private static BlockingQueue<CSVRecord> queue = new ArrayBlockingQueue<>(NUM_THREADS * TASKS_PER_THREAD + 100);

  private static Queue<CSVRecord> globalQueue = new ConcurrentLinkedQueue<>();


  public static void main(String[] args) {

    Runnable runnableTask = Part3::sendRequest;
    failedCount = new AtomicInteger(0);
    totalCount = new AtomicInteger(0);

    File file = new File("records.csv");
    if(file.exists()) {
      file.delete();
    }

    PrintWriter printWriter = null;
    try {
      printWriter = new PrintWriter(file);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    Thread[] csvWriters = new Thread[NUM_CONSUMER_THREADS];
    for(int i = 0; i < NUM_CONSUMER_THREADS; i++) {
      csvWriters[i] = new Thread(new CSVCustomWriter(queue, printWriter));
      csvWriters[i].start();
    }

    // Ec2 request start

    Timestamp startTime = Timestamp.from(Instant.now());
    System.out.println("Start Time: " + startTime);

    Thread tids[] = new Thread[NUM_THREADS];
    for(int i = 0; i <  NUM_THREADS; i++) {
      tids[i] = new Thread(runnableTask);
      tids[i].start();
    }

    try {
      for(int i = 0; i <  NUM_THREADS; i++) {
        tids[i].join();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    System.out.println("Total produced records: " + totalCount.get());

    Timestamp endTime = Timestamp.from(Instant.now());
    System.out.println("End Time: " + endTime);

    long timeDiff = endTime.getTime() - startTime.getTime();
    long totalTimeInSeconds = timeDiff/1000;
    if(totalTimeInSeconds == 0) {
      totalTimeInSeconds = 1;
    }
    long throughput = (NUM_THREADS * TASKS_PER_THREAD)/totalTimeInSeconds;
    long latency = 1000/(throughput/NUM_THREADS);

    System.out.println("Total time: " + timeDiff + " ms");
    System.out.println("Total time in seconds: " + totalTimeInSeconds);

    System.out.println("Throughput: " + throughput + " requests/second");
    System.out.println("Latency: " + latency + " ms");

    System.out.println("Number of successful requests: " + (NUM_THREADS * TASKS_PER_THREAD - failedCount.get()));
    System.out.println("Number of unsuccessful requests: " + failedCount.get());

    try {
      queue.put(new CSVRecord(true));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    try {
      for(int i = 0; i < NUM_CONSUMER_THREADS; i++) {
        csvWriters[i].join();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    System.out.println("Total consumed records: " + CSVCustomWriter.atomicInteger.get());
    printWriter.flush();
    printWriter.close();


    // CSV CSV CSV CSV

//    String fileName = "jumpy";
//    String fileExtension = ".csv";
//    boolean append = true;
//
//    try {
//
//      // Write data
//      List<String[]> list = new ArrayList<>();
//
//      for(CSVRecord record : globalQueue) {
//        String[] data = {record.getStartTime(), record.getEndTime(), record.getLatency(), record.getResponseCode(), record.getRequestType()};
//        list.add(data);
//      }
//
//      int total_files = list.size()%200000 == 0 ? list.size()/200000 :  list.size()/200000 + 1;
//
//      for(int i = 0; i < total_files; i++) {
//        int j = 0;
//        FileWriter fileWriter = new FileWriter(fileName+ i +fileExtension, append);
//        CSVWriter writer = new CSVWriter(fileWriter);
//        String[] header = {"Start Time", "End Time", "Latency", "Response Code", "Request Type"};
//        writer.writeNext(header);
//        List<String[]> subListData = new ArrayList<>();
//        while(j < 200000 && i*200000 + j < list.size()) {
//          subListData.add(list.get(i*200000 + j));
//          j++;
//        }
//        writer.writeAll(subListData);
//      }
//
//    } catch (IOException e) {
//      e.printStackTrace();
//    }

    // Create a writer object
//    String fileName = "new3.csv";
//    boolean append = true;
//
//    try {
//      FileWriter fileWriter = new FileWriter("new2.csv");
////      CSVWriter writer = new CSVWriter(fileWriter);
//
//      // Write data
//      fileWriter.write("Start Time,");
//      fileWriter.write(" End Time,");
//      fileWriter.write(" Latency,");
//      fileWriter.write(" Response Code,");
//      fileWriter.write(" Request Type");
//      fileWriter.write(System.getProperty( "line.separator" ));
//      fileWriter.flush();
//
//      for(CSVRecord queue : globalQueue) {
//
//        fileWriter.write("trial");
////        fileWriter.write(CSV_FILED_SEPERATOR);
////        fileWriter.write(queue.getEndTime()); fileWriter.write(CSV_FILED_SEPERATOR);
////        fileWriter.write(queue.getLatency()); fileWriter.write(CSV_FILED_SEPERATOR);
////        fileWriter.write(queue.getResponseCode()); fileWriter.write(CSV_FILED_SEPERATOR);
////        fileWriter.write(queue.getRequestType());
//
//        fileWriter.write(System.getProperty( "line.separator" ));
//        fileWriter.flush();
//
//      }
//
////      writer.close();
//      fileWriter.flush();
//      fileWriter.close();
//
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    }
  }

  public static void sendRequest() {
    SwipeDetails body = new SwipeDetails();
    SwipeApi apiInstance = new SwipeApi();
    apiInstance.getApiClient().setBasePath(EC2_SERVER_PATH);

    for(int i = 0; i < TASKS_PER_THREAD; i++) {
      Timestamp requestStartTime = Timestamp.from(Instant.now());
      String swipeDirection = randomSwipe();
      body.setSwipee(String.valueOf(ThreadLocalRandom.current().nextInt(1, SWIPEE_MAX+1)));
      body.setSwiper(String.valueOf(ThreadLocalRandom.current().nextInt(1, SWIPER_MAX+1)));
      body.setComment(randomCommentGenerator());
      int retryCount = 5;
      while(retryCount > 0) {
        try {
          apiInstance.swipe(body, swipeDirection);
          Timestamp requestEndTime = Timestamp.from(Instant.now());
          queue.put(new CSVRecord(requestStartTime.toString(), requestEndTime.toString(),
              String.valueOf(requestEndTime.getTime() - requestStartTime.getTime()),
              String.valueOf(201), REQUEST_TYPE
              ));
//          globalQueue.add(new CSVRecord(requestStartTime.toString(), requestEndTime.toString(),
//              String.valueOf(requestEndTime.getTime() - requestStartTime.getTime()),
//              String.valueOf(201), REQUEST_TYPE
//              ));
          retryCount = 0;
          totalCount.getAndIncrement();
        } catch (ApiException e) {
          retryCount--;
          if(retryCount == 0) {
            failedCount.incrementAndGet();
            System.out.println("Failed");
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static String randomSwipe() {
    Random random = new Random();
    if(random.nextInt(2) == 0)
      return LEFT;
    return RIGHT;
  }

  public static String randomCommentGenerator() {
    StringBuilder builder = new StringBuilder();
    int totalLength = ThreadLocalRandom.current().nextInt(1, COMMENTS_MAX_CHAR_LIMIT+1);
    for(int i = 0 ; i < totalLength; i++) {
      builder.append((char)(ThreadLocalRandom.current().nextInt(1, LOWER_CASE_ALPHABET_CHAR_LIMIT+1) + 'a'));
    }
    return builder.toString();
  }
}
