/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.cli.viewmodel.types;

public class PyPiPackageIdentifier {
  private final String packageName;

  public PyPiPackageIdentifier(com.schibsted.security.artishock.pypi.PyPiPackageIdentifier pyPiPackageIdentifier) {
    this.packageName = pyPiPackageIdentifier.getPackageName();
  }

  public String getPackageName() {
    return packageName;
  }

  @Override
  public String toString() {
    return packageName;
  }
}
