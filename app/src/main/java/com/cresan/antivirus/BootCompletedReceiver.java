package com.cresan.antivirus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tech.applications.coretools.time.ServiceTools;

/**
 * Created by hexdump on 15/01/16.
 */
public class BootCompletedReceiver extends BroadcastReceiver
{
    final String _logTag=BootCompletedReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(_logTag,"======> Received boot device intent");

        if(!ServiceTools.isServiceRunning(context,PackageListenerService.class))
        {
            Log.d(_logTag,"======> BootCompleteReceiver:onReceive: PackageListenerService not running. Starting it...");
            Intent myIntent = new Intent(context, PackageListenerService.class);
            context.startService(myIntent);
        }
        else
        {
            Log.d(_logTag,"======> BootCompleteReceiver:onReceive: PackageListenerService was running. No need to start it again.");
        }
    }
}