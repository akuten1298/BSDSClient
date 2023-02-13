/**
 * @author aakash
 */

import io.swagger.client.ApiException;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Random;

/**
 * @author aakash
 */
public class test {

  private static final String LEFT = "left";
  private static final String RIGHT = "right";
  private static final int SWIPEE_MAX = 100000;
  private static final int SWIPER_MAX = 5000;
  private static final int COMMENTS_MAX_CHAR_LIMIT = 256;
  private static final int LOWER_CASE_ALPHABET_CHAR_LIMIT = 26;

  private static final String EC2_SERVER_PATH = "http://54.70.221.86:8080/BSDSServer_war/";

  private static final String LOCAL_SERVER_PATH = "http://localhost:8081/BSDSServer_war_exploded/";

  public static void main(String[] args) {
    Timestamp startTime = Timestamp.from(Instant.now());
    System.out.println("Start Time: " + startTime);

    SwipeApi apiInstance = new SwipeApi();
    sendRequest(apiInstance);

    Timestamp endTime = Timestamp.from(Instant.now());
    System.out.println("End Time: " + endTime);

    long timeDiff = endTime.getTime() - startTime.getTime();
    System.out.println("Total time: " + timeDiff);
  }

  public static void sendRequest(SwipeApi apiInstance) {
    SwipeDetails body = new SwipeDetails();

    String swipeDirection = randomSwipe();
    apiInstance.getApiClient().setBasePath(LOCAL_SERVER_PATH);
    body.setSwipee(String.valueOf(randomNumber(SWIPEE_MAX)));
    body.setSwiper(String.valueOf(randomNumber(SWIPER_MAX)));
    body.setComment("yolo");
    try {
      apiInstance.swipe(body, swipeDirection);
    } catch (ApiException e) {
      e.printStackTrace();
    }
  }

  public static String randomSwipe() {
    Random random = new Random();
    if(random.nextInt(2) == 0)
      return LEFT;
    return RIGHT;
  }

  public static int randomNumber(int maxNumber) {
    Random random = new Random();
    return random.nextInt(maxNumber+1);
  }

  public static String randomCommentGenerator() {
    StringBuilder builder = new StringBuilder();
    Random random = new Random();
    int totalLength = random.nextInt(COMMENTS_MAX_CHAR_LIMIT + 1);
    for(int i = 0 ; i < totalLength; i++) {
      builder.append((char)(random.nextInt(LOWER_CASE_ALPHABET_CHAR_LIMIT) + 'a'));
    }
    return builder.toString();
  }
}
