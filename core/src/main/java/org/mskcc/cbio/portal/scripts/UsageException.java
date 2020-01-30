package org.mskcc.cbio.portal.scripts;

import java.io.IOException;
import joptsimple.OptionParser;

/**
 * RuntimeException to signal invalid command line usage
 *
 * @see {@link ConsoleRunnable}
 */
public class UsageException extends IllegalArgumentException {
    // the name of the command
    private String prog;
    // an OptionParser configured with the accepted options
    private OptionParser parser;
    // a string describing the arguments accepted
    private String argspec;
    // short description of the tool to display in the usage line
    private String description;
    // a final error/exit message
    private String msg;

    /**
     * Instantiates a UsageException using an OptionParser.
     *
     * @param prog  the name of the command
     * @param description  short description of the tool to display in
     *                     the usage line
     * @param parser  an OptionParser configured with the accepted options
     */
    public UsageException(
        String prog,
        String description,
        OptionParser parser
    ) {
        super("Invalid usage of the " + prog + " script");
        this.prog = prog;
        this.description = description;
        this.parser = parser;
    }

    /**
     * Instantiates a UsageException using an explicit argument specification.
     *
     * @param prog  the name of the command
     * @param description  short description of the tool to display in
     *                     the usage line
     * @param argspec  the string describing the arguments accepted
     *
     * @see  <a href="http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap12.html#tag_12_01">
     *           chapter 12.01 of the POSIX specification, on utility argument
     *           syntax conventions
     *       </a>
     */
    public UsageException(String prog, String description, String argspec) {
        super("Invalid usage of the " + prog + " script");
        this.prog = prog;
        this.description = description;
        this.argspec = argspec;
    }

    /**
     * Instantiates a UsageException using an OptionParser.
     *
     * @param prog  the name of the command
     * @param description  short description of the tool to display in
     *                     the usage line
     * @param parser  an OptionParser configured with the accepted options
     */
    public UsageException(
        String prog,
        String description,
        OptionParser parser,
        String msg
    ) {
        super(msg);
        this.prog = prog;
        this.description = description;
        this.parser = parser;
        this.msg = msg;
    }

    /**
     * Instantiates a UsageException using an explicit argument specification.
     *
     * @param prog  the name of the command
     * @param description  short description of the tool to display in
     *                     the usage line
     * @param argspec  the string describing the arguments accepted
     * @param msg  a final error/exit message
     *
     * @see  <a href="http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap12.html#tag_12_01">
     *           chapter 12.01 of the POSIX specification, on utility argument
     *           syntax conventions
     *       </a>
     */
    public UsageException(
        String prog,
        String description,
        String argspec,
        String msg
    ) {
        super(msg);
        this.prog = prog;
        this.description = description;
        this.argspec = argspec;
        this.msg = msg;
    }

    /**
     * Prints a helpful message about command line usage of the utility.
     */
    public void printUsageLine() {
        if (msg != null) {
            System.err.printf("%s\n", msg);
        }
        if (argspec != null) {
            System.err.printf("usage: %s %s\n", prog, argspec);
        }
        if (description != null) {
            System.err.printf("%s\n", description);
        }
        if (parser != null) {
            try {
                System.err.printf("Command line arguments for %s:\n", prog);
                parser.printHelpOn(System.err);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
