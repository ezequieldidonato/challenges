package com.quant.challenge.main;

import com.quant.challenge.checker.ApprovalChecker;
import com.quant.challenge.checker.ApprovalException;
import com.quant.challenge.checker.ApprovalResult;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Main class for starting validate_approvals cmd-line utility tool.
 */
public class Main {

    /**
     * Constant for changedFilesCmdLine and approversCmdLine separator
     */
    public static final String  SEPARATOR = ",";

    /**
     * Constant for default path if not specified.
     */
    public static final String DEFAULT_PATH = ".";

    // -- Private attributes for handling command line arguments

    @Option(name="--system-root", usage="Path to system root", handler = org.kohsuke.args4j.spi.PathOptionHandler.class)
    Path systemRoot = Paths.get(DEFAULT_PATH);

    @Option(name="--approvers", required = true, usage="Comma-separated list of approversCmdLine")
    private String approversCmdLine;

    @Option(name="--changed-files", required = true,
            usage="Comma-separated list of changed files with full path relative to system root")
    private String changedFilesCmdLine;


    /**
     * Command-line entry point.
     * @param args command-line arguments.
     */
    public static void main(String ...args) {
        Main main = new Main();
        main.cli(args);
    }

    /**
     * Main method.
     * @param args command-line arguments.
     */
    public void cli(String ...args) {

        // Parse the command-line arguments with args4j
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        // Stores arguments in convenient collections
        Set<String> approvers = getApprovers();
        Set<Path> changedFiles = getChangedFiles();

        // Instantiates the approval checker
        ApprovalChecker approvalChecker = new ApprovalChecker(systemRoot, approvers, changedFiles);

        try {
            // Starts the approval check process.
            ApprovalResult approvalResult = approvalChecker.check();

            // Prints results
            System.out.println(approvalResult.getMessage());
        } catch (ApprovalException e) {
            // Handles Exceptions
            System.out.println("Exception found while approving process was running. " + e.getMessage());
            System.exit(-1);
        }

    }

    /**
     * @return the approversCmdLine from the command-line string as a {@link Set} of strings.
     */
    public Set<String> getApprovers() {
        return new HashSet<>(Arrays.asList(approversCmdLine.trim().split(SEPARATOR)));
    }

    /**
     *
     * @return the changed files from the command-line string as a {@link Set} of {@link Path}.
     */
    public Set<Path> getChangedFiles() {
       final Set<Path> changedFiles = new HashSet<>();
       Arrays.asList(changedFilesCmdLine.trim().split(SEPARATOR)).forEach(file -> changedFiles.add(Paths.get(file)));
       return changedFiles;
    }

}
