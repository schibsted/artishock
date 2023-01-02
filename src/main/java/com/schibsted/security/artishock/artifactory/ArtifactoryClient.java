/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.artifactory;

import com.schibsted.security.artishock.npm.NpmPackageIdentifier;
import com.schibsted.security.artishock.config.Config;
import com.schibsted.security.artishock.pypi.PyPiPackageIdentifier;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClientBuilder;
import org.jfrog.artifactory.client.impl.ArtifactoryImpl;
import org.jfrog.artifactory.client.model.Folder;
import org.jfrog.artifactory.client.model.Item;
import org.jfrog.artifactory.client.model.impl.RemoteRepositoryImpl;
import org.jfrog.artifactory.client.model.impl.RepositoryTypeImpl;
import org.jfrog.artifactory.client.model.impl.VirtualRepositoryImpl;
import org.jfrog.artifactory.client.model.repository.settings.ComposerRepositorySettings;
import org.jfrog.artifactory.client.model.repository.settings.GradleRepositorySettings;
import org.jfrog.artifactory.client.model.repository.settings.MavenRepositorySettings;
import org.jfrog.artifactory.client.model.repository.settings.NpmRepositorySettings;
import org.jfrog.artifactory.client.model.repository.settings.NugetRepositorySettings;
import org.jfrog.artifactory.client.model.repository.settings.PypiRepositorySettings;
import org.jfrog.artifactory.client.model.repository.settings.RepositorySettings;
import org.jfrog.artifactory.client.model.repository.settings.SbtRepositorySettings;

/**
 * Fetch configs from internal Artifactory.
 */
public class ArtifactoryClient {
  private static final Logger log = LogManager.getLogger();

  private final Artifactory artifactory;

  /**
   * Resolve credentials and create ArtifactoryFetcher.
   */
  public ArtifactoryClient(Config config) {
    artifactory = ArtifactoryClientBuilder.create()
        .setUrl(config.getArtifactoryUrl())
        .setUsername(config.getArtifactoryUsername())
        .setPassword(config.getArtifactoryPassword())
        .build();
  }

  /**
   * This is useful for the remote repo, but for the local repo there is only one file: /.pypi/simple.html
   */
  public List<PyPiPackageIdentifier> getAllPyPiPackageIdentifiersFromPyPiDir(String repositoryName) {
    log.info(() -> "Downloading all package identifiers for " + repositoryName);
    var topLevelFolders = listSubFolders(repositoryName, "/.pypi/");

    var result = new ArrayList<PyPiPackageIdentifier>();
    for (var topLevelFolder : topLevelFolders) {
      var candidate = topLevelFolder.substring(1, topLevelFolder.length() - ".html".length());

      // TODO simple.html is the index, there might be other corner cases
      if (candidate.equals("simple")) {
        continue;
      }

      // TODO consider filtering out names that are not allowed upstream
      result.add(new PyPiPackageIdentifier(candidate));
    }
    return result;
  }

  public List<PyPiPackageIdentifier> getAllPyPiPackageIdentifiers(String repositoryName) {
    log.info(() -> "Downloading all package identifiers for " + repositoryName);
    var topLevelFolders = listTopLevelFolders(repositoryName);

    var result = new ArrayList<PyPiPackageIdentifier>();
    for (var topLevelFolder : topLevelFolders) {
      var candidate = topLevelFolder.substring(1);

      // TODO consider filtering out names that are not allowed upstream
      result.add(new PyPiPackageIdentifier(topLevelFolder.substring(1)));
    }
    return result;
  }

  public List<NpmPackageIdentifier> getAllNpmPackageIdentifiersForCache(String repositoryName) {
    return getAllNpmPackageIdentifiers(repositoryName, "/");
  }

  public List<NpmPackageIdentifier> getAllNpmPackageIdentifiersForLocal(String repositoryName) {
    return getAllNpmPackageIdentifiers(repositoryName, "/.npm/");
  }

  // TODO consider filtering out names that are not allowed upstream
  public List<NpmPackageIdentifier> getAllNpmPackageIdentifiers(String repositoryName, String path) {
    log.info(() -> "Downloading all package identifiers for " + repositoryName);
    var topLevelFolders = listSubFolders(repositoryName, path);

    var result = new ArrayList<NpmPackageIdentifier>();
    for (var topLevelFolder : topLevelFolders) {
      if (topLevelFolder.startsWith("/@")) {
        var subFolders = listSubFolders(repositoryName, topLevelFolder);
        var scope = topLevelFolder.substring(2);
        for (var packageName : subFolders) {
          result.add(new NpmPackageIdentifier(scope, packageName.substring(1)));
        }
      } else {
        if (topLevelFolder.startsWith("/.")) {
          continue;
        }
        result.add(new NpmPackageIdentifier(topLevelFolder.substring(1)));
      }
    }
    return result;
  }

