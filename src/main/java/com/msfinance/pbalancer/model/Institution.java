package com.msfinance.pbalancer.model;

import java.util.List;

public class Institution
{
    // TODO: get a better list
    public static List<String> ALL = List.of(
            "Vanguard",
            "Fidelity",
            "Schwab",
            "TD Ameritrade",
            "E*Trade",
            "Treasury Direct",
            "Other"
            );

    private Institution() {}
}
