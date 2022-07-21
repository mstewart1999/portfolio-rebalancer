package com.pbalancer.client;

public enum Environment
{
    DEV,
    TEST,
    PROD,
    ;

    private static final String SYS_PROP_KEY = "env";
    private static final Environment DEF = DEV;// default to dev

    private static Environment active;

    public static synchronized Environment getActive()
    {
        if(active == null)
        {
            String prop = System.getProperty(SYS_PROP_KEY);
            if((prop == null) || prop.isBlank())
            {
                prop = DEF.name(); // default to dev
            }
            prop = prop.trim().toUpperCase();
            try
            {
                active = Environment.valueOf(prop);
                if(active == null)
                {
                    throw new RuntimeException("Illegal Environment system prop env='" + prop + "'");
                }
            }
            catch (IllegalArgumentException  e)
            {
                throw new RuntimeException("Illegal Environment system prop env='" + prop + "'", e);
            }
        }
        return active;
    }
}
