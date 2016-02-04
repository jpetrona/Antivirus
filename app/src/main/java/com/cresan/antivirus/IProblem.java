package com.cresan.antivirus;


import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hexdump on 03/02/16.
 */
public interface IProblem extends IJSONSerializer
{
    enum ProblemType { AppProblem, SystemProblem}

    public ProblemType getType();
    public boolean isDangerous();
    public boolean problemExists(Context context);
}


