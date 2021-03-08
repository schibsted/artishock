/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.npm.client;

import java.time.ZonedDateTime;

public class NpmRelease implements Comparable<NpmRelease> {
  private final String version;
  private final NpmVersion npmVersion;

  private final ZonedDateTime time;

  public NpmRelease(String version, ZonedDateTime time) {
    this.version = version;

    var dash = version.split("[-+]");
    if (dash.length > 1) {
      // System.out.println("version has - or +, ignoring");
    }

    var parts = dash[0].split("\\.");
    if (parts.length != 3) {
      throw new RuntimeException("Unexpected version " + version);
    }

    var major = Integer.parseInt(parts[0]);
    var minor = Integer.parseInt(parts[1]);
    var patch = Integer.parseInt(parts[2]);
    this.npmVersion = new NpmVersion(major, minor, patch);

    this.time = time;
  }

  public String getVersion() {
    return version;
  }

  public ZonedDateTime getTime() {
    return time;
  }

  public NpmVersion getNpmVersion() {
    return npmVersion;
  }

  @Override
  public int compareTo(NpmRelease o) {
    return this.npmVersion.compareTo(o.getNpmVersion());
  }
}

