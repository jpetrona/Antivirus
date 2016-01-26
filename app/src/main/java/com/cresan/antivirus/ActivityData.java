package com.cresan.antivirus;

import android.content.pm.ActivityInfo;

import java.io.Serializable;

/**
 * Created by hexdump on 15/01/16.
 */
public class ActivityData
{
    private ActivityInfo _activityInfo;
    public ActivityInfo getActivityInfo() {return _activityInfo;}
    private int _reasonId;
    public int getReasorId() {return _reasonId;}

    public ActivityData(ActivityInfo activityInfo, int reasonId)
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

        ActivityData other = (ActivityData) o;
        return  _activityInfo.packageName.equals(other._activityInfo.packageName);
    }
}
