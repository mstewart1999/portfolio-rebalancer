package com.pbalancer.client.model.aa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pbalancer.client.service.FtpDownload;
import com.pbalancer.client.util.FileUtil;

public class AssetTickerCache
{
    private static final Logger LOG = LoggerFactory.getLogger(AssetTickerCache.class);

    private static final String MFLIST_FILE_NAME = "mfundslist.txt";
    private static final String NASDAQ_FILE_NAME = "nasdaqlisted.txt";
    private static final String OTHER_FILE_NAME = "otherlisted.txt";
    private static final String MFLIST_URL = "ftp://ftp.nasdaqtrader.com/SymbolDirectory/mfundslist.txt";
    private static final String NASDAQ_URL = "ftp://ftp.nasdaqtrader.com/SymbolDirectory/nasdaqlisted.txt";
    private static final String OTHER_URL = "ftp://ftp.nasdaqtrader.com/SymbolDirectory/otherlisted.txt";

    private static AssetTickerCache instance = null;

    public static synchronized AssetTickerCache getInstance()
    {
        if(instance == null)
        {
            instance = new AssetTickerCache();
            instance.load();
        }
        else
        {
            instance.reload(1);
        }
        return instance;
    }

    private Map<String,AssetTicker> known = new TreeMap<>();
    private long loadTmstp = 0;

    private AssetTickerCache()
    {
    }

    private void reload(final int days)
    {
        long millis = days*1000L*60*60*24;
        long now = System.currentTimeMillis();
        if(loadTmstp > (now-millis))
        {
            return;
        }
        else
        {
            load();
        }
    }

    private void load()
    {
        Map<String,AssetTicker> newKnown = new TreeMap<>();

        Path p = Paths.get("./data", MFLIST_FILE_NAME);
        String url = MFLIST_URL;
        if(!FileUtil.isNewerThanDays(p, 7))
        {
            try
            {
                FtpDownload.download(p, url);
                LOG.info("Downloaded: " + url);
            }
            catch (IOException e)
            {
                LOG.error("Error Downloading: " + url, e);
            }
        }
        try
        {
            Files.lines(p)
                .skip(1)
                .filter(l -> !l.startsWith("File Creation Time"))
                .filter(l -> !l.isBlank())
                .map(l -> new MFList(l.split(Pattern.quote("|"))))
                .filter(a -> a.isKeeper())
                .map(a -> a.toTicker())
                .forEach(t -> newKnown.put(t.getSymbol(), t))
                ;
            LOG.info("Loaded: " + p);
        }
        catch(IOException|NullPointerException|ArrayIndexOutOfBoundsException e)
        {
            throw new RuntimeException("Unable to load mutual funds from: " + p, e);
        }

        p = Paths.get("./data", NASDAQ_FILE_NAME);
        url = NASDAQ_URL;
        if(!FileUtil.isNewerThanDays(p, 7))
        {
            try
            {
                FtpDownload.download(p, url);
                LOG.info("Downloaded: " + url);
            }
            catch (IOException e)
            {
                LOG.error("Error Downloading: " + url, e);
            }
        }
        try
        {
            Files.lines(p)
                .skip(1)
                .filter(l -> !l.startsWith("File Creation Time"))
                .filter(l -> !l.isBlank())
                .map(l -> new NasdaqList(l.split(Pattern.quote("|"))))
                .filter(a -> a.isKeeper())
                .map(a -> a.toTicker())
                .forEach(t -> newKnown.put(t.getSymbol(), t))
                ;
            LOG.info("Loaded: " + p);
        }
        catch(IOException|NullPointerException|ArrayIndexOutOfBoundsException e)
        {
            throw new RuntimeException("Unable to load nasdaq tickers from: " + p, e);
        }


        p = Paths.get("./data", OTHER_FILE_NAME);
        url = OTHER_URL;
        if(!FileUtil.isNewerThanDays(p, 7))
        {
            try
            {
                FtpDownload.download(p, url);
                LOG.info("Downloaded: " + url);
            }
            catch (IOException e)
            {
                LOG.error("Error Downloading: " + url, e);
            }
        }
        try
        {
            Files.lines(p)
                .skip(1)
                .filter(l -> !l.startsWith("File Creation Time"))
                .filter(l -> !l.isBlank())
                .map(l -> new OtherList(l.split(Pattern.quote("|"))))
                .filter(a -> a.isKeeper())
                .map(a -> a.toTicker())
                .forEach(t -> newKnown.put(t.getSymbol(), t))
                ;
            LOG.info("Loaded: " + p);
        }
        catch(IOException|NullPointerException|ArrayIndexOutOfBoundsException e)
        {
            throw new RuntimeException("Unable to load other tickers from: " + p, e);
        }

        known = newKnown;
        loadTmstp = System.currentTimeMillis();
    }

