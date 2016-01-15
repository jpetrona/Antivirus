package com.cresan.antivirus;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;

import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.gelitenight.waveview.library.WaveView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.liulishuo.magicprogresswidget.MagicProgressBar;
import com.tech.applications.advertising.adnetworks.AdsCore;
import com.tech.applications.advertising.adnetworks.AdvertFragmentActivity;
import com.tech.applications.advertising.adnetworks.AdvertListener;
import com.tech.applications.coretools.ActivityTools;
import com.tech.applications.coretools.AdvertisingTools;
import com.tech.applications.coretools.BatteryData;
import com.tech.applications.coretools.BatteryTools;
import com.tech.applications.coretools.NetworkTools;
import com.tech.applications.coretools.NotificationTools;
import com.tech.applications.coretools.SerializationTools;
import com.tech.applications.coretools.advertising.IPackageChangesListener;
import com.tech.applications.coretools.advertising.PackageBroadcastReceiver;
import com.tech.applications.coretools.time.PausableCountDownTimer;

import com.cresan.androidprotector.R;
import com.tech.applications.coretools.time.ServiceTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AntivirusActivity extends AdvertFragmentActivity
{
    Set<PackageData> _whiteListPackages;
    public Set<PackageData> getWhiteListPackages() { return _whiteListPackages; }
    Set<PackageData> _blackListPackages;
    public Set<PackageData> getBlackListPackages(){return _blackListPackages;}
    Set<PackageData> _blackListActivities;
    public Set<PackageData> getBlackListActivities() { return _blackListActivities;}

    final String bannerAdUnit="";
	final String interstitialAdUnit="";

	String _logTag=AntivirusActivity.class.getSimpleName();

	AdvertListener _inMiddleAdListener=new AdvertListener() 
	{
		
		@Override
		public void adShown(HashMap<String, Object> appData)
		{
			android.util.Log.i(_logTag,"Ad shown....");
		}
		
		@Override
		public void adHidden(HashMap<String, Object> appData) 
		{
			android.util.Log.i(_logTag,"Ad hidden....");
			runOnUiThread(new Runnable() 
			{
				
				@Override
				public void run() 
				{
					//_continueCalibrationAfterAd();
				}
			});
		}
		
		@Override
		public void adFailed(HashMap<String, Object> appData) 
		{
			android.util.Log.i(_logTag,"Ad failed....");
			runOnUiThread(new Runnable() 
			{
				
				@Override
				public void run() 
				{
					/*_continueCalibrationAfterAd();*/
				}
			});
		}
		
		@Override
		public void returnedToAppAfterShoingAd(HashMap<String,Object> appData)
		{
			android.util.Log.i(_logTag,"Returned to the ap after showing ad....");
			runOnUiThread(new Runnable() 
			{
				
				@Override
				public void run() 
				{
					/*_continueCalibrationAfterAd();*/
				}
			});
		}

		@Override
		public void adClicked(HashMap<String, Object> appData) {
			// TODO Auto-generated method stub
			
		}
	};
	
	
	AppData _data=null;
	
	public void onCreate(Bundle paramBundle)
    {
		//Log.i(_logTag, "============= YEAH ACTIVITY RECREATED ============");
		super.onCreate(paramBundle);
        
	    setContentView(R.layout.activity_main);

		//Start service
		if(!ServiceTools.isServiceRunning(this,PackageListenerService.class))
		{
            Log.d(_logTag,"=====> AntivirusActivity:onCreate: Starting PackageListenerService because it was not running.");
            Intent i = new Intent(this, PackageListenerService.class);
			startService(i);
		}
        else
            Log.d(_logTag,"=====> AntivirusActivity:onCreate: No need to start PackageListenerService because it as running previously.");


        Fragment newFragment = new MainFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.container, newFragment).commit();

        //Configure Ads
		if(!NetworkTools.isNetworkAvailable(this))
	    {	
	    	showNoInetDialog();
	    	return;
	    }

	    AdsCore ac=getAdsCore();
		//ac.initAirpush();
	 	//ac.initStartApp("110020761","204682277");
	 	ac.initAdMob(interstitialAdUnit);
		//ac.initVungle("5364b2d90471b1cf05000071");
	 	
	 	AdView adView= new AdView(AntivirusActivity.this);
    	adView.setAdSize(AdSize.SMART_BANNER);
    	adView.setAdUnitId(bannerAdUnit);

    	ViewGroup vg= (ViewGroup)findViewById(R.id.topcontainer);
	    vg.addView(adView);

        AdRequest.Builder builder = new AdRequest.Builder();

        AdRequest adRequest = builder.build();
        adView.loadAd(adRequest);


	    _data=_deserializeAppData();

        _loadDataFiles();
    }

	void _loadDataFiles()
    {
        _whiteListPackages=new HashSet<PackageData>();
        _blackListPackages=new HashSet<PackageData>();
        _blackListActivities=new HashSet<PackageData>();

        //Load WhiteList
        try
        {
            String jsonFile=loadJSONFromAsset("whiteList.json");
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
            String jsonFile=loadJSONFromAsset("blackListPackages.json");
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
            String jsonFile=loadJSONFromAsset("blackListActivities.json");
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
    }

    public String loadJSONFromAsset(String file)
    {
        String json = null;
        try
        {
            InputStream is = this.getAssets().open(file);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

	public void onStart()
	{
		super.onStart();

	}

	protected AppData _deserializeAppData()
	{
		AppData data=SerializationTools.deserializeFromSharedPrefs(this, "bc_data");
		
		if(data==null)
			data=new AppData();
		
		return data;
	}

    public void showNoInetDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.information);

        builder.setMessage(R.string.no_inet_no_app);
        builder.setCancelable(false);
        builder.setInverseBackgroundForced(true);

        builder.setPositiveButton(R.string.quit_app,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        finish();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

	protected void _showTimedDialog(AlertDialog dialog, boolean negative, boolean blockPositive, boolean blockNegative)
	{
		dialog.show();
		
		Handler handler = new Handler();
		
		// Access the button and set it to invisible
		final Button posButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		if(blockPositive)
			posButton.setEnabled(false);
				
		final Button negButton=dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
		final boolean finalNegative=negative;
		
		if(negative && blockNegative)
		{	
			negButton.setEnabled(false);
		}
		
		// Post the task to set it visible in 5000ms  
		handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if (posButton != null)
                    posButton.setEnabled(true);
                if (finalNegative)
                    negButton.setEnabled(true);
            }
        }, 20000);
	}


	void _showVoteUs()
	{
		if (!_data.getVoted())
		{
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

			alertDialogBuilder.setTitle(getResources().getString(R.string.vote));

			// set dialog message
			alertDialogBuilder
					.setMessage(getResources().getString(R.string.gplay_vote))
					.setCancelable(false)
					.setPositiveButton(getResources().getString(android.R.string.yes), new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							//FlurryAgent.logEvent("WillVote");
							//resetProcess();
							final String appName = ActivityTools.getPackageName(AntivirusActivity.this);
							AdvertisingTools.openMarketURL(AntivirusActivity.this, "market://details?id=" + appName, "http://play.google.com/store/apps/details?id=" + appName);

							_data.setVoted(true);
							_data.serialize(AntivirusActivity.this);

							//_showDialogCalibrationFinished(_isRoot);
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							// if this button is clicked, just close
							// the dialog box and do nothing
							//resetProcess();
							//FlurryAgent.logEvent("NoVote");
							//_showDialogCalibrationFinished(_isRoot);
						}
					}).show();
		}
		else
		{
			//_showDialogCalibrationFinished(_isRoot);
		}

	}

	public void slideInFragment(Fragment newFragment)
	{
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);

		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack if needed
		transaction.replace(android.R.id.tabcontent, newFragment);
		transaction.addToBackStack(null);

		// Commit the transaction
		transaction.commit();
	}
}
