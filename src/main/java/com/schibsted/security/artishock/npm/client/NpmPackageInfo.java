/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.npm.client;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NpmPackageInfo {
  private final String name;
  private final List<NpmRelease> releases;
  private final ZonedDateTime created;
  private final ZonedDateTime modified;
  private final List<String> maintainers;

  public NpmPackageInfo(ViewRaw viewRaw) {
    if (viewRaw.errors != null) {
      throw new RuntimeException(String.format("code %d, message %s", viewRaw.errors.get(0).status, viewRaw.errors.get(0).message));
    }
    this.name = viewRaw.name;

    var releases = new ArrayList<NpmRelease>();
    for (var version : viewRaw.versions.keySet()) {
      var time = ZonedDateTime.parse(viewRaw.time.get(version));
      releases.add(new NpmRelease(version, time));
    }
    Collections.sort(releases);
    this.releases = releases;

    this.created = ZonedDateTime.parse(viewRaw.time.get("created"));
    this.modified = ZonedDateTime.parse(viewRaw.time.get("modified"));

    if (viewRaw.maintainers != null) {
      this.maintainers = viewRaw.maintainers.stream()
          .map(m -> m.name + " <" + m.email + ">")
          .collect(Collectors.toList());
    } else {
      this.maintainers = List.of();
    }
  }

  public String getName() {
    return name;
  }

  public List<NpmRelease> getReleases() {
    return releases;
  }

  public ZonedDateTime getCreated() {
    return created;
  }

  public ZonedDateTime getModified() {
    return modified;
  }

  public List<String> getMaintainers() {
    return maintainers;
  }

  public List<Integer> majorVersions() {
    return releases.stream()
        .map(r -> r.getNpmVersion().getMajor())
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  public NpmVersion highestVersion() {
    return releases.get(releases.size() - 1).getNpmVersion();
  }

  public Optional<NpmVersion> highestVersion(int majorVersion) {
    var candidates = releases.stream()
        .filter(r -> r.getNpmVersion().getMajor() == majorVersion)
        .sorted()
        .collect(Collectors.toList());

    if (candidates.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(candidates.get(candidates.size() - 1).getNpmVersion());
    }
  }
}
