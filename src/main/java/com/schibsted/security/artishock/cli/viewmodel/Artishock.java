/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.cli.viewmodel;

import com.google.common.base.Joiner;
import com.schibsted.security.artishock.cli.viewmodel.types.PyPiPackageIdentifier;
import com.schibsted.security.artishock.config.RateLimitRetryConfig;
import com.schibsted.security.artishock.npm.Npm;
import com.schibsted.security.artishock.artifactory.ArtifactoryClient;
import com.schibsted.security.artishock.cli.viewmodel.types.ArtifactoryRepository;
import com.schibsted.security.artishock.cli.viewmodel.types.ArtifactoryRepositoryStats;
import com.schibsted.security.artishock.cli.viewmodel.types.NpmPackageIdentifier;
import com.schibsted.security.artishock.cli.viewmodel.types.NpmPackageOrScope;
import com.schibsted.security.artishock.config.ConfigResolver;
import com.schibsted.security.artishock.pypi.Pypi;
import com.schibsted.security.artishock.shared.PackageSystem;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Artishock {
  private final Npm npm;
  private final Pypi pypi;
  private final ArtifactoryClient artifactoryClient;

  public Artishock() {
    var config = ConfigResolver.resolveConfig();
    this.artifactoryClient = new ArtifactoryClient(config);
    this.npm = new Npm(config, artifactoryClient);
    this.pypi = new Pypi(config, artifactoryClient);
  }

  public List<Object> repoLs(String packageSystem) {
    throwIfNotSupportedOptional(packageSystem, List.of(PackageSystem.MAVEN, PackageSystem.NPM, PackageSystem.PYPI));

    return artifactoryClient.repoLs().stream()
        .filter(f -> packageSystem == null || f.getArtifactType().equals(packageSystem))
        .map(ArtifactoryRepository::new)
        .collect(Collectors.toList());
  }

  public Object repoStats(String repoName, String packageSystem) {
    getPackageSystemOrThrow(packageSystem, List.of(PackageSystem.MAVEN, PackageSystem.NPM, PackageSystem.PYPI));

    return new ArtifactoryRepositoryStats(repoName, artifactoryClient.repoStats(repoName, packageSystem));
  }

  public Object packageStats(String repoName, String packageSystem, String packageName) {
    getPackageSystemOrThrow(packageSystem, List.of(PackageSystem.NPM));

    var identifier = new com.schibsted.security.artishock.npm.NpmPackageIdentifier(packageName);
    return new ArtifactoryRepositoryStats(packageName, artifactoryClient.packageStats(repoName, packageSystem, identifier));
  }

  public List<Object> excludeCandidates(String packageSystem, String local, String trusted, String excluded, RateLimitRetryConfig retryConfig) {
    var system = getPackageSystemOrThrow(packageSystem, List.of(PackageSystem.NPM, PackageSystem.PYPI));

    switch (system) {
      case NPM -> {
        return npm.excludeCandidates(local, Optional.ofNullable(trusted), Optional.ofNullable(excluded)).stream()
            .map(NpmPackageIdentifier::new)
            .collect(Collectors.toList());
      }
      case PYPI -> {
        return pypi.excludeCandidates(local, Optional.ofNullable(trusted), Optional.ofNullable(excluded), retryConfig).stream()
            .map(PyPiPackageIdentifier::new)
            .collect(Collectors.toList());
      }
      default -> throw new RuntimeException("Implementation bug");
    }
  }

  public List<Object> cached(String packageSystem, String local, String remote, RateLimitRetryConfig retryConfig) {
    var system = getPackageSystemOrThrow(packageSystem, List.of(PackageSystem.NPM, PackageSystem.PYPI));

    switch (system) {
      case NPM -> {
        return npm.cached(local, remote).stream()
            .map(NpmPackageIdentifier::new)
            .collect(Collectors.toList());
      }
      case PYPI -> {
        return pypi.cached(local, remote, retryConfig).stream()
            .map(PyPiPackageIdentifier::new)
            .collect(Collectors.toList());
      }
      default -> throw new RuntimeException("Implementation bug");
    }
  }

  public List<Object> inferredExclude(String packageSystem, String local, String remote, boolean enableUpstream, RateLimitRetryConfig retryConfig) {
    verifyEnableUpstreamOrThrow(enableUpstream);

    var system = getPackageSystemOrThrow(packageSystem, List.of(PackageSystem.NPM, PackageSystem.PYPI));

    switch (system) {
      case NPM -> {
        return npm.inferredExclude(local, remote, retryConfig).stream()
            .map(NpmPackageIdentifier::new)
            .collect(Collectors.toList());
      }
      case PYPI -> {
        return pypi.inferredExclude(local, remote, retryConfig).stream()
            .map(PyPiPackageIdentifier::new)
            .collect(Collectors.toList());
      }
      default -> throw new RuntimeException("Implementation bug");
    }
  }

  public List<Object> notClaimed(String packageSystem, String local, String excluded, boolean enableUpstream, RateLimitRetryConfig retryConfig) {
    verifyEnableUpstreamOrThrow(enableUpstream);

    var system = getPackageSystemOrThrow(packageSystem, List.of(PackageSystem.NPM, PackageSystem.PYPI));

    switch (system) {
      case NPM -> {
        return npm.notClaimed(local, Optional.ofNullable(excluded), retryConfig).stream()
            .map(NpmPackageOrScope::new)
            .collect(Collectors.toList());
      }
      case PYPI -> {
        return pypi.notClaimed(local, Optional.ofNullable(excluded), retryConfig).stream()
            .map(PyPiPackageIdentifier::new)
            .collect(Collectors.toList());
      }
      default -> throw new RuntimeException("Implementation bug");
    }
  }

  void throwIfNotSupportedOptional(String packageSystem, List<PackageSystem> supported) {
    if (packageSystem != null) {
      getPackageSystemOrThrow(packageSystem, supported);
    }
  }

  PackageSystem getPackageSystemOrThrow(String packageSystem, List<PackageSystem> supported) {
    var type = PackageSystem.fromString(packageSystem);
    if (type.isEmpty() || !supported.contains(type.get())) {
      throw new RuntimeException(String.format("Unsupported package system '%s', try one of {%s}",
          packageSystem,
          Joiner.on(", ").join(supported)));
    }

    return type.get();
  }

  void verifyEnableUpstreamOrThrow(boolean enableUpstream) {
    if (!enableUpstream) {
      throw new RuntimeException("This command only works by querying internal packages upstream, add `--query-upstream` if this is OK or don't use this command");
    }
  }
}
