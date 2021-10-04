package org.flinkcoin.node.configuration;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

public class CommandLineArguments {

    @Parameter(names = {"-h", "--help"}, help = true)
    public boolean help;

    @Parameters(
            commandNames = {"generate"},
            commandDescription = "Generate command"
    )
    public static class GenerateCommand {

    }

    @Parameters(
            commandNames = {"daemon"},
            commandDescription = "Generate command"
    )
    public static class DaemonCommand {

    }

}
