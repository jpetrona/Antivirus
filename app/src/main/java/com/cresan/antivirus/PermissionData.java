package com.cresan.antivirus;

import android.content.pm.ActivityInfo;

/**
 * Created by hexdump on 15/01/16.
 */
public class PermissionData
{
    private int _hazzard;
    public int getReasorId() {return _hazzard;}
    private String _permissionName;
    public String getPermissionName() { return _permissionName;}

    public PermissionData(String permissionName, int hazzard)
    {
        _permissionName=permissionName;
        _hazzard=hazzard;
    }

    public int hashCode()
    {
        return (int) _permissionName.hashCode()+_hazzard;
    }

    public boolean equals(Object o)
    {
        if(o == null)
            return false;

        PermissionData other = (PermissionData) o;
        return  _permissionName.equals(other._permissionName);
    }
}