  public List<Repository> repoLs() {
    log.info("Retrieving repos from Artifactory");
    var virtual = artifactory.repositories().list(RepositoryTypeImpl.VIRTUAL);
    var remote = artifactory.repositories().list(RepositoryTypeImpl.REMOTE);
    var local = artifactory.repositories().list(RepositoryTypeImpl.LOCAL);

    var repos = new HashMap<String, Repository>();
    var reposRaw = new HashMap<String, org.jfrog.artifactory.client.model.Repository>();

    for (var v : local) {
      var r = artifactory.repository(v.getKey()).get();
      var artifactType = artifactType(r.getRepositorySettings());
      repos.put(v.getKey(), new Repository(r.getKey(), "local", artifactType, null, new ArrayList<>()));
      reposRaw.put(v.getKey(), r);
    }


    for (var v : remote) {
      var r = (RemoteRepositoryImpl) artifactory.repository(v.getKey()).get();
      var artifactType = artifactType(r.getRepositorySettings());
      repos.put(v.getKey(), new Repository(r.getKey(), "remote", artifactType, r.getUrl(), new ArrayList<>()));
      reposRaw.put(v.getKey(), r);
    }

    for (var v : virtual) {
      var r = (VirtualRepositoryImpl) artifactory.repository(v.getKey()).get();
      var artifactType = artifactType(r.getRepositorySettings());

      repos.put(v.getKey(), new Repository(r.getKey(), "virtual", artifactType, null, new ArrayList<>()));
      reposRaw.put(v.getKey(), r);
    }

    for (var v : virtual) {
      var r = (VirtualRepositoryImpl) reposRaw.get(v.getKey());

      var repo = repos.get(v.getKey());
      for (var c : r.getRepositories()) {
        repo.addChild(repos.get(c));
      }
    }

    return repos.values().stream()
        .sorted(Comparator.comparingInt(a -> -a.getChildren().size()))
        .sorted(Comparator.comparingInt(a -> a.getParents().size()))
        .sorted(Comparator.comparing(Repository::getType).reversed())
        .collect(Collectors.toList());
  }

  // TODO support more
  public String artifactType(RepositorySettings repositorySettings) {
    if (repositorySettings instanceof PypiRepositorySettings) {
      return "pypi";
    } else if (repositorySettings instanceof GradleRepositorySettings) {
      return "gradle";
    } else if (repositorySettings instanceof NpmRepositorySettings) {
      return "npm";
    } else if (repositorySettings instanceof SbtRepositorySettings) {
      return "sbt";
    } else if (repositorySettings instanceof MavenRepositorySettings) {
      return "maven";
    } else if (repositorySettings instanceof NugetRepositorySettings) {
      return "nuget";
    } else if (repositorySettings instanceof ComposerRepositorySettings) {
      return "composer";
    } else {
      return "other";
    }
  }

  List<String> listTopLevelFolders(String repositoryName) {
    return listSubFolders(repositoryName, "/");
  }

  List<String> listSubFolders(String repositoryName, String folderName) {
    Folder folder = artifactory.repository(repositoryName).folder(folderName).info();

    return folder.getChildren().stream()
        .map(Item::getUri)
        .collect(Collectors.toList());
  }

  public Stats repoStats(String repoName, String packageSystem) {
    return recursiveStats(repoName, "/", archiveExtensions(packageSystem));
  }

  public Stats packageStats(String repoName, String packageSystem, NpmPackageIdentifier packageName) {
    return recursiveStats(repoName, packageName.toString(), archiveExtensions(packageSystem));
  }

  List<String> archiveExtensions(String packageSystem) {
    return switch (packageSystem.toLowerCase(Locale.ENGLISH)) {
      case "maven" -> List.of(".jar", ".war", ".rar", ".ear", ".sar", ".apk", ".aar", ".par", ".kar");
      case "npm" -> List.of(".tgz");
      case "pypi" -> List.of(".tar.gz");
      default -> throw new RuntimeException("Unknown package system " + packageSystem);
    };
  }

  public Stats recursiveStats(String repository, String path, List<String> archiveExtensions) {
    Folder folder = artifactory.repository(repository).folder(path).info();

    if (folder.getChildren() == null) {
      var extensionMatch = archiveExtensions.stream().filter(path::endsWith).findAny();
      if (extensionMatch.isPresent()) {
        //System.out.println(path);
        var stats = getDownloadStats(repository, path);
        Optional<ZonedDateTime> lastDownloaded = stats.getDownloadCount() > 0 ? Optional.of(stats.getLastDownloaded()) : Optional.empty();
        Optional<String> lastDownloadedBy = stats.getDownloadCount() > 0 ? Optional.of(stats.getLastDownloadedBy()) : Optional.empty();
        return new Stats(stats.getDownloadCount(), 1, lastDownloaded, lastDownloadedBy, Set.of(extensionMatch.get()));
      } else {
        return Stats.empty();
      }
    }

    var result = Stats.empty();
    for (var child : folder.getChildren()) {
      result = result.merge(recursiveStats(repository, path + child.getUri(), archiveExtensions));
    }

    return result;
  }

  /**
   * https://www.jfrog.com/confluence/display/JFROG/Artifactory+REST+API#ArtifactoryRESTAPI-FileStatistics
   */
  public DownloadStats getDownloadStats(String repository, String path) {
    var artifactoryImpl = (ArtifactoryImpl) artifactory;
    var url = "/api/storage/" + repository + "/" + path + "?stats";
    try {
      return new DownloadStats(artifactoryImpl.get(url, StatsInfoImpl.class, StatsInfo.class));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
