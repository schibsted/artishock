/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.npm.client;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchRaw {
  public List<Object> objects;

  public static class Object {
    @JsonProperty("package")
    public Package aPackage;
  }

  public static class Package {
    public String name;
    public String scope;
    public String date;
    public String description;
    public UsernameEmail publisher;
    public List<UsernameEmail> maintainers;
  }

  public static class UsernameEmail {
    public String username;
    public String email;
  }
}
