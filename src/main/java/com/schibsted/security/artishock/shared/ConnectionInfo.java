/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.shared;

import java.util.Optional;

public class ConnectionInfo {
  private final String prefix;
  private final Optional<String> username;
  private final Optional<String> password;

  public ConnectionInfo(String prefix, String username, String password) {
    this.prefix = prefix;
    this.username = Optional.of(username);
    this.password = Optional.of(password);
  }


  public ConnectionInfo(String prefix) {
    this.prefix = prefix;
    this.username = Optional.empty();
    this.password = Optional.empty();
  }

  public String getPrefix() {
    return prefix;
  }

  public Optional<String> getUsername() {
    return username;
  }

  public Optional<String> getPassword() {
    return password;
  }
}
