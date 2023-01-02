/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.artifactory;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Stats {
  private final long downloadCount;
  private final long archiveCount;
  private final Optional<ZonedDateTime> lastDownloaded;
  private final Optional<String> lastDownloadedBy;
  private final Set<String> extensions;

  public Stats(long downloadCount, long archiveCount,
                Optional<ZonedDateTime> lastDownloaded,
                Optional<String> lastDownloadedBy,
                Set<String> extensions) {
    this.downloadCount = downloadCount;
    this.archiveCount = archiveCount;
    this.lastDownloaded = lastDownloaded;
    this.lastDownloadedBy = lastDownloadedBy;
    this.extensions = extensions;
  }

  public long getDownloadCount() {
    return downloadCount;
  }

  public long getArchiveCount() {
    return archiveCount;
  }

  public Optional<ZonedDateTime> getLastDownloaded() {
    return lastDownloaded;
  }

  public Optional<String> getLastDownloadedBy() {
    return lastDownloadedBy;
  }

  public Set<String> getExtensions() {
    return extensions;
  }

  public Stats merge(Stats other) {
    var extensions = Stream.of(this.extensions, other.getExtensions())
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());

    return new Stats(this.downloadCount + other.getDownloadCount(),
        this.archiveCount + other.getArchiveCount(),
        isAfter(this.lastDownloaded, other.getLastDownloaded()) ? this.lastDownloaded : other.getLastDownloaded(),
        isAfter(this.lastDownloaded, other.getLastDownloaded()) ? this.getLastDownloadedBy() : other.getLastDownloadedBy(),
        extensions);
  }

  public static Stats empty() {
    return new Stats(0, 0, Optional.empty(), Optional.empty(), Set.of());
  }

  private static boolean isAfter(Optional<ZonedDateTime> a, Optional<ZonedDateTime> b) {
    if (a.isEmpty() && b.isEmpty()) {
      return true;
    }

    if (a.isPresent() && b.isEmpty()) {
      return true;
    }

    if (b.isPresent() && a.isEmpty()) {
      return false;
    }

    return a.get().isAfter(b.get());
  }
}
