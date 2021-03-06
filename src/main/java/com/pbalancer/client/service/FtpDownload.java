package com.pbalancer.client.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class FtpDownload
{
    public static void download(final Path p, final String url) throws IOException
    {
        Files.createDirectories(p.getParent());
        try(
                InputStream in = new URL(url).openStream();
                OutputStream out = Files.newOutputStream(p)
            )
        {
            in.transferTo(out);
        }
    }
}
