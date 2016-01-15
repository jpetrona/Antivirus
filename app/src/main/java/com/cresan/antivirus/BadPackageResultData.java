package com.cresan.antivirus;

import android.content.pm.PackageInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by hexdump on 15/01/16.
 */
public class BadPackageResultData
{
    public String getPackageName() { return _packageInfo.packageName; }

    private PackageInfo _packageInfo;
    public PackageInfo getPackageInfo() {return _packageInfo;}
    public void setPackageInfo(PackageInfo pi) { _packageInfo=pi;}

    public BadPackageResultData(PackageInfo packageInfo)
    {
        _packageInfo=packageInfo;
    }

    private Set<BadActivityData> _badActivities;
    public void AddBadActivityData(BadActivityData bad)
    {
        if(_badActivities==null)
            _badActivities=new HashSet<BadActivityData>();
        _badActivities.add(bad);
    }

    public int hashCode()
    {
        return (int) getPackageName().hashCode();
    }

    public boolean equals(Object o)
    {
        if(o == null)
            return false;

        BadPackageResultData other = (BadPackageResultData) o;
        return _packageInfo.packageName.equals(other._packageInfo);
    }
}
