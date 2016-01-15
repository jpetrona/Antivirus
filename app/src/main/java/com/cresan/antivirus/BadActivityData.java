package com.cresan.antivirus;

import android.content.pm.ActivityInfo;

/**
 * Created by hexdump on 15/01/16.
 */
public class BadActivityData
{
    private ActivityInfo _activityInfo;
    public ActivityInfo getActivityInfo() {return _activityInfo;}
    private int _reasonId;
    public int getReasorId() {return _reasonId;}

    public BadActivityData(ActivityInfo activityInfo, int reasonId)
    {
        _activityInfo=activityInfo;
        _reasonId=reasonId;
    }

    public int hashCode()
    {
        return (int) _activityInfo.packageName.hashCode()+_reasonId;
    }

    public boolean equals(Object o)
    {
        if(o == null)
            return false;

        BadActivityData other = (BadActivityData) o;
        return  _activityInfo.packageName.equals(other._activityInfo.packageName);
    }
}
