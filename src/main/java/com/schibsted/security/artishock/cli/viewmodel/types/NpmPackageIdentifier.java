/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.cli.viewmodel.types;

import java.util.Optional;

public class NpmPackageIdentifier {
  private final String packageName;
  private final Optional<String> scope;

  public NpmPackageIdentifier(com.schibsted.security.artishock.npm.NpmPackageIdentifier packageIdentifier) {
    this.packageName = packageIdentifier.getPackageName();
    this.scope = packageIdentifier.getScope();
  }

  public String getPackageName() {
    return packageName;
  }

  public Optional<String> getScope() {
    return scope;
  }

  @Override
  public String toString() {
    return scope.map(s -> "@" + s + "/" + packageName).orElse(packageName);
  }
}
