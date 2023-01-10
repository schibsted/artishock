/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.pypi.client;

import com.schibsted.security.artishock.config.Config;
import com.schibsted.security.artishock.pypi.PyPiPackageIdentifier;
import com.schibsted.security.artishock.shared.CacheCategory;
import com.schibsted.security.artishock.shared.ConnectionInfo;
import com.schibsted.security.artishock.shared.HttpClient;
import com.schibsted.security.artishock.shared.SimpleCache;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PyPiClient {
  private static final Logger log = LogManager.getLogger();

  private final Config config;

  public PyPiClient(Config config) {
    this.config = config;
  }

  public ConnectionInfo upstream() {
    return new ConnectionInfo("https://pypi.org");
  }

  public List<PyPiPackageIdentifier> getAllPyPiPackageIdentifierFromIndex(String repositoryName, int retries, long pauseSeconds) {
    log.info(() -> "Fetching PyPi package from index in " + repositoryName);

    var raw = getPackagesFromIndex(new ConnectionInfo(config.getArtifactoryUrl() + "/api/pypi/" + repositoryName,
        config.getArtifactoryUsername(), config.getArtifactoryPassword()), "/simple/", retries, pauseSeconds);

    var preprocessed = raw.replaceAll("\".*\"", "\"\"")
        .replace("\n", "")
        .replaceAll("<br/>", "")
        .replaceAll("<br />", "")
        .replaceAll("<head>.*</head>", "");

    var xmlMapper = new XmlMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try
    {
      var r = xmlMapper.readValue(preprocessed, SimpleRaw.class);

      // TODO consider filtering out names that are not allowed upstream
      return r.body.stream()
          .map(PyPiPackageIdentifier::new)
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public boolean packageExistsCached(ConnectionInfo connectionInfo, PyPiPackageIdentifier packageIdentifier, int retries, long pauseSeconds) {
    Supplier<String> f = () -> Boolean.toString(packageExists(connectionInfo, packageIdentifier, retries, pauseSeconds));
    var result = SimpleCache.getFromCacheOrExecute(connectionInfo, packageIdentifier.toString(), CacheCategory.PACKAGE_EXISTS, f);

    if (result.equals("true")) {
      return true;
    } else if (result.equals("false")) {
      return false;
    } else {
      throw new RuntimeException(String.format("Must be 'true' or 'false' got '%s'", result));
    }
  }

  public boolean packageExists(ConnectionInfo connectionInfo, PyPiPackageIdentifier packageName, int retries, long pauseSeconds) {
    return HttpClient.exists(connectionInfo, "/simple/" + packageName.getPackageName() + "/", retries, pauseSeconds);
  }

  String getPackagesFromIndex(ConnectionInfo connectionInfo, String path, int retries, long pauseSeconds) {
    return HttpClient.fetch(connectionInfo, path, retries, pauseSeconds);
  }
}
