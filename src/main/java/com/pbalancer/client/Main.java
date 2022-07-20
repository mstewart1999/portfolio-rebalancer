package com.pbalancer.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;

public class Main
{
    private static final Logger LOG;
    static
    {
        setupConsole();
        setupLogging();
        LOG = LoggerFactory.getLogger(Main.class);
    }

    /**
     * Setup stdout and stderr (if necessary).
     * To be most effective, this should be the very first thing executed - even before main()
     */
    private static void setupConsole()
    {
        String nativeProp = System.getProperty("org.graalvm.nativeimage.imagecode", "");
        if(nativeProp.equals("runtime"))
        {
            // gluon build the native-image as /SUBSYSTEM:WINDOWS, rather than /SUBSYSTEM:CONSOLE
            // this means there is no console to view output and stdout/stderr go nowhere
            // capture this output for debugging
            try
            {
                System.out.println("Redirecting console output to file"); // this goes nowhere, but better safe than confused while debugging
                //Object oldOut = System.out;
                //Object oldErr = System.err;
                ConsoleSubstitute console = new ConsoleSubstitute("./console.txt");
                System.setOut(console);
                System.setErr(console);

                /* these don't provide any useful info
                System.out.println("old System.out=" + oldOut);
                System.out.println("old System.err=" + oldErr);
                System.out.println("old System.out.class=" + oldOut.getClass());
                System.out.println("old System.err.class=" + oldErr.getClass());
                */
                //System.out.println(System.getProperties());
            }
            catch (FileNotFoundException e)
            {
                // pretty worthless
                e.printStackTrace();
            }
        }
    }

    private static void setupLogging()
    {
        if(Files.exists(Path.of("./logging.properties")))
        {
            try(final InputStream in = new FileInputStream("./logging.properties"))
            {
                LogManager.getLogManager().readConfiguration(in);
                System.out.println("Configured JUL from file ./logging.properties");
            }
            catch (final IOException e)
            {
                System.err.println("Could not load default logging.properties file");
                e.printStackTrace();
            }
        }
        else if(Main.class.getResource("/logging.properties") != null)
        {
            // use the one bundled in classpath, jar, or native executable
            try(final InputStream in = Main.class.getResourceAsStream("/logging.properties"))
            {
                LogManager.getLogManager().readConfiguration(in);
                System.out.println("Configured JUL from resource /logging.properties");
            }
            catch (final IOException e)
            {
                System.err.println("Could not load default logging.properties resource");
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("No JUL config file nor resource.");
        }

        // old config method, did not jive with native-image resource URLs
        //System.setProperty("java.util.logging.config.file", path);
    }

    public static void main(final String args[]) throws IOException
    {
        LOG.info("Main.main() - starting");
        try
        {
            Application.launch(App.class, args);
        }
        finally
        {
            LOG.info("Main.main() - ending");
        }
    }
}
