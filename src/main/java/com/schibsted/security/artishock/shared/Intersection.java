/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.shared;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Intersection {
  private static final Logger log = LogManager.getLogger();

  public static <T> List<T> cacheIntersection(List<T> local, List<T> cached) {
    var localSet = new HashSet<>(local);
    var cachedSet = new HashSet<>(cached);

    var intersection = local.stream()
        .filter(cachedSet::contains)
        .collect(Collectors.toList());

    log.info("Without cache " + (localSet.size() - intersection.size()));
    log.info("With cache " + intersection.size());

    return intersection;
  }
}
