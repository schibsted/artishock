/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.shared;

import com.schibsted.security.artishock.config.ConfigResolver;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

public class SimpleCache {
  public static boolean notInCache(String key, String repo, CacheCategory npmCacheCategory) {
    var cacheFile = cacheFile(key, repo, npmCacheCategory);

    return !cacheFile.exists() || olderThanOneWeek(cacheFile);
  }

  private static boolean olderThanOneWeek(File cacheFile) {
    if (cacheFile.exists()) {
      try {
        var lastModified = Files.getLastModifiedTime(cacheFile.toPath());
        var ageInDays = ChronoUnit.DAYS.between(lastModified.toInstant(), Instant.now());

        return ageInDays >= 7;
      } catch (IOException e) {
        throw new RuntimeException("Failed to find age of cache " + cacheFile);
      }
    }

    return false;
  }

  private static File cacheFile(String key, String repo, CacheCategory npmCacheCategory) {
    var urlEncodedKey = urlEncode(key);
    var urlEncodedRepo = urlEncode(repo);
    return ConfigResolver.configDirectory().resolve("cache/" + urlEncodedRepo + npmCacheCategory.getPath() + urlEncodedKey).toFile();
  }

  private static String urlEncode(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }

  public static String getFromCache(String key, String repo, CacheCategory npmCacheCategory) {
    var path = cacheFile(key, repo, npmCacheCategory).toPath();
    try {
      return Files.readString(path);
    } catch (IOException e) {
      throw new RuntimeException(String.format("Failed read cached data from '%s'", path), e);
    }
  }

  public static void putInCache(String key, String repo, CacheCategory npmCacheCategory, String content) {
    var configDirectory = ConfigResolver.configDirectory();
    var urlEncodedRepo = urlEncode(repo);
    createDirectoryIfNonexistent(configDirectory);
    createDirectoryIfNonexistent(configDirectory.resolve("cache/"));
    createDirectoryIfNonexistent(configDirectory.resolve("cache/" + urlEncodedRepo));
    createDirectoryIfNonexistent(configDirectory.resolve("cache/" + urlEncodedRepo + npmCacheCategory.getPath()));

    var path = cacheFile(key, repo, npmCacheCategory).toPath();
    try {
      Files.writeString(path, content);
    } catch (IOException e) {
      throw new RuntimeException(String.format("Failed to write cache to '%s'", path));
    }
  }

  public static void createDirectoryIfNonexistent(Path path) {
    var cacheDirectory = path.toFile();
    if (!cacheDirectory.exists()) {
      if (!cacheDirectory.mkdir()) {
        throw new RuntimeException(String.format("Failed to create '%s'", cacheDirectory));
      }
    }
  }

  public static String getFromCacheOrExecute(ConnectionInfo connectionInfo, String key, CacheCategory cacheCategory, Supplier<String> f) {
    var repo = connectionInfo.getPrefix();

    if (SimpleCache.notInCache(key, repo, cacheCategory)) {
      SimpleCache.putInCache(key, repo, cacheCategory, f.get());
    }
    return SimpleCache.getFromCache(key, repo, cacheCategory);
  }
}
