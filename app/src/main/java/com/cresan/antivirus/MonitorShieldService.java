package com.cresan.antivirus;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.tech.applications.coretools.ActivityTools;
import com.tech.applications.coretools.NotificationTools;
import com.tech.applications.coretools.StringTools;
import com.tech.applications.coretools.advertising.IPackageChangesListener;
import com.tech.applications.coretools.advertising.PackageBroadcastReceiver;

import com.cresan.androidprotector.R;
import com.tech.applications.coretools.JSonTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hexdump on 14/01/16.
 * Based on stack overflow link: http://stackoverflow.com/questions/20594936/communication-between-activity-and-service
 *
 */
public class MonitorShieldService extends Service
{
    final String _logTag=MonitorShieldService.class.getSimpleName();

    private final IBinder _binder=new MonitorShieldLocalBinder();

    PackageBroadcastReceiver _packageBroadcastReceiver;

    Set<PackageData> _whiteListPackages;
    public Set<PackageData> getWhiteListPackages() { return _whiteListPackages; }
    Set<PackageData> _blackListPackages;
    public Set<PackageData> getBlackListPackages(){return _blackListPackages;}
    Set<PackageData> _blackListActivities;
    public Set<PackageData> getBlackListActivities() { return _blackListActivities;}
    Set<PermissionData> _suspiciousPermissions;
    public Set<PermissionData> getSuspiciousPermissions() { return _suspiciousPermissions;}
    UserWhiteList _userWhiteList=null;
    public UserWhiteList getUserWhiteList() { return _userWhiteList;}
    MenacesCacheSet _menacesCacheSet =null;
    public MenacesCacheSet getMenacesCacheList() { return _menacesCacheSet; }

    IClientInterface _clientInterface=null;
    public void registerClient(IClientInterface clientInterface) { _clientInterface=clientInterface;}

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.i("CRESAN","################## Service OnCreate called");

        _packageBroadcastReceiver = new PackageBroadcastReceiver();
        _packageBroadcastReceiver.setPackageBroadcastListener(new IPackageChangesListener()
        {

            public void OnPackageAdded(Intent i)
            {
                Intent toExecuteIntent = new Intent(MonitorShieldService.this, AntivirusActivity.class);

                Intent openAppIntent = getPackageManager().getLaunchIntentForPackage(i.getData().getSchemeSpecificPart());

                String packageName = i.getData().getSchemeSpecificPart();

                String appName = ActivityTools.getAppNameFromPackage(MonitorShieldService.this, packageName);

                if (_checkIfPackageInWhiteList(packageName, _whiteListPackages))
                    NotificationTools.notificatePush(MonitorShieldService.this, 0xFF00, R.drawable.ic_launcher,
                            appName + " is a trusted application.", appName, "App " + appName + " is a trusted application verified by antivirus.", openAppIntent);
                else
                    NotificationTools.notificatePush(MonitorShieldService.this, 0xFF00, R.drawable.ic_launcher,
                            appName + " needs to be scanned.", appName, "App installed: Click to scan for menaces", toExecuteIntent);
            }

            public void OnPackageRemoved(Intent intent)
            {
            }
        });

        IntentFilter packageFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_INSTALL");
        packageFilter.addDataScheme("package");
        this.registerReceiver(_packageBroadcastReceiver, packageFilter);

        _loadDataFiles();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        this.unregisterReceiver(_packageBroadcastReceiver);
        _packageBroadcastReceiver = null;

