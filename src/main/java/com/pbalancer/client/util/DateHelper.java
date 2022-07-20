package com.pbalancer.client.util;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
import java.util.Date;

public class DateHelper
{
    private final static DateTimeFormatter US_DATE = new DateTimeFormatterBuilder()
            .appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NEVER)
            .appendLiteral('/')
            .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NEVER)
            .appendLiteral('/')
            .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .parseLenient()
            .toFormatter(); //ResolverStyle.STRICT, IsoChronology.INSTANCE);


    public static LocalDate parseUSLocalDate(final String text) throws DateTimeParseException, IndexOutOfBoundsException
    {
        if(text == null)
        {
            return null;
        }
        return US_DATE.parse(text, LocalDate::from);
    }

    public static String formatUSLocalDate(final LocalDate dt)
    {
        if(dt == null)
        {
            return "";
        }
        return US_DATE.format(dt);
    }

    public static String formatISOLocalDate(final Date when)
    {
        if(when == null)
        {
            return "";
        }
        return DateTimeFormatter.ISO_LOCAL_DATE.format(
                when.toInstant().atZone(ZoneId.systemDefault()));
    }

    /**
     * Determine if a date is older than a given number of days.
     * @param dt when
     * @param days how many
     * @return true if it has been at least 'days' nbr of days since 'dt'
     */
    public static boolean olderThanDays(final Date dt, final int days)
    {
        Date now = new Date();
        long daysDiff = (now.getTime() - dt.getTime()) / 1000 / 60 /60 / 24;
        return daysDiff >= days;
    }

    public static boolean olderThanDays(final LocalDate dt, final int days)
    {
        Date date = Date.from(dt.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return olderThanDays(date, days);
    }
}
