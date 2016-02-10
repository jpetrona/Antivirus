package com.cresan.antivirus;

import android.content.Context;

import java.util.Set;


/**
 * Created by hexdump on 22/01/16.
 */
public class MenacesCacheSet extends JSONDataSet<IProblem>
{
    public MenacesCacheSet(Context context)
    {
        super(context,"menacescache.json",new ProblemFactory());
    }
}
