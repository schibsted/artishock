/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.cli.view;

import com.google.common.base.Joiner;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class Renderer {
  private final OutputFormat outputFormat;
  private final PrintStream outputStream;

  public Renderer(OutputFormat outputFormat, PrintStream outputStream) {
    this.outputStream = outputStream;

    this.outputFormat = outputFormat;
    switch (outputFormat) {
      case JSON:
      case TEXT:
        break;
      default:
        throw new IllegalArgumentException(String.format("Unexpected output format '%s', reverting to text", outputFormat.toString()));
    }
  }

  public <T extends Collection<?>> void render(T object) {
    writeToStream(arrayOutput(object).getBytes(StandardCharsets.UTF_8));
  }

  public <T> void render(T object) {
    writeToStream(singleOutput(object).getBytes(StandardCharsets.UTF_8));
  }

  public void writeToStream(byte[] data) {
    try {
      this.outputStream.write(data);
      this.outputStream.flush();
    } catch (IOException e) {
      throw new RuntimeException("Failed to write output from CLI", e);
    }
  }

  private <T extends Collection<?>> String arrayOutput(T object) {
    switch (outputFormat) {
      case JSON:
        return serializeToJSON(object) + "\n";
      case TEXT:
        return Joiner.on("\n").join(object.stream().map(Object::toString).filter(s -> !s.isEmpty()).collect(Collectors.toList())) + "\n";
      default:
        throw new RuntimeException("Unexpected output format");
    }
  }

  public <T> String singleOutput(T object) {
    switch (outputFormat) {
      case JSON:
        return serializeToJSON(object) + "\n";
      case TEXT:
        return object.toString() + "\n";
      default:
        throw new RuntimeException("Unexpected output format");
    }
  }

  private String serializeToJSON(Object object) {
    ObjectMapper m = new ObjectMapper()
        .registerModule(new Jdk8Module());
    m.enable(SerializationFeature.INDENT_OUTPUT);
    try {
      return m.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize output to JSON", e);
    }
  }
}
