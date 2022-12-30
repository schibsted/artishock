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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NpmClient {
  private static final Logger log = LogManager.getLogger();

  public NpmClient() {
  }

  public List<NpmPackageIdentifier> getPackageList(List<NpmPackageIdentifier> packages, ConnectionInfo connectionInfo) {
    log.info(() -> "Fetching select packages from " + connectionInfo.getPrefix());
    return checkUpstream(packages, connectionInfo);
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

  List<NpmPackageIdentifier> checkUpstream(List<NpmPackageIdentifier> packages, ConnectionInfo connectionInfo) {
    var result = new ArrayList<NpmPackageIdentifier>();
    for (var packageName : packages) {
      if (existsUpstream(packageName, connectionInfo)) {
        result.add(packageName);
      }
    }

    return result;
  }

  boolean existsUpstream(NpmPackageIdentifier packageName, ConnectionInfo connectionInfo) {
      Supplier<String> f = () -> Boolean.toString(packageExists(packageName.toString(), connectionInfo));
      var result = SimpleCache.getFromCacheOrExecute(connectionInfo, packageName.toString(), CacheCategory.PACKAGE_EXISTS, f);

      if (result.equals("true")) {
        return true;
      } else if (result.equals("false")) {
        return false;
      } else {
        throw new RuntimeException(String.format("Must be 'true' or 'false' got '%s'", result));
      }
  }

  boolean fetchOrgIsClaimed(ConnectionInfo connectionInfo, String scope) {
    return HttpClient.exists(connectionInfo, "/org/" + scope);
  }

  private boolean packageExists(String packageName, ConnectionInfo connectionInfo) {
    return HttpClient.exists(connectionInfo, "/" + packageName);
  }

  public ConnectionInfo upstream() {
    return new ConnectionInfo("https://registry.npmjs.org");
  }
}
