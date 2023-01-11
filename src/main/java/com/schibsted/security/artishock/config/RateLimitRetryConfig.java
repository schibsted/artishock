package com.schibsted.security.artishock.config;

public class RateLimitRetryConfig {
  private final int retries;
  private final long pauseSeconds;

  public RateLimitRetryConfig(int retries, long pauseSeconds) {
    this.retries = retries;
    this.pauseSeconds = pauseSeconds;
  }

  public int getRetries() {
    return retries;
  }

  public long getPauseSeconds() {
    return pauseSeconds;
  }
}
