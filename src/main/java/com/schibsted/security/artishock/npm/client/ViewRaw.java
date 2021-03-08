/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.npm.client;

import java.util.List;
import java.util.Map;

public class ViewRaw {
  public String name;
  public Map<String, Version> versions;
  public Map<String, String> time;
  public List<Maintainers> maintainers;
  public String error;
  public List<Error> errors;

  public static class Version {
    public String version;
    public String name;
  }

  public static class Repository {
    public String type;
    public String url;
  }

  public static class Maintainers {
    public String name;
    public String email;
  }

  public static class Error {
    public int status;
    public String message;
  }
}
