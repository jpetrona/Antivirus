package com.cresan.antivirus;

/**
 * Created by hexdump on 03/02/16.
 */
public interface IFactory<T>
{
    T createInstance(String s);
}
