/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.npm.client;

import com.schibsted.security.artishock.config.RateLimitRetryConfig;
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

  public List<NpmPackageIdentifier> getPackageList(List<NpmPackageIdentifier> packages, ConnectionInfo connectionInfo, RateLimitRetryConfig retryConfig) {
    log.info(() -> "Fetching select packages from " + connectionInfo.getPrefix());
    return checkUpstream(packages, connectionInfo, retryConfig);
  }

  public List<NpmPackageOrScope> notClaimedOrg(List<String> scopes, RateLimitRetryConfig retryConfig) {
    var result = new ArrayList<NpmPackageOrScope>();
    for (var scope : scopes) {
      if (!claimedOrgCached(scope, retryConfig)) {
        result.add(new NpmPackageOrScope(Optional.of(scope), Optional.empty()));
      }
    }

    return result;
  }

  boolean claimedOrgCached(String scope, RateLimitRetryConfig retryConfig) {
    var connectionInfo = new ConnectionInfo("https://www.npmjs.com");
    Supplier<String> f = () -> Boolean.toString(fetchOrgIsClaimed(connectionInfo, scope, retryConfig));
    var result = SimpleCache.getFromCacheOrExecute(connectionInfo, scope, CacheCategory.ORG, f);

    if (result.equals("true")) {
      return true;
    } else if (result.equals("false")) {
      return false;
    } else {
      throw new RuntimeException("Wrong code");
    }
  }

  List<NpmPackageIdentifier> checkUpstream(List<NpmPackageIdentifier> packages, ConnectionInfo connectionInfo, RateLimitRetryConfig retryConfig) {
    var result = new ArrayList<NpmPackageIdentifier>();
    for (var packageName : packages) {
      if (existsUpstream(packageName, connectionInfo, retryConfig)) {
        result.add(packageName);
      }
    }

    return result;
  }

  boolean existsUpstream(NpmPackageIdentifier packageName, ConnectionInfo connectionInfo, RateLimitRetryConfig retryConfig) {
      Supplier<String> f = () -> Boolean.toString(packageExists(packageName.toString(), connectionInfo, retryConfig));
      var result = SimpleCache.getFromCacheOrExecute(connectionInfo, packageName.toString(), CacheCategory.PACKAGE_EXISTS, f);

      if (result.equals("true")) {
        return true;
      } else if (result.equals("false")) {
        return false;
      } else {
        throw new RuntimeException(String.format("Must be 'true' or 'false' got '%s'", result));
      }
  }

  boolean fetchOrgIsClaimed(ConnectionInfo connectionInfo, String scope, RateLimitRetryConfig retryConfig) {
    return HttpClient.exists(connectionInfo, "/org/" + scope, retryConfig);
  }

  private boolean packageExists(String packageName, ConnectionInfo connectionInfo, RateLimitRetryConfig retryConfig) {
    return HttpClient.exists(connectionInfo, "/" + packageName, retryConfig);
  }

  public ConnectionInfo upstream() {
    return new ConnectionInfo("https://registry.npmjs.org");
  }
}
