/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.npm.client;

public class NpmVersion implements Comparable<NpmVersion> {
  private final int major;
  private final int minor;
  private final int patch;

  public NpmVersion(int major, int minor, int patch) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
  }

  public int getMajor() {
    return major;
  }

  public int getMinor() {
    return minor;
  }

  public int getPatch() {
    return patch;
  }

  @Override
  public int compareTo(NpmVersion o) {
    if (this.major == o.getMajor()) {
      if (this.minor == o.getMinor()) {
        return Integer.compare(this.patch, o.getPatch());
      } else {
        return Integer.compare(this.minor, o.getMinor());
      }
    } else {
      return Integer.compare(this.major, o.getMajor());
    }
  }

  public String versionAsString() {
    return major + "." + minor + "." + patch;
  }
}
