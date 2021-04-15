/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.config;

public class Config {
  private final String artifactoryUrl;
  private final String artifactoryUsername;
  private final String artifactoryPassword;

  public Config(String artifactoryUrl, String artifactoryUsername, String artifactoryPassword) {
    if (artifactoryUrl.endsWith("/")) {
      this.artifactoryUrl = artifactoryUrl.substring(0, artifactoryUrl.length() - 1);
    } else {
      this.artifactoryUrl = artifactoryUrl;
    }
    this.artifactoryUsername = artifactoryUsername;
    this.artifactoryPassword = artifactoryPassword;
  }

  public String getArtifactoryUrl() {
    return artifactoryUrl;
  }

  public String getArtifactoryUsername() {
    return artifactoryUsername;
  }

  public String getArtifactoryPassword() {
    return artifactoryPassword;
  }
}
