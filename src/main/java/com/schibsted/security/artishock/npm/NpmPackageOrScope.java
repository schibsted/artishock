/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.npm;

import java.util.Objects;
import java.util.Optional;

public class NpmPackageOrScope {
  private final Optional<String> packageName;
  private final Optional<String> scope;

  public NpmPackageOrScope(String name) {
    var parts = name.split("/");
    if (parts.length > 1) {
      if (!parts[0].startsWith("@")) {
        throw new RuntimeException(String.format("Scope '%s' must start with '@'", parts[0]));
      }
      this.scope = Optional.of(parts[0].substring(1));
      this.packageName = Optional.of(parts[1]);
    } else {
      if (parts[0].startsWith("@")) {
        this.scope = Optional.of(parts[0].substring(1));
        this.packageName = Optional.empty();
      } else {
        this.packageName = Optional.of(parts[0]);
        this.scope = Optional.empty();
      }
    }
  }

  public NpmPackageOrScope(Optional<String> scope, Optional<String> packageName) {
    this.scope = scope;
    this.packageName = packageName;
  }

  public NpmPackageOrScope(NpmPackageIdentifier npmPackageIdentifier) {
    this.scope = npmPackageIdentifier.getScope();
    this.packageName = Optional.of(npmPackageIdentifier.getPackageName());
  }

  public Optional<String> getPackageName() {
    return packageName;
  }

  public Optional<String> getScope() {
    return scope;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NpmPackageOrScope that = (NpmPackageOrScope) o;
    return Objects.equals(packageName, that.packageName) && Objects.equals(scope, that.scope);
  }

  @Override
  public int hashCode() {
    return Objects.hash(packageName, scope);
  }

  @Override
  public String toString() {
    if (packageName.isPresent()) {
      return scope.map(s -> "@" + s + "/" + packageName).orElse(packageName.get());
    } else if (scope.isPresent()) {
      return "@" + scope.get();
    } else {
      throw new IllegalStateException("Neither scope nor package name present");
    }
  }
}
