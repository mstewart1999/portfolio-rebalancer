package com.msfinance.pbalancer.util;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;

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
}
