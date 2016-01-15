package com.cresan.antivirus;

import android.content.pm.PackageInfo;

import java.util.List;

/**
 * Created by hexdump on 15/01/16.
 */
public class PackageData
{
    private String _packageName;
    public String getPackageName() { return _packageName; }
    public void setPackageName(String packageName) { _packageName=packageName;}

    public int hashCode()
    {
        return (int) _packageName.hashCode();
    }

    public boolean equals(Object o)
    {
        if(o == null)
            return false;

        PackageData other = (PackageData) o;
        return _packageName.equals(other._packageName);
    }

    public List<PackageData> getPackagesByName(List<PackageData> packages, String filter, List<PackageData> result)
    {
        boolean wildcard=false;

        result.clear();

        if(filter.charAt(filter.length()-1)=='*')
        {
            wildcard=true;
            filter=filter.substring(0,filter.length()-2);
        }
        else
            wildcard=false;

        PackageData packInfo =null;

        for (int i=0; i < packages.size(); i++)
        {
            packInfo=packages.get(i);

            if(packInfo._packageName.startsWith(filter))
            {
                result.add(packInfo);

                //Just one package if we were not using a wildcard
                if (!wildcard)
                    break;
            }
        }

        return result;
    }
}
