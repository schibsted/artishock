/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.shared;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileReader {
  public static List<String> linesFromFile(String location) {
    var path = Path.of(location);
    try {
      return Files.readAllLines(path);
    } catch (IOException e) {
      throw new RuntimeException(String.format("Failed to read file at '%s'", path), e);
    }
  }
}
