/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.npm;

import com.schibsted.security.artishock.artifactory.ArtifactoryClient;
import com.schibsted.security.artishock.config.Config;
import com.schibsted.security.artishock.npm.client.NpmClient;
import com.schibsted.security.artishock.shared.ConnectionInfo;
import com.schibsted.security.artishock.shared.FileReader;
import com.schibsted.security.artishock.shared.Intersection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Npm {
  private final ArtifactoryClient artifactoryClient;
  private final Config config;

  private final NpmClient npmClient;

  public Npm(Config config, ArtifactoryClient artifactoryClient) {
    this.config = config;
    this.artifactoryClient = artifactoryClient;
    this.npmClient = new NpmClient();
  }

  public List<NpmPackageIdentifier> excludeCandidates(String local, Optional<String> trusted, Optional<String> excluded) {
    var candidates = artifactoryClient.getAllNpmPackageIdentifiersForLocal(local);

    if (trusted.isPresent()) {
      candidates = filterOutPackageOrScope(candidates, packageOrScopes(trusted.get()));
    }

    if (excluded.isPresent()) {
      candidates = filterOutPackageOrScope(candidates, packageOrScopes(excluded.get()));
    }

    return candidates;
  }

  public List<NpmPackageIdentifier> cached(String local, String remote) {
    var localPackages = artifactoryClient.getAllNpmPackageIdentifiersForLocal(local);

    var remoteCached = artifactoryClient.getAllNpmPackageIdentifiersForCache(remote + "-cache");
    return Intersection.cacheIntersection(localPackages, remoteCached);
  }

  public List<NpmPackageIdentifier> inferredExclude(String local, String remote) {
    var localPackages = artifactoryClient.getAllNpmPackageIdentifiersForLocal(local);

    // TODO search for scoped packages? This will leak additional names upstream
    var localPackagesWithoutScope = packagesWithoutScope(localPackages);

    var upstreamPackages = npmClient.getPackageList(localPackagesWithoutScope, npmClient.upstream());

    var remotePackages = npmClient.getPackageList(localPackagesWithoutScope,
        new ConnectionInfo(npmApi(config.getArtifactoryUrl(), remote), config.getArtifactoryUsername(), config.getArtifactoryPassword()));

    upstreamPackages.removeAll(remotePackages);
    upstreamPackages.sort(Comparator.comparing(NpmPackageIdentifier::toString));
    return upstreamPackages;
  }

  public List<NpmPackageOrScope> notClaimed(String local) {
    var localPackages = new ArrayList<>(artifactoryClient.getAllNpmPackageIdentifiersForLocal(local));

    var localPackagesWithoutScope = packagesWithoutScope(localPackages);

    var localScopes = localPackages.stream()
        .flatMap(f -> f.getScope().stream())
        .distinct()
        .collect(Collectors.toList());

    var upstreamPackages = npmClient.getPackageList(localPackagesWithoutScope, npmClient.upstream());

    localPackagesWithoutScope.removeAll(upstreamPackages);

    var result = localPackagesWithoutScope.stream()
        .map(NpmPackageOrScope::new)
        .collect(Collectors.toList());

    var notClaimedScopes = npmClient.notClaimedOrg(localScopes);

    result.addAll(notClaimedScopes);

    return result;
  }

  String npmApi(String artifactoryBase, String repositoryName) {
    return artifactoryBase + "/api/npm/" + repositoryName;
  }

  List<NpmPackageIdentifier> filterOutPackageOrScope(List<NpmPackageIdentifier> all, List<NpmPackageOrScope> exclude) {
    var excludeScopes = exclude.stream()
        .filter(f -> f.getScope().isPresent() && f.getPackageName().isEmpty())
        .flatMap(f -> f.getScope().stream())
        .collect(Collectors.toSet());
    var excludePackages = exclude.stream()
        .filter(f -> f.getScope().isEmpty())
        .flatMap(f -> f.getPackageName().stream())
        .collect(Collectors.toSet());
    var excludePackageAndScope = exclude.stream()
        .filter(f -> f.getScope().isPresent() && f.getPackageName().isPresent())
        .collect(Collectors.toSet());

    return all.stream()
        .filter(p -> p.getScope().isEmpty() || !excludeScopes.contains(p.getScope().get()))
        .filter(p -> p.getScope().isPresent() || !excludePackages.contains(p.getPackageName()))
        .filter(p -> p.getScope().isEmpty() || !excludePackageAndScope.contains(new NpmPackageOrScope(p)))
        .collect(Collectors.toList());
  }

  List<NpmPackageOrScope> packageOrScopes(String location) {
    return FileReader.linesFromFile(location).stream()
        .map(NpmPackageOrScope::new)
        .collect(Collectors.toList());
  }

  static List<NpmPackageIdentifier> packagesWithoutScope(List<NpmPackageIdentifier> packages) {
    return packages.stream()
        .filter(p -> p.getScope().isEmpty())
        .collect(Collectors.toList());
  }
}
