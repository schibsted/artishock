/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock.cli.view;

import com.schibsted.security.artishock.cli.viewmodel.Artishock;
import java.util.List;
import javax.inject.Inject;
import io.airlift.airline.Command;
import io.airlift.airline.Help;
import io.airlift.airline.OptionType;
import io.airlift.airline.model.GlobalMetadata;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

public class Commands {
  private static final String PACKAGE_SYSTEM_NAME = "--package-system";
  private static final String LOCAL_NAME = "--local";
  private static final String LOCAL_DESCRIPTION = "Name of an Artifactory repo of type local";
  private static final String REMOTE_NAME = "--remote";
  private static final String REPO_DESCRIPTION = "Name of Artifactory repo";
  private static final String REPO_NAME = "--repo";
  private static final String REMOTE_DESCRIPTION = "Name of an Artifactory repo of type remote";
  private static final String QUERY_UPSTREAM_NAME = "--query-upstream";
  private static final String QUERY_UPSTREAM_DESCRIPTION = "Acknowledge that this command sends internal package names upstream";

  private static Artishock confused() {
    return new Artishock();
  }

  @io.airlift.airline.Command(name = "help", description = "Help")
  public static class CustomHelp extends Help {
    @Override
    public void run() {
      super.run();
      System.exit(1);
    }
  }

  @io.airlift.airline.Command(name = "--help", description = "Help", hidden = true)
  public static class CustomHelpAlternativeName extends CustomHelp {
  }

  @io.airlift.airline.Command(name = "version", description = "Get version")
  public static class Version implements Runnable {

    @Override
    public void run() {
      Package p = getClass().getPackage();
      String version = p.getImplementationVersion();

      System.out.printf("artishock %s%n", version);
    }
  }

  @io.airlift.airline.Command(name = "--version", description = "Get version", hidden = true)
  public static class VersionAlternativeName extends Version {
  }

  public static class BaseCommand implements Runnable {
    @Inject
    public GlobalMetadata global;

    @io.airlift.airline.Option(type = OptionType.COMMAND, name = "--json", description = "Output as JSON")
    public boolean json;

    @io.airlift.airline.Option(type = OptionType.COMMAND, name = "--verbose", description = "Make verbose")
    public boolean verbose;

    @io.airlift.airline.Option(type = OptionType.COMMAND, name = "--stacktrace", description = "Enable stacktrace")
    public boolean stacktrace;

    @io.airlift.airline.Option(type = OptionType.COMMAND, name = "--help", description = "Show help")
    public boolean help;

    protected Renderer renderer() {
      return new Renderer(json ? OutputFormat.JSON : OutputFormat.TEXT, System.out);
    }

    private void help(List<String> commands) {
      if (help) {
        Help.help(global, commands);
        System.exit(0);
      }
    }

    private void configureVerbose() {
      if (verbose) {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
      } else {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.OFF);
      }
    }

    protected void verboseAndHelp(String command) {
      configureVerbose();
      help(List.of(command));
    }

