package com.cresan.antivirus;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.tech.applications.coretools.ActivityTools;
import com.tech.applications.coretools.NotificationTools;
import com.tech.applications.coretools.advertising.IPackageChangesListener;
import com.tech.applications.coretools.advertising.PackageBroadcastReceiver;

import com.cresan.androidprotector.R;
/**
 * Created by hexdump on 14/01/16.
 */
public class PackageListenerService extends Service
{
    final String _logTag=PackageListenerService.class.getSimpleName();

    PackageBroadcastReceiver _packageBroadcastReceiver;

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.i(_logTag, "===========> Service registered");

        _packageBroadcastReceiver = new PackageBroadcastReceiver();
        _packageBroadcastReceiver.setPackageBroadcastListener(new IPackageChangesListener()
        {

            public void OnPackageAdded(Intent i)
            {
                Intent toExecuteIntent = new Intent(PackageListenerService.this, AntivirusActivity.class);

                String appName= ActivityTools.getAppNameFromPackage(PackageListenerService.this, i.getData().getSchemeSpecificPart());

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

        Log.i(_logTag, "===========> Service stoped");
    }

    @Override
    public IBinder onBind(Intent i)
    {
        return null;
    }

}
