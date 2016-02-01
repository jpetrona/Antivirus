package com.cresan.antivirus;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.tech.applications.coretools.ActivityTools;
import com.tech.applications.coretools.StringTools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hexdump on 26/01/16.
 */
class Scanner
{
    public  static Set<GoodPackageResultData> scanForWhiteListedApps(List<PackageInfo> packagesToSearch, Set<PackageData> whiteListPackages,
                                                                 Set<GoodPackageResultData> result)
    {
        Set<GoodPackageResultData> subResult=new HashSet<GoodPackageResultData>();

        //Check against whitelist
        for(PackageData pd : whiteListPackages)
        {
            getPackagesByNameFilter(packagesToSearch, pd.getPackageName(), subResult);

            result.addAll(subResult);
        }

        return result;
    }

    public static boolean isAppWhiteListed(String packageName, Set<PackageData> whiteListPackages)
    {
        boolean matches=false;

        //Check against whitelist
        for(PackageData pd : whiteListPackages)
        {
            if(StringTools.stringMatchesMask(packageName,pd.getPackageName()))
                return true;
        }

        return matches;
    }

    //In setToUpdate we receive a set of BadPackageData ready to be update with newly detected menaces
    public static Set<BadPackageData> scanForBlackListedActivityApps(List<PackageInfo> packagesToSearch, Set<PackageData> blackListedActivityPackages,
                                                                        Set<BadPackageData> setToUpdate)
    {
        List<ActivityInfo> subResult=new ArrayList<ActivityInfo>();

        ActivityInfo[] activities;


        for(PackageInfo pi : packagesToSearch)
        {
            for(PackageData pd: blackListedActivityPackages)
            {
                //In subResult we have now all the ActivityInfo entries resulting in a menace
                getActivitiesByNameFilter(pi, pd.getPackageName(), subResult);

                //If we found bad activities in the package fill the bad package information into result
                if(subResult.size()>0)
                {
                    //Update or create new if it does not exist
                    BadPackageData bprd=getBadPackageResultByPackageName(setToUpdate, pi.packageName);
                    if(bprd==null)
                    {
                        bprd = new BadPackageData(pi.packageName);
                        setToUpdate.add(bprd);
                    }

                    for(ActivityInfo ai: subResult)
                    {
                        bprd.addActivityData(new ActivityData(ai.name));
                    }
                }
            }
        }
        return setToUpdate;
    }

    public static BadPackageData scanForBlackListedActivityApp(PackageInfo pi,BadPackageData bprdToFill,
                                                                     Set<PackageData> blackListedActivityPackages, List<ActivityInfo> arrayToRecycle)

    {
        for(PackageData pd: blackListedActivityPackages)
        {
            //In subResult we have now all the ActivityInfo entries resulting in a menace
            getActivitiesByNameFilter(pi, pd.getPackageName(), arrayToRecycle);

            //If we found bad activities in the package fill the bad package information into result
            if(arrayToRecycle.size()>0)
            {
                for(ActivityInfo ai: arrayToRecycle)
                {
                    bprdToFill.addActivityData(new ActivityData(ai.packageName));
                }
            }
        }

        return bprdToFill;
    }

    public static BadPackageData scanForBlackListedActivityApp(Context context, String packageName, Set<PackageData> blackListedActivityPackages)
    {
        PackageInfo pi=null;
        try
        {
            pi=ActivityTools.getPackageInfo(context,packageName, PackageManager.GET_ACTIVITIES);
        }
        catch(PackageManager.NameNotFoundException ex)
        {
            pi=null;
        }

        if(pi==null)
            return null;

        BadPackageData bprd=new BadPackageData(pi.packageName);

        List<ActivityInfo> arrayToRecycle=new ArrayList<ActivityInfo>();

        scanForBlackListedActivityApp(pi, bprd, blackListedActivityPackages, arrayToRecycle);


        return bprd;
    }


    //In setToUpdate we receive a set of BadPackageData ready to be update with newly detected menaces
    public static Set<BadPackageData> scanForSuspiciousPermissionsApps(List<PackageInfo> packagesToSearch, Set<PermissionData> suspiciousPermissions,
                                                                          Set<BadPackageData> setToUpdate)
    {
        BadPackageData bprd=null;

        for(PackageInfo pi : packagesToSearch)
        {
            //Try to update a bad package data if found
            bprd=getBadPackageResultByPackageName(setToUpdate,pi.packageName);

            if(bprd==null)
                bprd=new BadPackageData(pi.packageName);

            scanForSuspiciousPermissionsApp(pi, bprd, suspiciousPermissions);

            //This is only a new menace if we got some permission data
            if(bprd.getPermissionData().size()>0)
                setToUpdate.add(bprd);
        }

        return setToUpdate;
    }

