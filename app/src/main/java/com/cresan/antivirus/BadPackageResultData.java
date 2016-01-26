package com.cresan.antivirus;

import android.content.pm.PackageInfo;

import java.io.Serializable;
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

    private Set<ActivityData> _activities=new HashSet<ActivityData>();
    public void addActivityData(ActivityData bad)  { _activities.add(bad);  }
    public Set<ActivityData> getActivityData() { return _activities; }

    private Set<PermissionData> _permissions=new HashSet<PermissionData>();
    public void addPermissionData(PermissionData bad) { _permissions.add(bad);  }
    public Set<PermissionData> getPermissionData() { return _permissions; }

    private boolean _installedThroughGooglePlay=false;
    public boolean getInstalledThroughGooglePlay() { return _installedThroughGooglePlay; }
    public void setInstalledThroughGooglePlay(boolean installed) { _installedThroughGooglePlay=installed;}

    public int hashCode()
    {
        return (int) getPackageName().hashCode();
    }


    public boolean equals(Object o)
    {
        if(o == null)
            return false;

        BadPackageResultData other = (BadPackageResultData) o;
        return _packageInfo.packageName.equals(other._packageInfo.packageName);
    }

    public boolean isMenace()
    {
        return !getInstalledThroughGooglePlay() || getActivityData().size()>0 || getPermissionData().size()>0;
    }
}
