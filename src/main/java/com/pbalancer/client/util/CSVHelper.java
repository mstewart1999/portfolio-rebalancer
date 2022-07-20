package com.pbalancer.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CSVHelper
{
    public static List<String> fromCsvLine(final String line)
    {
        List<String> out = new ArrayList<>();
        String[] fields = line.split(","); // extremely naive - no quoting
        for(String f : fields)
        {
            f = Objects.requireNonNullElse(f, "");
            out.add(f.trim());
        }

        // trim trailing empty cells
        while(out.size() > 0)
        {
            int last = out.size()-1;
            if(out.get(last).isBlank())
            {
                out.remove(last);
            }
            else
            {
                break;
            }
        }

        return out;
    }
}
