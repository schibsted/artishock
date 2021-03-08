/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.npm;

import java.util.Objects;
import java.util.Optional;

public class NpmPackageIdentifier {
  private final Optional<String> scope;
  private final String packageName;

  public NpmPackageIdentifier(String scope, String packageName) {
    this.scope = Optional.of(scope);
    this.packageName = packageName;
  }

  public NpmPackageIdentifier(String name) {
    var parts = name.split("/");
    if (parts.length == 2) {
      if (!parts[0].startsWith("@")) {
        throw new RuntimeException(String.format("Scope '%s' must start with '@'", parts[0]));
      }
      this.scope = Optional.of(parts[0].substring(1));
      this.packageName = parts[1];
    } else if (parts.length == 1){
      this.scope = Optional.empty();
      this.packageName = parts[0];
    } else {
      throw new RuntimeException(String.format("Expected at most one '/' to separate scope and package name in '%s'", name));
    }
  }

  public Optional<String> getScope() {
    return scope;
  }

  public String getPackageName() {
    return packageName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NpmPackageIdentifier that = (NpmPackageIdentifier) o;
    return scope.equals(that.scope) && packageName.equals(that.packageName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scope, packageName);
  }

  @Override
  public String toString() {
    return scope.map(s -> "@" + s + "/" + packageName).orElse(packageName);
  }
}
