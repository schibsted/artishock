/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.artifactory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class DownloadStats {
  private final String uri;
  private final long downloadCount;
  private final ZonedDateTime lastDownloaded;
  private final String lastDownloadedBy;
  private final long remoteDownloadCount;
  private final ZonedDateTime remoteLastDownloaded;

  public DownloadStats(StatsInfo statsInfo) {
    this.uri = statsInfo.getUri();
    this.downloadCount = statsInfo.getDownloadCount();
    this.lastDownloaded = ZonedDateTime.ofInstant(Instant.ofEpochMilli(statsInfo.getLastDownloaded()), ZoneOffset.UTC);
    this.lastDownloadedBy = statsInfo.getLastDownloadedBy();
    this.remoteDownloadCount = statsInfo.getRemoteDownloadCount();
    this.remoteLastDownloaded = ZonedDateTime.ofInstant(Instant.ofEpochMilli(statsInfo.getRemoteLastDownloaded()), ZoneOffset.UTC);
  }

  public String getUri() {
    return uri;
  }

  public long getDownloadCount() {
    return downloadCount;
  }

  public ZonedDateTime getLastDownloaded() {
    return lastDownloaded;
  }

  public String getLastDownloadedBy() {
    return lastDownloadedBy;
  }

  public long getRemoteDownloadCount() {
    return remoteDownloadCount;
  }

  public ZonedDateTime getRemoteLastDownloaded() {
    return remoteLastDownloaded;
  }
}
