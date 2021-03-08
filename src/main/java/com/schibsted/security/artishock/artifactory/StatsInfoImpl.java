/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.artifactory;

public class StatsInfoImpl implements StatsInfo {
  private long downloadCount;
  private long remoteDownloadCount;
  private long lastDownloaded;
  private long remoteLastDownloaded;
  private String lastDownloadedBy;
  private String uri;

  @Override
  public long getDownloadCount() {
    return downloadCount;
  }

  @Override
  public long getRemoteDownloadCount() {
    return remoteDownloadCount;
  }

  @Override
  public long getLastDownloaded() {
    return lastDownloaded;
  }

  @Override
  public long getRemoteLastDownloaded() {
    return remoteLastDownloaded;
  }

  @Override
  public String getLastDownloadedBy() {
    return lastDownloadedBy;
  }

  @Override
  public String getUri() {
    return uri;
  }

  public void setDownloadCount(long downloadCount) {
    this.downloadCount = downloadCount;
  }

  public void setRemoteDownloadCount(long remoteDownloadCount) {
    this.remoteDownloadCount = remoteDownloadCount;
  }

  public void setLastDownloaded(long lastDownloaded) {
    this.lastDownloaded = lastDownloaded;
  }

  public void setRemoteLastDownloaded(long remoteLastDownloaded) {
    this.remoteLastDownloaded = remoteLastDownloaded;
  }

  public void setLastDownloadedBy(String lastDownloadedBy) {
    this.lastDownloadedBy = lastDownloadedBy;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }
}
