/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.cli.viewmodel.types;

import com.schibsted.security.artishock.artifactory.Repository;
import java.util.List;
import java.util.stream.Collectors;

public class ArtifactoryRepository {
  private final String name;
  private final String repositoryType;
  private final String packageSystem;
  private final List<ArtifactoryRepository> repositories;
  private final String upstreamUrl;

  public ArtifactoryRepository(Repository repository) {
    this.name = repository.getName();
    this.repositoryType = repository.getType();
    this.packageSystem = repository.getArtifactType();
    this.repositories = repository.getChildren().stream().map(ArtifactoryRepository::new).collect(Collectors.toList());
    this.upstreamUrl = repository.getUrl();
  }

  public String getName() {
    return name;
  }

  public String getRepositoryType() {
    return repositoryType;
  }

  public String getPackageSystem() {
    return packageSystem;
  }

  public String getUpstreamUrl() {
    return upstreamUrl;
  }

  public List<ArtifactoryRepository> getRepositories() {
    return repositories;
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    print(sb, this, 0);
    return sb.toString();
  }

  private void print(StringBuilder sb, ArtifactoryRepository repo, int indent) {
    var prefix = "-".repeat(indent);
    if (indent > 0) {
      prefix = prefix + " ";
    }
    if (repo.getUpstreamUrl() != null) {
      sb.append(String.format("%s%s [%s:%s:%s]", prefix, repo.getName(), repo.getPackageSystem(), repo.getRepositoryType(), repo.getUpstreamUrl()));
    } else {
      sb.append(String.format("%s%s [%s:%s]", prefix, repo.getName(), repo.getPackageSystem(), repo.getRepositoryType()));
    }
    for (var c : repo.getRepositories()) {
      sb.append("\n");
      print(sb, c, indent + 1);
    }
  }
}
