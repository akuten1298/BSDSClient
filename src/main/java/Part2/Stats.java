package Part2;

/**
 * @author aakash
 */
public class Stats {

  public long mean;
  public long median;
  public long minLatency;
  public long maxLatency;
  public long percentileLatency;

  public Stats() {
    minLatency = Long.MAX_VALUE;
    maxLatency = Long.MIN_VALUE;
  }

  public long getMean() {
    return mean;
  }

  public void setMean(long mean) {
    this.mean = mean;
  }

  public long getMedian() {
    return median;
  }

  public void setMedian(long median) {
    this.median = median;
  }

  public long getMinLatency() {
    return minLatency;
  }

  public void setMinLatency(long minLatency) {
    this.minLatency = minLatency;
  }

  public long getMaxLatency() {
    return maxLatency;
  }

  public void setMaxLatency(long maxLatency) {
    this.maxLatency = maxLatency;
  }

  public long getPercentileLatency() {
    return percentileLatency;
  }

  public void setPercentileLatency(long percentileLatency) {
    this.percentileLatency = percentileLatency;
  }
}
