package com.cresan.antivirus;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.tech.applications.advertising.adnetworks.AdsCore;
import com.tech.applications.advertising.adnetworks.AdvertFragmentActivity;
import com.tech.applications.advertising.adnetworks.AdvertListener;
import com.tech.applications.coretools.ActivityTools;
import com.tech.applications.coretools.AdvertisingTools;
import com.tech.applications.coretools.NetworkTools;
import com.tech.applications.coretools.SerializationTools;

import com.tech.applications.coretools.JSonTools;

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
	Set<PermissionData> _suspiciousPermissions;
	public Set<PermissionData> getSuspiciousPermissions() { return _suspiciousPermissions;}

    UserWhiteList _userWhiteList=null;
    public UserWhiteList getUserWhiteList() { return _userWhiteList;}

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
        makeActionOverflowMenuShown();
	    setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//Start service
		if(!ServiceTools.isServiceRunning(this,PackageListenerService.class))
		{
            String jsonFile= JSonTools.loadJSONFromAsset(this, "whiteList.json");
            Log.d(_logTag,"=====> AntivirusActivity:onCreate: Starting PackageListenerService because it was not running.");
            Intent i = new Intent(this, PackageListenerService.class);
			i.putExtra("whitelist",jsonFile);
            startService(i);
		}
        else
            Log.d(_logTag,"=====> AntivirusActivity:onCreate: No need to start PackageListenerService because it as running previously.");


        android.support.v7.app.ActionBar bar=getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);


        slideInFragment(new MainFragment());

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                _handleBackButton();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {
        _handleBackButton();
    }

    void _handleBackButton()
    {
        goBack();

    }


	void _loadDataFiles()
    {
        _whiteListPackages=new HashSet<PackageData>();
        _blackListPackages=new HashSet<PackageData>();
        _blackListActivities=new HashSet<PackageData>();
		_suspiciousPermissions= new HashSet<PermissionData>();

        //Build user list
        _userWhiteList=new UserWhiteList(this);

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
		transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,R.anim.slide_in_left,R.anim.slide_out_right);

		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack if needed
		transaction.replace(android.R.id.content, newFragment);
		transaction.addToBackStack(null);

		// Commit the transaction
		transaction.commit();
	}

    public void goBack()
    {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 1)
        {
            fm.popBackStack();

        }
        else
        {   //No tenemos fragments en el stack asi qeu a tomar por culo la app
            //super.onBackPressed();

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.warning))
                    .setMessage(getString(R.string.dialog_message_exit))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            finish();

                        }
                    }).setNegativeButton("no", new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                }
            }).show();


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_layout, menu);
        return true;

    }


    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.ignoredListButton:

                Log.d("ign","IGNORED BUTTON MENU");
                return true;
            case R.id.RateUs:
                Log.d("ign","RATE US BUTTON MENU");
                return true;
            case R.id.information:
                Log.d("ign","INFORMATION BUTTON MENU");
                return true;
            default:
                return super.onContextItemSelected(item);
        }


    }

    

    private void makeActionOverflowMenuShown() {
        //devices with hardware menu button (e.g. Samsung Note) don't show action overflow menu
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            Log.d("TAG", e.getLocalizedMessage());
        }
    }
}
