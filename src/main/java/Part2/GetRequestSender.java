package Part2;

import io.swagger.client.api.SwipeApi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import io.swagger.client.ApiException;
import io.swagger.client.model.ResponseMsg;

public class GetRequestSender implements Runnable {

    private static final String matchURL = "http://54.187.62.84:8080/BSDSServer_war/matches/";
    private static final String statsURL = "http://54.187.62.84:8080/BSDSServer_war/stats/";
    private static final String REQUEST_TYPE = "GET";
    private List<Long> latencies;
    public GetRequestSender() {
        latencies = new ArrayList<>();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                for(int i = 0; i < 5; i++) {
                    generateRandomGetRequest();
                }
            } catch (InterruptedException e) {
                // Thread interrupted
                System.out.println("Thread interrupted");
                break;
            }
        }
    }

    public void generateRandomGetRequest() {
        Timestamp startTime;
        Timestamp endTime;

        URL obj = null;
        String url = null;
        if(randomRequest() == 0) {
            try {
                url = matchURL + generateRandomId();
                obj = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            try {
                url = statsURL + generateRandomId();
                obj = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        startTime = Timestamp.from(Instant.now());
        try {
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // Set the request method
            con.setRequestMethod("GET");

            // Get the response code
            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
//            System.out.println("Response code: 200");
            in.close();
        } catch (FileNotFoundException e) {
//            System.out.println("Response code: 404");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            endTime = Timestamp.from(Instant.now());
            long timeDiff = endTime.getTime() - startTime.getTime();
            latencies.add(timeDiff);
        }
    }

    public static int randomRequest() {
        Random random = new Random();
        if(random.nextInt(2) == 0)
            return 0;
        return 1;
    }

    public static int generateRandomId() {
        Random random = new Random();
        return random.nextInt(1, 50000);
    }

    public void calculateStats() {
        System.out.println("---------------------GET REQUEST STATS----------------");

        int totalNumberOfRequests = latencies.size();
        long totalTime = 0;
        for(long time : latencies)
            totalTime += time;

        System.out.println("Mean Latency: " + totalTime / totalNumberOfRequests + "ms");

        Collections.sort(latencies);

        System.out.println("Min Latency: " + latencies.get(0) + "ms");
        System.out.println("Max Latency: " + latencies.get(latencies.size() - 1) + "ms");
    }
}
