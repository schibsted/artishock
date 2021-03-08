/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.artifactory;

import java.util.ArrayList;
import java.util.List;

public class Repository {
  private final String name;
  private final String type;
  private final String artifactType;
  private final List<Repository> children;
  private final List<Repository> parents;
  private final String url;

  public Repository(String name, String type, String artifactType, String url, List<Repository> children) {
    this.name = name;
    this.type = type;
    this.artifactType = artifactType;
    this.children = children;
    this.parents = new ArrayList<>();
    this.url = url;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getArtifactType() {
    return artifactType;
  }

  public String getUrl() {
    return url;
  }

  public List<Repository> getChildren() {
    return children;
  }

  public List<Repository> getParents() {
    return parents;
  }

  public void addChild(Repository repository) {
    this.children.add(repository);
    repository.addParent(this);
  }

  public void addParent(Repository repository) {
    parents.add(repository);
  }
}
