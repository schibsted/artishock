/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.shared;

public enum CacheCategory {
  PACKAGE_INFO("/package-info/"),
  SEARCH("/search/"),
  ORG("/org/"),
  PACKAGE_EXISTS("/package-exists/");

  private final String path;

  CacheCategory(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }
}
