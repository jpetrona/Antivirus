package com.cresan.antivirus;

import android.app.Activity;
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
            matches=pd.isPackageInListByName(whiteListPackages,packageName);
            if(matches)
                return true;
        }

        return matches;
    }

    //In setToUpdate we receive a set of BadPackageResultData ready to be update with newly detected menaces
    public static Set<BadPackageResultData> scanForBlackListedActivityApps(List<PackageInfo> packagesToSearch, Set<PackageData> blackListedActivityPackages,
                                                                        Set<BadPackageResultData> setToUpdate)
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
                    BadPackageResultData bprd=getBadPackageResultByPackageName(setToUpdate, pi.packageName);
                    if(bprd==null)
                    {
                        bprd = new BadPackageResultData(pi);
                        setToUpdate.add(bprd);
                    }

                    for(ActivityInfo ai: subResult)
                    {
                        bprd.addActivityData(new ActivityData(ai, 0));
                    }
                }
            }
        }
        return setToUpdate;
    }

    public static BadPackageResultData scanForBlackListedActivityApp(BadPackageResultData bprdToFill,
                                                                     Set<PackageData> blackListedActivityPackages, List<ActivityInfo> arrayToRecycle)

    {
        for(PackageData pd: blackListedActivityPackages)
        {
            //In subResult we have now all the ActivityInfo entries resulting in a menace
            getActivitiesByNameFilter(bprdToFill.getPackageInfo(), pd.getPackageName(), arrayToRecycle);

            //If we found bad activities in the package fill the bad package information into result
            if(arrayToRecycle.size()>0)
            {
                for(ActivityInfo ai: arrayToRecycle)
                {
                    bprdToFill.addActivityData(new ActivityData(ai, 0));
                }
            }
        }

        return bprdToFill;
    }

    public static BadPackageResultData scanForBlackListedActivityApp(Context context, String packageName, Set<PackageData> blackListedActivityPackages)
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

        BadPackageResultData bprd=new BadPackageResultData(pi);

        List<ActivityInfo> arrayToRecycle=new ArrayList<ActivityInfo>();

        scanForBlackListedActivityApp(bprd, blackListedActivityPackages, arrayToRecycle);


        return bprd;
    }


    //In setToUpdate we receive a set of BadPackageResultData ready to be update with newly detected menaces
    public static Set<BadPackageResultData> scanForSuspiciousPermissionsApps(List<PackageInfo> packagesToSearch, Set<PermissionData> suspiciousPermissions,
                                                                          Set<BadPackageResultData> setToUpdate)
    {
        BadPackageResultData bprd=null;

        //Check against whitelist
        for(PackageInfo pi : packagesToSearch)
        {
            bprd=getBadPackageResultByPackageName(setToUpdate,pi.packageName);
            if(bprd==null)
                bprd=new BadPackageResultData(pi);

            scanForSuspiciousPermissionsApp(bprd, suspiciousPermissions);
            setToUpdate.add(bprd);
        }

        return setToUpdate;
    }

    public static BadPackageResultData scanForSuspiciousPermissionsApp(BadPackageResultData bprdToFill,Set<PermissionData> suspiciousPermissions)
    {
        for(PermissionData permData : suspiciousPermissions)
        {
            if(ActivityTools.packageInfoHasPermission(bprdToFill.getPackageInfo(), permData.getPermissionName()))
            {
                bprdToFill.addPermissionData(permData);
            }
        }

        return bprdToFill;
    }

    public static BadPackageResultData scanForSuspiciousPermissionsApp(Context context, String packageName, Set<PermissionData> suspiciousPermissions)
    {
        BadPackageResultData bprd=null;

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

        bprd=new BadPackageResultData(pi);

        return scanForSuspiciousPermissionsApp(bprd,suspiciousPermissions);
    }


    public static BadPackageResultData getBadPackageResultByPackageName(Set<BadPackageResultData> prd, String packageName)
    {
        BadPackageResultData result=null;

        for (BadPackageResultData p : prd)
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

    public static Set<BadPackageResultData> scanInstalledAppsFromGooglePlay(Context context,List<PackageInfo> packagesToSearch, Set<BadPackageResultData> setToUpdate)
    {
        //Check against whitelist
        for(PackageInfo pi : packagesToSearch)
        {
            if(!ActivityTools.checkIfAppWasInstalledThroughGooglePlay(context,pi.packageName))
            {
                //Update or create new if it does not exist
                BadPackageResultData bprd=Scanner.getBadPackageResultByPackageName(setToUpdate, pi.packageName);
                if(bprd==null)
                {
                    bprd = new BadPackageResultData(pi);
                    setToUpdate.add(bprd);
                }

                bprd.setInstalledThroughGooglePlay(false);
            }
            else
            {
                BadPackageResultData bprd=Scanner.getBadPackageResultByPackageName(setToUpdate, pi.packageName);
                if(bprd!=null)
                    bprd.setInstalledThroughGooglePlay(true);
            }
        }

        return setToUpdate;
    }


    protected static BadPackageResultData scanInstalledAppFromGooglePlay(Context context,BadPackageResultData bprd)
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
