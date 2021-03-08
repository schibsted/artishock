/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.shared;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum PackageSystem {
  NPM("npm"),
  MAVEN("maven"),
  GRADLE("gradle"),
  PYPI("pypi");

  private final String alias;

  private static final Map<String, PackageSystem> map = new HashMap<>();
  static {
    for (var v : values()) {
      map.put(v.getAlias(), v);
    }
  }

  PackageSystem(String alias) {
    this.alias = alias;
  }

  public String getAlias() {
    return alias;
  }

  public static Optional<PackageSystem> fromString(String alias) {
    return Optional.ofNullable(map.get(alias));
  }

  @Override
  public String toString() {
    return alias;
  }
}
