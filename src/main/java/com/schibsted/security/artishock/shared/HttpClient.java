/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.shared;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpClient {
  private static final Logger log = LogManager.getLogger();

  private static final OkHttpClient client = new OkHttpClient.Builder()
      .connectTimeout(50, TimeUnit.SECONDS)
      .readTimeout(300, TimeUnit.SECONDS)
      .writeTimeout(50, TimeUnit.SECONDS)
      .build();

  public static Request prepareRequest(ConnectionInfo connectionInfo, String path) {
    var url = connectionInfo.getPrefix() + path;
    var requestBuilder = new Request.Builder()
        .url(url);

    if (connectionInfo.getUsername().isPresent() && connectionInfo.getPassword().isPresent()) {
      var credentials = Credentials.basic(connectionInfo.getUsername().get(), connectionInfo.getPassword().get());
      requestBuilder.addHeader("Authorization", credentials);
    } else {
      // External request: be polite
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // do nothing
      }
    }

    return requestBuilder.build();
  }

  private static Response execute(Request request) {
    log.info(() -> "Fetching " + request.url());

    int retry = 0;
    do {
      try {
        var response = client.newCall(request).execute();
        if (response.code() == 429) {
          retry++;
          try {
            Thread.sleep(2000);
          } catch (InterruptedException ignore) {
          }
        } else {
          throwIfUnauthorized(response);

          return response;
        }
      } catch (IOException e) {
        throw new RuntimeException("Failed to fetch " + request.url(), e);
      }
    } while (retry < 5);

    throw new RuntimeException("more than 5 retries for " + request.url());
  }

  private static void throwIfUnauthorized(Response response) {
    if (response.code() == 401) {
      response.close();
      throw new RuntimeException("401 Unauthorized " + response.request().url());
    }
  }

  public static String fetch(ConnectionInfo connectionInfo, String path) {
    var request = prepareRequest(connectionInfo, path);

    try (var response = execute(request)) {
      if (!response.isSuccessful()) {
        throw new RuntimeException("Download not successful from " + request.url());
      }
      var body = response.body();
      if (body == null) {
        throw new RuntimeException("Failed to get body from " + request.url());
      }
      return body.string();
    } catch (IOException e) {
      throw new RuntimeException("Failed to get body from " + request.url());
    }
  }

  /**
   * Returns true if response is 200, false if response is 404, throws otherwise
   */
  public static boolean exists(ConnectionInfo connectionInfo, String path) {
    var request = prepareRequest(connectionInfo, path);

    try (var response = execute(request)) {
      if (response.code() == 200) {
        return true;
      } else if (response.code() == 404) {
        return false;
      }
      throw new RuntimeException(String.format("Expected code '200' or '404' for '%s', got '%d'", request.url(), response.code()));
    }
  }
}
