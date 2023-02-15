package Part2;

import io.swagger.client.ApiException;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
  private static final int TASKS_PER_THREAD = 5000;

  private static final int PERCENTILE = 99;

  private static final int NUM_CONSUMER_THREADS = 1;

  private static final String REQUEST_TYPE = "POST";
  public static AtomicInteger failedCount;
  public static AtomicInteger totalCount;

  private static BlockingQueue<CSVRecord> queue = new ArrayBlockingQueue<>(NUM_THREADS * TASKS_PER_THREAD + 100);

  private static List<CSVRecord> csvRecords = new ArrayList<>();

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
    System.out.println("Total Start Time: " + startTime);

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

    Timestamp endTime = Timestamp.from(Instant.now());
    System.out.println("Total End Time: " + endTime);

    try {
      queue.put(new CSVRecord(true));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    System.out.println("Total produced records: " + totalCount.get());

    Stats stats = new Stats();
    calculateStats(stats);

    long timeDiff = endTime.getTime() - startTime.getTime();
    long totalTimeInSeconds = timeDiff/1000;
    if(totalTimeInSeconds == 0) {
      totalTimeInSeconds = 1;
    }

    long throughput = (NUM_THREADS * TASKS_PER_THREAD)/totalTimeInSeconds;

    System.out.println("Total time: " + timeDiff + " ms");
    System.out.println("Total time in seconds: " + totalTimeInSeconds);

    System.out.println("Number of successful requests: " + (NUM_THREADS * TASKS_PER_THREAD - failedCount.get()));
    System.out.println("Number of unsuccessful requests: " + failedCount.get());

    System.out.println("Throughput: " + throughput + " requests/second");
    System.out.println("Minimum Latency: " + stats.getMinLatency() + "ms");
    System.out.println("Maximum Latency: " + stats.getMaxLatency() + "ms");
    System.out.println("Mean Latency: " + stats.getMean() + "ms");
    System.out.println("Median Latency: " + stats.getMedian() + "ms");
    System.out.println("99th Percentile Latency: " + stats.getPercentileLatency() + "ms");

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

  }

  public static void calculateStats(Stats stats) {
    List<Long> latencies = new ArrayList<>();
    long total = 0;
    for (CSVRecord csvRecord : csvRecords) {
      long latency = Long.parseLong(csvRecord.getLatency());
      latencies.add(latency);
      total += latency;
      stats.setMinLatency(Math.min(latency, stats.getMinLatency()));
      stats.setMaxLatency(Math.max(latency, stats.getMaxLatency()));
    }
    Collections.sort(latencies);

    stats.setMean(total / csvRecords.size());

    if (latencies.size() % 2 == 0) {
      stats.setMedian((latencies.get(latencies.size()/2) + latencies.get(latencies.size()/2 - 1))/2);
    } else {
      stats.setMedian(latencies.get(latencies.size()/2));
    }

    int index = (int) (PERCENTILE / 100.0 * latencies.size());
    stats.setPercentileLatency(latencies.get(index));
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
          CSVRecord record = new CSVRecord(requestStartTime.toString(), requestEndTime.toString(),
              String.valueOf(requestEndTime.getTime() - requestStartTime.getTime()),
              String.valueOf(201), REQUEST_TYPE
          );
          queue.put(record);
          csvRecords.add(record);
//          globalQueue.add(new Part2.CSVRecord(requestStartTime.toString(), requestEndTime.toString(),
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
