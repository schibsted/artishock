/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigResolver {
  private static final Logger log = LogManager.getLogger();

  public static Config resolveConfig() {
    var file = configDirectory().resolve("artishock.config").toFile();

    var mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {
      var config = new ConfigRaw();

      if (file.exists()) {
        log.info(() -> String.format("Reading config file %s", file.toPath()));
        var content = Files.readString(file.toPath());
        config = mapper.readValue(content, ConfigRaw.class);
      } else {
        log.info(() -> String.format("Could not read config file %s", file.toPath()));
      }

      var artifactoryUrl = getValue(config.artifactoryUrl, "ARTISHOCK_ARTIFACTORY_URL", "artifactory url");
      var artifactoryUser = getValue(config.artifactoryUsername, "ARTISHOCK_ARTIFACTORY_USERNAME", "artifactory user");
      var artifactoryPassword = getValue(config.artifactoryPassword, "ARTISHOCK_ARTIFACTORY_PASSWORD", "artifactory api key");

      return new Config(artifactoryUrl, artifactoryUser, artifactoryPassword);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Could deserialize config");
    } catch (IOException e) {
      throw new RuntimeException("Could not read config from");
    }
  }

  public static Path configDirectory() {
    return Path.of(userHome() + "/.artishock/");
  }

  static String userHome() {
    return System.getProperty("user.home");
  }

  static String getValue(String configFileValue, String env, String description) {
    Optional<String> envOverride = Optional.ofNullable(System.getenv(env));

    if (envOverride.isEmpty() && configFileValue == null) {
      throw new RuntimeException("Unable to get config " + description);
    }

    if (envOverride.isPresent()) {
      log.info(() -> String.format("Using %s from environment variable", description));
      return envOverride.get();
    } else {
      log.info(() -> String.format("Using %s from config file", description));
      return configFileValue;
    }
  }
}
