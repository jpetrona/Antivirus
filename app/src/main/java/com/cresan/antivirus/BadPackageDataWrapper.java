package com.cresan.antivirus;

import java.util.List;

/**
 * Created by hexdump on 29/01/16.
 */
class BadPackageDataWrapper
{
    public BadPackageDataWrapper(BadPackageData abpd, boolean achecked)
    {
        bpd=abpd;
        checked=achecked;
    }
    public BadPackageData bpd=null;
    public boolean checked=false;

    static public  BadPackageDataWrapper findByPackageDataInstance(PackageData pd, List<BadPackageDataWrapper> bpdwl)
    {
        for(BadPackageDataWrapper bpdw : bpdwl)
        {
            if(bpdw.bpd.getPackageName().equals(pd.getPackageName()))
            {
                return bpdw;
            }
        }

        return null;
    }
}
