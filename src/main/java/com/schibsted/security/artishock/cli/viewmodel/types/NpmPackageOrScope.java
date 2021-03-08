/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.cli.viewmodel.types;

import java.util.Optional;

public class NpmPackageOrScope {
  private final Optional<String> packageName;
  private final Optional<String> scope;
  private final String name;

  public NpmPackageOrScope(com.schibsted.security.artishock.npm.NpmPackageOrScope packageOrScope) {
    this.packageName = packageOrScope.getPackageName();
    this.scope = packageOrScope.getScope();
    this.name = packageOrScope.toString();
  }

  public Optional<String> getPackageName() {
    return packageName;
  }

  public Optional<String> getScope() {
    return scope;
  }

  @Override
  public String toString() {
    return name;
  }
}