    public AssetTicker lookup(String symbol)
    {
        if(symbol == null)
        {
            symbol = "";
        }
        return known.get(symbol);
    }

    public Collection<AssetTicker> list()
    {
        return Collections.unmodifiableCollection(known.values());
    }

    public Map<String,AssetTicker> all()
    {
        return Collections.unmodifiableMap(known);
    }


    /**
     * Record for mfundslist.txt.
     * See https://www.nasdaqtrader.com/Trader.aspx?id=SymbolDirDefs#funds
     */
    private record MFList(String symbol, String name, String family, String type, String category, String pricingAgent)
    {
        public MFList(final String[] parts)
        {
            this(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
        }

        public boolean isKeeper()
        {
            return
//                !symbol.equals("Fund Symbol")
//                &&
//                !symbol.startsWith("File Creation Time")
//                &&
                !family.equals("NASDAQ Test Funds")
                ;
        }

        public AssetTicker toTicker()
        {
            return new AssetTicker(symbol, name);
        }
    }

    /**
     * Record for nasdqlisted.txt.
     * See https://www.nasdaqtrader.com/Trader.aspx?id=SymbolDirDefs#nasdaq
     */
    private record NasdaqList(String symbol, String name, String category, String testIssue, String finStatus, String roundLot)
    {
        public NasdaqList(final String[] parts)
        {
            this(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
            // file also contains undocumented fields
            // ETF and NextShares
        }

        public boolean isKeeper()
        {
            return
//                !symbol.equals("Symbol")
//                &&
//                !symbol.startsWith("File Creation Time")
//                &&
                !testIssue.equals("Y")
                ;
        }

        public AssetTicker toTicker()
        {
            return new AssetTicker(symbol, name);
        }
    }

    /**
     * Record for otherlisted.txt.
     * See https://www.nasdaqtrader.com/Trader.aspx?id=SymbolDirDefs#other
     */
    private record OtherList(String actSymbol, String name, String exchange, String cqsSymbol, String etf, String roundLot, String testIssue, String nasdaqSymbol)
    {
        public OtherList(final String[] parts)
        {
            this(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        }

        public boolean isKeeper()
        {
            if(!actSymbol.equals(cqsSymbol) || !actSymbol.equals(nasdaqSymbol) || ! cqsSymbol.equals(nasdaqSymbol))
            {
                // https://www.nasdaqtrader.com/trader.aspx?id=CQSsymbolconvention
                // these are warrants, debentures, preferred stock, weird "units", and other unknown stuff
                // 676 out of 6487
                //System.out.printf("%s | %s | %s | %s | %s %n", exchange, actSymbol, cqsSymbol, nasdaqSymbol, name);
            }
            return
//                !actSymbol.equals("ACT Symbol")
//                &&
//                !actSymbol.startsWith("File Creation Time")
//                &&
                !testIssue.equals("Y")
                ;
        }

        public AssetTicker toTicker()
        {
            // google finance seems to know at least some of these by their nasqdaqSymbol
            // but use CQS?
            return new AssetTicker(cqsSymbol, name);
        }
    }
}
