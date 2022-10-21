/*
 * Copyright 2021 Schibsted. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.artishock;

import com.google.common.collect.ListMultimap;
import com.schibsted.security.artishock.cli.view.Commands;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import io.airlift.airline.Cli;
import io.airlift.airline.Help;
import io.airlift.airline.OptionType;
import io.airlift.airline.ParseArgumentsMissingException;
import io.airlift.airline.ParseArgumentsUnexpectedException;
import io.airlift.airline.ParseOptionMissingException;
import io.airlift.airline.Parser;
import io.airlift.airline.model.GlobalMetadata;
import io.airlift.airline.model.OptionMetadata;

public class ArtishockCli {
  public static void main(String[] args) {
    var builder = Cli.<Runnable>builder("artishock")
        .withDefaultCommand(Commands.CustomHelp.class);

    builder.withCommand(Commands.CustomHelp.class);
    builder.withCommand(Commands.CustomHelpAlternativeName.class);
    builder.withCommand(Commands.Version.class);
    builder.withCommand(Commands.VersionAlternativeName.class);

    builder.withCommand(Commands.RepoLs.class);
    builder.withCommand(Commands.RepoStats.class);
    builder.withCommand(Commands.PackageStats.class);
    builder.withCommand(Commands.Cached.class);
    builder.withCommand(Commands.InferredExclude.class);
    builder.withCommand(Commands.NotClaimed.class);
    builder.withCommand(Commands.ExcludeCandidates.class);

    var parser = builder.build();
    try {
      parser.parse(args).run();
    } catch (ParseArgumentsUnexpectedException e) {
      System.err.println(e.getMessage());
      System.err.println("Try '--help' instead");
      System.exit(1);
    } catch (ParseArgumentsMissingException | ParseOptionMissingException e) {
      if (flagIsSet("--help", parser.getMetadata(), args)) {
        Help.help(parser.getMetadata(), Arrays.asList(args));
      } else {
        System.err.println(e.getMessage());
        System.err.println("Try adding '--help'");
      }
      System.exit(1);
    } catch (Exception e) {
      boolean stacktrace = flagIsSet("--stacktrace", parser.getMetadata(), args);

      if (!stacktrace) {
        System.err.println(e.getMessage());
        System.exit(1);
      }
      throw e;
    }
  }

  private static boolean flagIsSet(String flag, final GlobalMetadata globalMetadata, final String[] args) {
    try {
      Parser p = new Parser();
      ListMultimap<OptionMetadata, Object> options = p.parse(globalMetadata, args).getParsedOptions();

      for (Map.Entry<OptionMetadata, Collection<Object>> option : options.asMap().entrySet()) {
        OptionMetadata metadata = option.getKey();

        if (metadata.getOptionType() == OptionType.COMMAND && metadata.getOptions().contains(flag)) {
          return option.getValue().contains(true);
        }
      }
    } catch (Exception e) {
      System.err.println("Failed to determine if the stacktrace should be shown.");
    }

    return false;
  }
}
