package com.pbalancer.client;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ConsoleSubstitute extends PrintStream
{
    public ConsoleSubstitute(final String filename) throws FileNotFoundException
    {
        super(new FileOutputStream(filename), true, StandardCharsets.UTF_8);
    }
}