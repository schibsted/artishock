/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.npm.client;

import com.schibsted.security.artishock.shared.CacheCategory;
import com.schibsted.security.artishock.shared.ConnectionInfo;
import com.schibsted.security.artishock.npm.NpmPackageIdentifier;
import com.schibsted.security.artishock.npm.NpmPackageOrScope;
import com.schibsted.security.artishock.shared.HttpClient;
import com.schibsted.security.artishock.shared.SimpleCache;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NpmClient {
  private static final Logger log = LogManager.getLogger();

  private final ObjectMapper mapper;

  public NpmClient() {
    mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public List<NpmPackageIdentifier> getPackageList(List<NpmPackageIdentifier> packages, ConnectionInfo connectionInfo) {
    log.info(() -> "Fetching select packages from " + connectionInfo.getPrefix());
    return new ArrayList<>(getNpmPackages(packages, connectionInfo).keySet());
  }

  public List<NpmPackageOrScope> notClaimedOrg(List<String> scopes) {
    var result = new ArrayList<NpmPackageOrScope>();
    for (var scope : scopes) {
      if (!claimedOrgCached(scope)) {
        result.add(new NpmPackageOrScope(Optional.of(scope), Optional.empty()));
      }
    }

    return result;
  }

  boolean claimedOrgCached(String scope) {
    var connectionInfo = new ConnectionInfo("https://www.npmjs.com");
    Supplier<String> f = () -> Boolean.toString(fetchOrgIsClaimed(connectionInfo, scope));
    var result = SimpleCache.getFromCacheOrExecute(connectionInfo, scope, CacheCategory.ORG, f);

    if (result.equals("true")) {
      return true;
    } else if (result.equals("false")) {
      return false;
    } else {
      throw new RuntimeException("Wrong code");
    }
  }

  Map<NpmPackageIdentifier, NpmPackageInfo> getNpmPackages(List<NpmPackageIdentifier> packages, ConnectionInfo connectionInfo) {
    var result = new HashMap<NpmPackageIdentifier, NpmPackageInfo>();
    for (var packageName : packages) {
      var npmjs = getPackageInfo(packageName, connectionInfo);
      var p = convert(npmjs);
      p.ifPresent(npmPackageInfo -> result.put(packageName, npmPackageInfo));
    }

    return result;
  }

  Optional<NpmPackageInfo> convert(ViewRaw viewRaw) {
    if (viewRaw.error == null && viewRaw.errors == null) {
      return Optional.of(new NpmPackageInfo(viewRaw));
    } else {
      if (viewRaw.errors != null && viewRaw.errors.get(0).status == 404) {
        return Optional.empty();
      } else if (viewRaw.error != null && (viewRaw.error.equals("Not found") || viewRaw.error.equals("not_found"))) {
        return Optional.empty();
      }

      if (viewRaw.errors != null) {
        throw new RuntimeException(String.format("Got error %s", viewRaw.errors.get(0).message));
      } else {
        throw new RuntimeException(String.format("Got error %s", viewRaw.error));
      }
    }
  }

  ViewRaw getPackageInfo(NpmPackageIdentifier packageName, ConnectionInfo connectionInfo) {
    try {
      Supplier<String> f = () -> fetchPackageInfo(packageName.toString(), connectionInfo);
      var result = SimpleCache.getFromCacheOrExecute(connectionInfo, packageName.toString(), CacheCategory.PACKAGE_INFO, f);

      return mapper.readValue(result, ViewRaw.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  boolean fetchOrgIsClaimed(ConnectionInfo connectionInfo, String scope) {
    return HttpClient.exists(connectionInfo, "/org/" + scope);
  }

  private String fetchPackageInfo(String packageName, ConnectionInfo connectionInfo) {
    return HttpClient.fetch(connectionInfo, "/" + packageName);
  }

  public ConnectionInfo upstream() {
    return new ConnectionInfo("https://registry.npmjs.org");
  }
}
