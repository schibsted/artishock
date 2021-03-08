/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.cli.viewmodel.types;

import com.google.common.base.Joiner;
import com.schibsted.security.artishock.artifactory.Stats;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

public class ArtifactoryRepositoryStats {
  private final String name;
  private final long downloadCount;
  private final long archiveCount;
  private final Optional<String> lastDownloaded;
  private final Optional<String> lastDownloadedBy;
  private final Set<String> extensions;

  public ArtifactoryRepositoryStats(String name, Stats stats) {
    this.name = name;
    this.downloadCount = stats.getDownloadCount();
    this.archiveCount = stats.getArchiveCount();
    this.lastDownloaded = stats.getLastDownloaded().map(ZonedDateTime::toString);
    this.lastDownloadedBy = stats.getLastDownloadedBy();
    this.extensions = stats.getExtensions();
  }

  public String getName() {
    return name;
  }

  public long getDownloadCount() {
    return downloadCount;
  }

  public long getArchiveCount() {
    return archiveCount;
  }

  public Optional<String> getLastDownloaded() {
    return lastDownloaded;
  }

  public Optional<String> getLastDownloadedBy() {
    return lastDownloadedBy;
  }

  public Set<String> getExtensions() {
    return extensions;
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();

    sb.append(String.format("name: %s\n", name));
    sb.append(String.format("archives: %d\n", archiveCount));
    sb.append(String.format("archives types: %s\n", Joiner.on(",").join(extensions)));
    sb.append(String.format("downloads: %d\n", downloadCount));

    lastDownloaded.ifPresent(s -> sb.append(String.format("last downloaded: %s\n", s)));
    lastDownloadedBy.ifPresent(s -> sb.append(String.format("last downloaded by: %s\n", s)));

    return sb.toString();
  }
}