        Log.d("CRESAN", "################## Service onDestroy command called");
    }

    @Override
    public IBinder onBind(Intent i)
    {
        return _binder;
    }

    //returns the instance of the service
    public class MonitorShieldLocalBinder extends Binder
    {
        public MonitorShieldService getServiceInstance()
        {
            return MonitorShieldService.this;
        }
    }

    public interface IClientInterface
    {
        //Called when a menace is found by the watchdog
        public void onMonitorFoundMenace(BadPackageResultData menace);
        //All packages to scan can be useful if the client wants to do for example some animation to cheat :P
        public void onScanResult(List<PackageInfo> allPacakgesToScan, Set<BadPackageResultData> scanResult);
    }

    /*private void _loadDataFiles()
    {

        if(_whiteListPackages==null)
            _whiteListPackages=new HashSet<PackageData>();
        else
        {
            if(_whiteListPackages.size()>0)
            {
                Log.d("CRESAN", "################# NO NEED TO LOAD WHITELIST...");
                return;
            }
            else
            {
                Log.d("CRESAN", "################# SOMETHING ODD HAPPENED. WHITELISTPACKAGES IS INSTANTIATED BUT NOT HAS ITEMS ?????????...");
            }

        }

        Log.d("CRESAN", "################# LOADING WHITELIST FILE FROM SERVICE");

        //Load WhiteList
        try
        {
            String jsonFile= JSonTools.loadJSONFromAsset(this, "whiteList.json");
            JSONObject obj = new JSONObject(jsonFile);

            JSONArray m_jArry = obj.getJSONArray("data");

            for (int i = 0; i < m_jArry.length(); i++)
            {
                JSONObject temp = m_jArry.getJSONObject(i);
                PackageData pd=new PackageData();
                pd.setPackageName(temp.getString("packageName"));
                _whiteListPackages.add(pd);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }*/

    private void _loadDataFiles()
    {

        _whiteListPackages=new HashSet<PackageData>();
        _blackListPackages=new HashSet<PackageData>();
        _blackListActivities=new HashSet<PackageData>();
        _suspiciousPermissions= new HashSet<PermissionData>();

        //Build/Load user list
        _userWhiteList=new UserWhiteList(this);
        //Build/Load MenaceCache list
        _menacesCacheSet = new MenacesCacheSet(this);

        //Load WhiteList
        try
        {
            String jsonFile= JSonTools.loadJSONFromAsset(this, "whiteList.json");
            JSONObject obj = new JSONObject(jsonFile);

            JSONArray m_jArry = obj.getJSONArray("data");

            for (int i = 0; i < m_jArry.length(); i++)
            {
                JSONObject temp = m_jArry.getJSONObject(i);
                PackageData pd=new PackageData();
                pd.setPackageName(temp.getString("packageName"));
                _whiteListPackages.add(pd);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        //Load blackPackagesList
        try
        {
            String jsonFile= JSonTools.loadJSONFromAsset(this, "blackListPackages.json");
            JSONObject obj = new JSONObject(jsonFile);

            JSONArray m_jArry = obj.getJSONArray("data");

            for (int i = 0; i < m_jArry.length(); i++)
            {
                JSONObject temp = m_jArry.getJSONObject(i);
                PackageData pd=new PackageData();
                pd.setPackageName(temp.getString("packageName"));
                _blackListPackages.add(pd);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        //Load blackActivitiesList
        try
        {
            String jsonFile= JSonTools.loadJSONFromAsset(this, "blackListActivities.json");
            JSONObject obj = new JSONObject(jsonFile);

            JSONArray m_jArry = obj.getJSONArray("data");

            for (int i = 0; i < m_jArry.length(); i++)
            {
                JSONObject temp = m_jArry.getJSONObject(i);
                PackageData pd=new PackageData();
                pd.setPackageName(temp.getString("packageName"));
                _blackListActivities.add(pd);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        //Load permissions data
        try
        {
            String jsonFile= JSonTools.loadJSONFromAsset(this, "permissions.json");
            JSONObject obj = new JSONObject(jsonFile);

            JSONArray m_jArry = obj.getJSONArray("data");

            for (int i = 0; i < m_jArry.length(); i++)
            {
                JSONObject temp = m_jArry.getJSONObject(i);
                PermissionData pd=new PermissionData(temp.getString("permissionName"),temp.getInt("dangerous"));
                _suspiciousPermissions.add(pd);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }

    protected boolean _checkIfPackageInWhiteList(String packageName, Set<PackageData> whiteListPackages)
    {
        for (PackageData packageInfo :  whiteListPackages)
        {
            String packageMask=packageInfo.getPackageName();
            if(StringTools.stringMatchesMask(packageName, packageMask))
                return true;
        }

        return false;
    }

    public void scanFileSystem()
    {
        //Scan installed packages
        List<PackageInfo> allPackages= ActivityTools.getApps(this, PackageManager.GET_ACTIVITIES | PackageManager.GET_PERMISSIONS);
        List<PackageInfo> nonSystemAppsPackages= ActivityTools.getNonSystemApps(this, allPackages);

        //Packages with problems will be stored here
        Set<GoodPackageResultData> tempGoodResults=new HashSet<GoodPackageResultData>();
        Set<BadPackageResultData> tempBadResults=new HashSet<BadPackageResultData>();

        Scanner.scanForWhiteListedApps(nonSystemAppsPackages, _whiteListPackages, tempGoodResults);

        /*Log.d(_logTag, "=====> Showing whitelisted apps");
        for (GoodPackageResultData p : tempGoodResults)
        {
            Log.d(_logTag, p.getPackageName());
        }

        Log.d(_logTag, " ");*/

        List<PackageInfo> potentialBadApps=_removeWhiteListPackagesFromPackageList(nonSystemAppsPackages, _whiteListPackages);
        potentialBadApps=_userWhiteList.removeMyPackagesFromPackageList(potentialBadApps);

        Scanner.scanForBlackListedActivityApps(potentialBadApps, _blackListActivities, tempBadResults);
        Scanner.scanForSuspiciousPermissionsApps(potentialBadApps, _suspiciousPermissions, tempBadResults);
        Scanner.scanInstalledAppsFromGooglePlay(this, potentialBadApps, tempBadResults);

        /*for (BadPackageResultData p : tempBadResults)
        {
            Log.d(_logTag, "======PACKAGE "+p.getPackageName()+" GPlay install: "+p.getInstalledThroughGooglePlay());
            if(p.getActivityData().size()>0)
            {
                Log.d(_logTag, "=========BLACK-ACTIVITIES>");
                for (ActivityData ad : p.getActivityData())
                {
                    Log.d(_logTag, "=============> " + ad.getActivityInfo().name);
                }
            }
            if(p.getPermissionData().size()>0)
            {
                Log.d(_logTag,"=========BAD-PERMISSIONS>");
                for(PermissionData pd : p.getPermissionData())
                {
                    Log.d(_logTag,"=============> "+ pd.getPermissionName());
                }
            }

            Log.d(_logTag," ");
        }

        showResultFragment(new ArrayList<BadPackageResultData>(tempBadResults));*/

        //Pasamos esto por ahora para que no se pete el tema
        List<PackageInfo> _packagesInfo=new ArrayList<PackageInfo>();
        _packagesInfo.add(allPackages.get(0));
        _packagesInfo.add(allPackages.get(1));
        _packagesInfo.add(allPackages.get(2));

        //Merge results with non resolved previous ones and serialize
        _menacesCacheSet.setSetWithBadPackageList(tempBadResults);
        _menacesCacheSet.writeData();

        if(_clientInterface!=null)
            _clientInterface.onScanResult(_packagesInfo,tempBadResults);

        //_startScanningAnimation(_packageInfo,_foundMenaces);

    }

    public void scanApp(String packageName)
    {

        boolean whiteListed=Scanner.isAppWhiteListed(packageName, _whiteListPackages);

        if(whiteListed)
        {

        }
        else
        {
            //We have it in our white package list
            if(_userWhiteList.checkIfPackageInList(packageName))
            {

            }
            else
            {
                PackageInfo pi=null;
                try
                {
                    pi=ActivityTools.getPackageInfo(this,packageName, PackageManager.GET_ACTIVITIES);
                }
                catch(PackageManager.NameNotFoundException ex)
                {
                    pi=null;
                }

                if(pi!=null)
                {
                    BadPackageResultData bpbr=new BadPackageResultData(pi);
                    List<ActivityInfo> recycleList=new ArrayList<ActivityInfo>();
                    Scanner.scanForBlackListedActivityApp(bpbr, _blackListActivities, recycleList);
                    Scanner.scanForSuspiciousPermissionsApp(bpbr, _suspiciousPermissions);
                    Scanner.scanInstalledAppFromGooglePlay(this,bpbr);

                    if(bpbr.isMenace())
                    {
                        PackageData pd=new PackageData();
                        pd.setPackageName(bpbr.getPackageInfo().packageName);
                        _menacesCacheSet.addPackage(pd);
                        _menacesCacheSet.writeData();

                        if(_clientInterface!=null)
                            _clientInterface.onMonitorFoundMenace(bpbr);
                    }
                }
            }
        }
    }

    public Set<BadPackageResultData> getBadPackageResultDataList()
    {
        //Convert menaces found to BadPackageResultDataList
        return null;
    }

    protected List<PackageInfo> _removeWhiteListPackagesFromPackageList(List<PackageInfo> packagesToSearch, Set<PackageData> whiteListPackages)
    {
        boolean found=false;

        List<PackageInfo> trimmedPackageList=new ArrayList<PackageInfo>(packagesToSearch);

        //Check against whitelist
        for(PackageData pd : whiteListPackages)
        {
            PackageInfo p = null;
            int index = 0;
            String packageName = pd.getPackageName();
            found = false;

            while (found == false && index < trimmedPackageList.size())
            {
                p = trimmedPackageList.get(index);
                if (StringTools.stringMatchesMask(p.packageName, pd.getPackageName()))
                {
                    found = true;
                    trimmedPackageList.remove(index);
                }
                else
                    ++index;
            }
        }

        return trimmedPackageList;
    }
}