    @Override
    public void run() {
    }
  }

  @Command(name = "repo-ls", description = "List Artifactory repositories")
  public static class RepoLs extends BaseCommand {
    @io.airlift.airline.Option(type = OptionType.COMMAND, name = PACKAGE_SYSTEM_NAME, description = "maven, npm, pypi")
    public String packageSystem;

    @Override
    public void run() {
      verboseAndHelp("repo-ls");

      renderer().render(confused().repoLs(packageSystem));
    }
  }


  @Command(name = "repo-stats", description = "Stats for a given Artifactory repository by iterating over all files (slow)")
  public static class RepoStats extends BaseCommand {

    @io.airlift.airline.Option(name = REPO_NAME, description = REPO_DESCRIPTION, required = true)
    public String repoName;

    @io.airlift.airline.Option(name = PACKAGE_SYSTEM_NAME, description = "maven, npm, pypi", required = true)
    public String packageSystem;

    @Override
    public void run() {
      verboseAndHelp("repo-stats");

      renderer().render(confused().repoStats(repoName, packageSystem));
    }
  }

  @Command(name = "package-stats", description = "Stats for a given package in Artifactory by iterating recursively (can be slow)")
  public static class PackageStats extends BaseCommand {

    @io.airlift.airline.Option(name = REPO_NAME, description = REPO_DESCRIPTION, required = true)
    public String repoName;

    @io.airlift.airline.Option(name = PACKAGE_SYSTEM_NAME, description = "npm", required = true)
    public String packageSystem;

    @io.airlift.airline.Option(name = "--package", description = "Name of the package", required = true)
    public String packageName;

    @Override
    public void run() {
      verboseAndHelp("package-stats");

      renderer().render(confused().packageStats(repoName, packageSystem, packageName));
    }
  }

  @Command(name = "exclude-candidates", description = "Packages that are candidates to be excluded")
  public static class ExcludeCandidates extends BaseCommand {
    @io.airlift.airline.Option(name = PACKAGE_SYSTEM_NAME, description = "npm, pypi", required = true)
    public String packageSystem;

    @io.airlift.airline.Option(name = LOCAL_NAME, description = LOCAL_DESCRIPTION, required = true)
    public String local;

    @io.airlift.airline.Option(name = "--trusted", description = "File containing trusted packages")
    public String trusted;

    @io.airlift.airline.Option(name = "--excluded", description = "File containing excluded packages")
    public String excluded;

    @Override
    public void run() {
      verboseAndHelp("exclude-candidates");

      renderer().render(confused().excludeCandidates(packageSystem, local, trusted, excluded));
    }
  }

  @Command(name = "cached", description = "Local packages that exist upstream and have been cached by Artifactory")
  public static class Cached extends BaseCommand {
    @io.airlift.airline.Option(name = PACKAGE_SYSTEM_NAME, description = "npm, pypi", required = true)
    public String packageSystem;

    @io.airlift.airline.Option(name = LOCAL_NAME, description = LOCAL_DESCRIPTION, required = true)
    public String local;

    @io.airlift.airline.Option(name = REMOTE_NAME, description = REMOTE_DESCRIPTION, required = true)
    public String remote;

    @Override
    public void run() {
      verboseAndHelp("cached");

      renderer().render(confused().cached(packageSystem, local, remote));
    }
  }

  @Command(name = "inferred-exclude", description = "Infer excluded packages (best effort)")
  public static class InferredExclude extends BaseCommand {
    @io.airlift.airline.Option(name = PACKAGE_SYSTEM_NAME, description = "npm, pypi", required = true)
    public String packageSystem;

    @io.airlift.airline.Option(name = LOCAL_NAME, description = LOCAL_DESCRIPTION, required = true)
    public String local;

    @io.airlift.airline.Option(name = REMOTE_NAME, description = REMOTE_DESCRIPTION, required = true)
    public String remote;

    @io.airlift.airline.Option(name = QUERY_UPSTREAM_NAME, description = QUERY_UPSTREAM_DESCRIPTION, required = false)
    boolean queryUpstream;

    @Override
    public void run() {
      verboseAndHelp("inferred-exclude");

      renderer().render(confused().inferredExclude(packageSystem, local, remote, queryUpstream));
    }
  }

  @Command(name = "not-claimed", description = "Local packages not claimed upstream")
  public static class NotClaimed extends BaseCommand {
    @io.airlift.airline.Option(name = PACKAGE_SYSTEM_NAME, description = "npm, pypi", required = true)
    public String packageSystem;

    @io.airlift.airline.Option(name = LOCAL_NAME, description = LOCAL_DESCRIPTION, required = true)
    public String local;

    @io.airlift.airline.Option(name = "--excluded", description = "File containing excluded packages")
    public String excluded;

    @io.airlift.airline.Option(name = QUERY_UPSTREAM_NAME, description = QUERY_UPSTREAM_DESCRIPTION, required = false)
    boolean queryUpstream;

    @Override
    public void run() {
      verboseAndHelp("not-claimed");

      renderer().render(confused().notClaimed(packageSystem, local, excluded, queryUpstream));
    }
  }
}
