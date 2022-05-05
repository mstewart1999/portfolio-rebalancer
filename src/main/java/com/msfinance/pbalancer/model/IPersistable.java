package com.msfinance.pbalancer.model;

public interface IPersistable
{
    public void markDirty();
    public void markClean();
    public boolean isDirty();
}
