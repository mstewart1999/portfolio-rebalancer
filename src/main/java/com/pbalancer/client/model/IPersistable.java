package com.pbalancer.client.model;

public interface IPersistable
{
    public void markDirty();
    public void markClean();
    public boolean isDirty();
}
