package Spring;

import io.swagger.client.ApiException;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * @author aakash
 */
public class SpringClient {

  private static final String LEFT = "left";
  private static final String RIGHT = "right";
  private static final int SWIPEE_MAX = 100000;
  private static final int SWIPER_MAX = 5000;
  private static final int COMMENTS_MAX_CHAR_LIMIT = 256;
  private static final int LOWER_CASE_ALPHABET_CHAR_LIMIT = 26;
  private static final String EC2_SERVER_PATH = "http://54.70.221.86:8080/BSDSServer_war/";

  private static final String LOCAL_SERVER_PATH = "http://localhost:8082/BSDSServer_war_exploded/";
  private static final String LOCAL_SPRING_SERVER = "http://localhost:8081/swipe/";
  private static final int NUM_THREADS = 100;

  private static final int TASKS_PER_THREAD = 5000;

  public static AtomicInteger failedCount;

  public static void main(String[] args) {

    Runnable runnableTask = SpringClient::sendRequest;
    failedCount = new AtomicInteger(0);

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
    System.out.println("Mean Latency: " + latency + " ms");

    System.out.println("Number of successful requests: " + (NUM_THREADS * TASKS_PER_THREAD - failedCount.get()));
    System.out.println("Number of unsuccessful requests: " + failedCount.get());
    System.out.println();
  }

  public static void sendRequest() {

    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    SwipeDetails body = new SwipeDetails();

    for(int i = 0; i < TASKS_PER_THREAD; i++) {
      String swipeDirection = randomSwipe();
      body.setSwipee(String.valueOf(ThreadLocalRandom.current().nextInt(1, SWIPEE_MAX+1)));
      body.setSwiper(String.valueOf(ThreadLocalRandom.current().nextInt(1, SWIPER_MAX+1)));
      body.setComment(randomCommentGenerator());

      HttpEntity<SwipeDetails> request = new HttpEntity<>(body, headers);

      String url = LOCAL_SPRING_SERVER + swipeDirection;
      ResponseEntity<String> response = null;
      try {
        response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
      if(response != null && !response.getStatusCode().is2xxSuccessful())
        failedCount.getAndIncrement();
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
