/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.pypi;

import java.util.Objects;

public class PyPiPackageIdentifier {
  private final String packageName;

  public PyPiPackageIdentifier(String packageName) {
    this.packageName = packageName;
  }

  public String getPackageName() {
    return packageName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PyPiPackageIdentifier that = (PyPiPackageIdentifier) o;
    return Objects.equals(packageName, that.packageName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(packageName);
  }

  @Override
  public String toString() {
    return packageName;
  }
}
