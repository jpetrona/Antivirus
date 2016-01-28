package com.cresan.antivirus;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

import com.cresan.androidprotector.R;
import com.tech.applications.coretools.ServiceTools;

public class AntivirusActivity extends AdvertFragmentActivity implements MonitorShieldService.IClientInterface
{
    public static final String kMainFragmentTag="MainFragmentTag";
    public static final String kResultFragmentTag="ResultFragmentTag";
    public static final String kInfoFragmnetTag="InfoFragmentTag";
    public static final String kIgnoredFragmentTag="IgnoredFragmentTag";

    public MainFragment getMainFragment()
    {
        FragmentManager fm= getSupportFragmentManager();
        MainFragment f= (MainFragment) fm.findFragmentByTag(kMainFragmentTag);

        if(f==null)
            return new MainFragment();
        else
            return f;
    }

    public ResultsFragment getResultFragment()
    {
        FragmentManager fm= getSupportFragmentManager();
        ResultsFragment f= (ResultsFragment) fm.findFragmentByTag(kResultFragmentTag);

        if(f==null)
            return new ResultsFragment();
        else
            return f;
    }

    public InfoAppFragment getInfoFragment()
    {
        FragmentManager fm= getSupportFragmentManager();
        InfoAppFragment f= (InfoAppFragment) fm.findFragmentByTag(kInfoFragmnetTag);

        if(f==null)
            return new InfoAppFragment();
        else
            return f;
    }

    public IgnoredListFragment getIgnoredFragment()
    {
        FragmentManager fm= getSupportFragmentManager();
        IgnoredListFragment f= (IgnoredListFragment) fm.findFragmentByTag(kIgnoredFragmentTag);

        if(f==null)
            return new IgnoredListFragment();
        else
            return f;
    }

    public UserWhiteList getUserWhiteList()
    {
        return _serviceInstance.getUserWhiteList();
    }
    public Set<BadPackageData> getBadResultPackageDataFromMenaceSet() { return _serviceInstance.getMenacesCacheSet().getSet(); }
    public MenacesCacheSet getMenacesCacheSet() { return _serviceInstance.getMenacesCacheSet(); }

    final String bannerAdUnit="";
	final String interstitialAdUnit="";

	String _logTag=AntivirusActivity.class.getSimpleName();

    MonitorShieldService _serviceInstance=null;




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

    private ServiceConnection _serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            Log.d(_logTag, "OOOOOOOOOOOOOOOOOO> onServiceConnected called");

            MonitorShieldService.MonitorShieldLocalBinder binder = (MonitorShieldService.MonitorShieldLocalBinder) service;
            _serviceInstance = binder.getServiceInstance(); //Get getInstance of your service!
            _serviceInstance.registerClient(AntivirusActivity.this); //Activity register in the service as client for callabcks!

            //Now that service is active run fragment to init it
            slideInFragment(AntivirusActivity.kMainFragmentTag);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            Log.d(_logTag, "OOOOOOOOOOOOOOOOOO> onServiceDisconnected called");
            _serviceInstance=null;
        }
    };


    //Registration for any activity inside our app to listen to service calls
    MonitorShieldService.IClientInterface _appMonitorServiceListener=null;
    public void setMonitorServiceListener(MonitorShieldService.IClientInterface listener) { _appMonitorServiceListener=listener;}

    //Called when a menace is found by the watchdog
    public void onMonitorFoundMenace(BadPackageData menace)
    {
        if(_appMonitorServiceListener!=null)
            _appMonitorServiceListener.onMonitorFoundMenace(menace);
    }
    public void onScanResult(List<PackageInfo> allPacakgesToScan,Set<BadPackageData> scanResult)
    {
        if(_appMonitorServiceListener!=null)
            _appMonitorServiceListener.onScanResult(allPacakgesToScan, scanResult);
    }

    public void startMonitorScan(MonitorShieldService.IClientInterface listener)
    {
        _appMonitorServiceListener=listener;
        if(_serviceInstance!=null)
            _serviceInstance.scanFileSystem();
    }

    public AppData getAppData()
    {
        Log.d(_logTag,"OOOOOOOOOOOOOOOOOOO> "+"AntivirusActivity:getAppData: Usando app data");
        return AppData.getInstance(this);
    }
	
	public void onCreate(Bundle paramBundle)
    {
		//Log.i(_logTag, "============= YEAH ACTIVITY RECREATED ============");
		super.onCreate(paramBundle);
        makeActionOverflowMenuShown();
	    setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		//Start service
		if(!ServiceTools.isServiceRunning(this,MonitorShieldService.class))
		{
            Log.d(_logTag, "=====> AntivirusActivity:onCreate: Starting MonitorShieldService because it was not running.");
            Intent i = new Intent(this, MonitorShieldService.class);
            startService(i);

            //Bind to service
            bindService(i,_serviceConnection, Context.BIND_AUTO_CREATE);
		}
        else
        {
            Log.d(_logTag, "=====> AntivirusActivity:onCreate: No need to start MonitorShieldService because it as running previously.");
            Intent i = new Intent(this, MonitorShieldService.class);

            //Bind to service
            bindService(i,_serviceConnection, Context.BIND_AUTO_CREATE);
        }


        android.support.v7.app.ActionBar bar=getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);


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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                _handleBackButton();
                return true;
            case R.id.ignoredListButton:

                UserWhiteList userWhiteList=getUserWhiteList();
                Set<BadPackageData> packageData =  userWhiteList.getSet();
                showIgnoredFragment(new ArrayList<BadPackageData>(packageData));

                Log.d("ign", "IGNORED BUTTON MENU");
                return true;
            case R.id.RateUs:
                Log.d("ign", "RATE US BUTTON MENU");
            return true;
            case R.id.information:
                Log.d("ign", "INFORMATION BUTTON MENU");
            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    void showIgnoredFragment(List<BadPackageData> userWhiteList)
    {
        IgnoredListFragment newFragment= (IgnoredListFragment) this.slideInFragment(AntivirusActivity.kIgnoredFragmentTag);
        newFragment.setData(userWhiteList);
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

    public void onStart()
	{
		super.onStart();

	}

	/*protected AppData _deserializeAppData()
	{
		AppData data=SerializationTools.deserializeFromDataFolder(this,_data.filePath);
		
		if(data==null)
			data=new AppData();
		
		return data;
	}*/

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
		final AppData appData=AppData.getInstance(this);
        if (appData.getVoted())
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

                            appData.setVoted(true);
                            appData.serialize(AntivirusActivity.this);

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

	public Fragment slideInFragment(String fragmentId)
	{
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,R.anim.slide_in_left,R.anim.slide_out_right);

		Fragment f=null;
        switch(fragmentId)
        {
            case kMainFragmentTag:
                f=getMainFragment();
                break;
            case kInfoFragmnetTag:
                f=getInfoFragment();
                break;
            case kResultFragmentTag:
                f=getResultFragment();
                break;
            case kIgnoredFragmentTag:
                f=getIgnoredFragment();
                break;
            default:
        }


		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack if needed
		transaction.replace(android.R.id.content, f, fragmentId);
		transaction.addToBackStack(null);

		// Commit the transaction
		transaction.commit();

        return f;
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
