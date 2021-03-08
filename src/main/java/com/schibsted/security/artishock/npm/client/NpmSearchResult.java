/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.npm.client;

import java.time.ZonedDateTime;

public class NpmSearchResult {
  private final String name;
  private final String scope;
  private final String description;
  private final ZonedDateTime date;

  public NpmSearchResult(SearchRaw.Package packageAlt) {
    this.name = packageAlt.name;
    this.scope = packageAlt.scope; // can be null
    this.description = packageAlt.description; // can be null
    if (packageAlt.date != null) {
      this.date = ZonedDateTime.parse(packageAlt.date);
    } else {
      this.date = null;
    }
  }

  public String getName() {
    return name;
  }

  public String getScope() {
    return scope;
  }

  public String getDescription() {
    return description;
  }

  public ZonedDateTime getDate() {
    return date;
  }
}
