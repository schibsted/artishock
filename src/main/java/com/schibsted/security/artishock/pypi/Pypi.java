/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.pypi;

import com.schibsted.security.artishock.artifactory.ArtifactoryClient;
import com.schibsted.security.artishock.config.Config;
import com.schibsted.security.artishock.pypi.client.PyPiClient;
import com.schibsted.security.artishock.shared.ConnectionInfo;
import com.schibsted.security.artishock.shared.FileReader;
import com.schibsted.security.artishock.shared.Intersection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Pypi {
  private final ArtifactoryClient artifactoryClient;
  private final PyPiClient pyPiClient;
  private final Config config;

  public Pypi(Config config, ArtifactoryClient artifactoryClient) {
    this.artifactoryClient = artifactoryClient;
    this.pyPiClient = new PyPiClient(config);
    this.config = config;
  }

  public List<PyPiPackageIdentifier> excludeCandidates(String local, Optional<String> trusted, Optional<String> excluded, int retries, long pauseSeconds) {
    var candidates = pyPiClient.getAllPyPiPackageIdentifierFromIndex(local, retries, pauseSeconds);

    var exclude = new ArrayList<PyPiPackageIdentifier>();
    trusted.ifPresent(s -> exclude.addAll(fromFile(s)));
    excluded.ifPresent(s -> exclude.addAll(fromFile(s)));

    candidates.removeAll(exclude);

    return candidates;
  }

  public List<PyPiPackageIdentifier> cached(String local, String remote, int retries, long pauseSeconds) {
    var localPackages = pyPiClient.getAllPyPiPackageIdentifierFromIndex(local, retries, pauseSeconds);
    var remoteCached = artifactoryClient.getAllPyPiPackageIdentifiersFromPyPiDir(remote + "-cache");

    return Intersection.cacheIntersection(localPackages, remoteCached);
  }

  public List<PyPiPackageIdentifier> inferredExclude(String local, String remote, int retries, long pauseSeconds) {
    var localPackages = pyPiClient.getAllPyPiPackageIdentifierFromIndex(local, retries, pauseSeconds);

    var upstreamPackages = checkUpstream(localPackages, retries, pauseSeconds);
    var remotePackages = checkLocal(remote, localPackages, retries, pauseSeconds);

    upstreamPackages.removeAll(remotePackages);

    return upstreamPackages;
  }

  public List<PyPiPackageIdentifier> notClaimed(String local, Optional<String> excluded, int retries, long pauseSeconds) {
    var localPackages = pyPiClient.getAllPyPiPackageIdentifierFromIndex(local, retries, pauseSeconds);

    var exclude = new ArrayList<PyPiPackageIdentifier>();
    excluded.ifPresent(s -> exclude.addAll(fromFile(s)));

    localPackages.removeAll(exclude);

    var upstreamPackages = checkUpstream(localPackages, retries, pauseSeconds);
    localPackages.removeAll(upstreamPackages);

    return localPackages;
  }

  List<PyPiPackageIdentifier> checkUpstream(List<PyPiPackageIdentifier> local, int retries, long pauseSeconds) {
    var result = new ArrayList<PyPiPackageIdentifier>();
    for (var l : local) {
      if (existsUpstream(l, retries, pauseSeconds)) {
        result.add(l);
      }
    }
    return result;
  }

  List<PyPiPackageIdentifier> checkLocal(String repoName, List<PyPiPackageIdentifier> local, int retries, long pauseSeconds) {
    var result = new ArrayList<PyPiPackageIdentifier>();
    for (var l : local) {
      if (existsInArtifactory(repoName, l, retries, pauseSeconds)) {
        result.add(l);
      }
    }
    return result;
  }

  boolean existsUpstream(PyPiPackageIdentifier packageIdentifier, int retries, long pauseSeconds) {
    return pyPiClient.packageExistsCached(pyPiClient.upstream(), packageIdentifier, retries, pauseSeconds);
  }

  boolean existsInArtifactory(String repo, PyPiPackageIdentifier packageIdentifier, int retries, long pauseSeconds) {
    return pyPiClient.packageExistsCached(new ConnectionInfo(config.getArtifactoryUrl() + "/api/pypi/" + repo,
            config.getArtifactoryUsername(),
            config.getArtifactoryPassword()),
        packageIdentifier, retries, pauseSeconds);
  }

  List<PyPiPackageIdentifier> fromFile(String location) {
    return FileReader.linesFromFile(location).stream()
        .map(PyPiPackageIdentifier::new)
        .collect(Collectors.toList());
  }
}
