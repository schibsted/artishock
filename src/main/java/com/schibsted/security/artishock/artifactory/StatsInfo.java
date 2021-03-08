/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.artifactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public interface StatsInfo {
  long getDownloadCount();
  long getRemoteDownloadCount();
  long getLastDownloaded();
  long getRemoteLastDownloaded();
  String getLastDownloadedBy();
  String getUri();
}
