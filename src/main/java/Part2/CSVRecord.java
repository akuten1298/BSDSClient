package Part2;

/**
 * @author aakash
 */
public class CSVRecord {

  String startTime;
  String endTime;
  String latency;
  String responseCode;
  String requestType;

  boolean poisonPill;


  public CSVRecord() {
  }

  public CSVRecord(String startTime, String endTime, String latency, String responseCode,
      String requestType) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.latency = latency;
    this.responseCode = responseCode;
    this.requestType = requestType;
    this.poisonPill = false;
  }

  public CSVRecord(boolean poisonPill) {
    this.poisonPill = poisonPill;
  }

  public String getStartTime() {
    return startTime;
  }

  public boolean isPoisonPill() {
    return poisonPill;
  }

  public String getEndTime() {
    return endTime;
  }

  public String getLatency() {
    return latency;
  }

  public String getResponseCode() {
    return responseCode;
  }

  public String getRequestType() {
    return requestType;
  }

  @Override
  public String toString() {
    return "Part2.CSVRecord{" +
        "startTime='" + startTime + '\'' +
        ", endTime='" + endTime + '\'' +
        ", latency='" + latency + '\'' +
        ", responseCode='" + responseCode + '\'' +
        ", requestType='" + requestType + '\'' +
        '}';
  }
}
