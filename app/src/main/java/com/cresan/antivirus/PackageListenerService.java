package com.cresan.antivirus;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.IBinder;
import android.util.Log;

import com.tech.applications.coretools.ActivityTools;
import com.tech.applications.coretools.MediaTools;
import com.tech.applications.coretools.NotificationTools;
import com.tech.applications.coretools.advertising.IPackageChangesListener;
import com.tech.applications.coretools.advertising.PackageBroadcastReceiver;

import com.cresan.androidprotector.R;
import com.tech.applications.coretools.time.JSonTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hexdump on 14/01/16.
 */
public class PackageListenerService extends Service
{
    final String _logTag=PackageListenerService.class.getSimpleName();

    PackageBroadcastReceiver _packageBroadcastReceiver;

    Set<PackageData> _whiteListPackages=null;

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
                Intent toExecuteIntent = new Intent(PackageListenerService.this, AntivirusActivity.class);

                String packageName = i.getData().getSchemeSpecificPart();

                String appName = ActivityTools.getAppNameFromPackage(PackageListenerService.this, packageName);

                _loadDataFiles();

                if (_checkIfPackageInWhiteList(packageName, _whiteListPackages))
                    NotificationTools.notificatePush(PackageListenerService.this, 0xFF00, R.drawable.ic_launcher,
                            "Ticker text", appName, "App " + appName + " is a trusted application verified by antivirus.", toExecuteIntent);
                else
                    NotificationTools.notificatePush(PackageListenerService.this, 0xFF00, R.drawable.ic_launcher,
                            "Ticker text", appName, "App installed: Click to scan for menaces", toExecuteIntent);
            }

            public void OnPackageRemoved(Intent intent)
            {
            }
        });

        IntentFilter packageFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_INSTALL");
        packageFilter.addDataScheme("package");
        this.registerReceiver(_packageBroadcastReceiver, packageFilter);
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
        return null;
    }

    private void _loadDataFiles()
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
            String jsonFile=JSonTools.loadJSONFromAsset(this,"whiteList.json");
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
    }

    protected boolean _checkIfPackageInWhiteList(String packageName, Set<PackageData> whiteListPackages)
    {
        for (PackageData packageInfo :  whiteListPackages)
        {
            String packageMask=packageInfo.getPackageName();
            if(MainFragment.packageNameBelongsToPackageMask(packageName,packageMask))
                return true;
        }

        return false;

    }
}