    public static BadPackageData scanForSuspiciousPermissionsApp(PackageInfo pi, BadPackageData bprdToFill,Set<PermissionData> suspiciousPermissions)
    {
        for(PermissionData permData : suspiciousPermissions)
        {
            if(ActivityTools.packageInfoHasPermission(pi, permData.getPermissionName()))
            {
                bprdToFill.addPermissionData(permData);
            }
        }

        return bprdToFill;
    }

    public static BadPackageData scanForSuspiciousPermissionsApp(Context context, String packageName, Set<PermissionData> suspiciousPermissions)
    {
        BadPackageData bprd=null;

        PackageInfo pi=null;
        try
        {
            pi=ActivityTools.getPackageInfo(context,packageName, PackageManager.GET_ACTIVITIES);
        }
        catch(PackageManager.NameNotFoundException ex)
        {
            pi=null;
        }

        if(pi==null)
            return null;

        bprd=new BadPackageData(pi.packageName);

        return scanForSuspiciousPermissionsApp(pi,bprd,suspiciousPermissions);
    }


    public static BadPackageData getBadPackageResultByPackageName(Set<BadPackageData> prd, String packageName)
    {
        BadPackageData result=null;

        for (BadPackageData p : prd)
        {
            if(p.getPackageName().equals(packageName))
            {
                result=p;
                break;
            }
        }

        return result;
    }



    public static Set<GoodPackageResultData> getPackagesByNameFilter(List<PackageInfo> packages, String filter, Set<GoodPackageResultData> result)
    {
        boolean wildcard=false;

        result.clear();

        if(filter.charAt(filter.length()-1)=='*')
            wildcard=true;

        PackageInfo packInfo =null;

        for (int i=0; i < packages.size(); i++)
        {
            packInfo=packages.get(i);

            if(StringTools.stringMatchesMask(packInfo.packageName, filter))
            {
                result.add(new GoodPackageResultData(packInfo));

                //Just one package if we were not using a wildcard
                if (!wildcard)
                    break;
            }
        }

        return result;
    }

    //We will return a list of uniquely named ActivityInfo becase we can't have 2 same activities in a manifeset with same name
    public static  List<ActivityInfo> getActivitiesByNameFilter(PackageInfo pi, String filter, List<ActivityInfo> result)
    {
        result.clear();

        if(pi.activities==null)
            return result;

        boolean wildcard=false;

        if(filter.charAt(filter.length()-1)=='*')
        {
            wildcard=true;
            filter=filter.substring(0,filter.length()-2);
        }
        else
            wildcard=false;

        ActivityInfo activityInfo =null;

        for (int i=0; i < pi.activities.length; i++)
        {
            activityInfo=pi.activities[i];

            if(activityInfo.name.startsWith(filter))
            {
                result.add(activityInfo);
            }
        }

        return result;
    }

    public static Set<BadPackageData> scanInstalledAppsFromGooglePlay(Context context,List<PackageInfo> packagesToSearch, Set<BadPackageData> setToUpdate)
    {
        //Check against whitelist
        for(PackageInfo pi : packagesToSearch)
        {
            if(!ActivityTools.checkIfAppWasInstalledThroughGooglePlay(context,pi.packageName))
            {
                //Update or create new if it does not exist
                BadPackageData bprd=Scanner.getBadPackageResultByPackageName(setToUpdate, pi.packageName);
                if(bprd==null)
                {
                    bprd = new BadPackageData(pi.packageName);
                    setToUpdate.add(bprd);
                }

                bprd.setInstalledThroughGooglePlay(false);
            }
            else
            {
                BadPackageData bprd=Scanner.getBadPackageResultByPackageName(setToUpdate, pi.packageName);
                if(bprd!=null)
                    bprd.setInstalledThroughGooglePlay(true);
            }
        }

        return setToUpdate;
    }


    protected static BadPackageData scanInstalledAppFromGooglePlay(Context context,BadPackageData bprd)
    {
        if(!ActivityTools.checkIfAppWasInstalledThroughGooglePlay(context,bprd.getPackageName()))
        {
            bprd.setInstalledThroughGooglePlay(false);
        }
        else
        {
            bprd.setInstalledThroughGooglePlay(true);
        }

        return bprd;
    }
}
