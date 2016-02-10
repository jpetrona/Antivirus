package com.cresan.antivirus;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

import org.joda.time.DateTime;
import org.joda.time.Days;

public class AntivirusActivity extends AdvertFragmentActivity implements MonitorShieldService.IClientInterface
{
    public static final String kMainFragmentTag="MainFragmentTag";
    public static final String kResultFragmentTag="ResultFragmentTag";
    public static final String kInfoFragmnetTag="InfoFragmentTag";
    public static final String kIgnoredFragmentTag="IgnoredFragmentTag";

    public static final String kBannerAdUnit="ca-app-pub-3912218987594825/9095635994";
    public static final String kInterstitialAdUnit="ca-app-pub-3912218987594825/4525835590";

    Menu _menu=null;
    public Menu getMenu() {return _menu;}

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


    public boolean canShowAd()
    {
        AppData appData=getAppData();

        DateTime today=new DateTime();
        int diffDays= Days.daysBetween(appData.getLastAdDate(),new DateTime()).getDays();

        if(diffDays>0)
        {
            appData.setLastAdDate(today);
            appData.serialize(this);
            return true;
        }
        else
            return false;
    }



    public UserWhiteList getUserWhiteList()
    {
        return _serviceInstance.getUserWhiteList();
    }
    public Set<IProblem> getProblemsFromMenaceSet() { return _serviceInstance.getMenacesCacheSet().getSet(); }
    public MenacesCacheSet getMenacesCacheSet() { return _serviceInstance.getMenacesCacheSet(); }
    public void updateMenacesAndWhiteUserList()
    {
        //Remove not existent problems
        ProblemsDataSetTools.removeNotExistingProblems(this, getUserWhiteList());
        ProblemsDataSetTools.removeNotExistingProblems(this, getMenacesCacheSet());

        //Add existent system problems
        Scanner.scanSystemProblems(this, getUserWhiteList(), getMenacesCacheSet().getSet());
    }


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

    boolean _bound=false;
    private ServiceConnection _serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            _bound=true;

            Log.d(_logTag, "OOOOOOOOOOOOOOOOOO> onServiceConnected called");

            MonitorShieldService.MonitorShieldLocalBinder binder = (MonitorShieldService.MonitorShieldLocalBinder) service;
            _serviceInstance = binder.getServiceInstance(); //Get getInstance of your service!

            if(_serviceInstance!=null)
                new Exception("Service instance is null. At it can't be!!!!");

            _serviceInstance.registerClient(AntivirusActivity.this); //Activity register in the service as client for callabcks!

            //Now that service is active run fragment to init it
            FragmentManager fm=getSupportFragmentManager();
            if(fm.getBackStackEntryCount()<=0)
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
    public void onMonitorFoundMenace(IProblem menace)
    {
        if(_appMonitorServiceListener!=null)
            _appMonitorServiceListener.onMonitorFoundMenace(menace);
    }
    public void onScanResult(List<PackageInfo> allPacakgesToScan,Set<IProblem> scanResult)
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
		Log.i(_logTag, "============= ONCREATE HAS BEEN CALLED============");

        super.onCreate(/*paramBundle*/null);

        //Restart app if someone kill it
        /*if(paramBundle!=null)
        {
            FragmentManager fm=getSupportFragmentManager();
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            Intent i = new Intent(this, MonitorShieldService.class);
            stopService(i);

            finish();
            Intent intent = new Intent(this, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return;
        }*/

        makeActionOverflowMenuShown();
	    setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getSupportActionBar();

		android.support.v7.app.ActionBar bar=getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);


        //Configure Ads
		if(!NetworkTools.isNetworkAvailable(this))
	    {
	    	showNoInetDialog();
	    	return;
	    }

	    AdsCore ac=getAdsCore();
	 	
        List<String> testDevices=new ArrayList<String>();
        //testDevices.add("4DDC4FAE98222F010D35E8EA7A6E4E34");
        //testDevices.add("4F5A2126D375B24EB67CF38C123A0CAF");
        ac.initAdMob(kInterstitialAdUnit,testDevices);

	 	AdView adView= new AdView(AntivirusActivity.this);
    	adView.setAdSize(AdSize.SMART_BANNER);
    	adView.setAdUnitId(kBannerAdUnit);

    	ViewGroup vg= (ViewGroup)findViewById(R.id.topcontainer);
	    vg.addView(adView);

        AdRequest.Builder builder = new AdRequest.Builder();
        //builder.addTestDevice("4DDC4FAE98222F010D35E8EA7A6E4E34");
        //builder.addTestDevice("4F5A2126D375B24EB67CF38C123A0CAF");
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
                showIgnoredFragment(userWhiteList);
                Log.d("ign", "IGNORED BUTTON MENU");
                return true;
            case R.id.RateUs:
                _showVoteUs();
                Log.d("ign", "RATE US BUTTON MENU");
            return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void showIgnoredFragment(UserWhiteList userWhiteList)
    {
        //Create list of apps that are whitelisted and installed
        List<IProblem> ignoredAppsInstalledOnSystem=IgnoredListFragment.getExistingProblems(this, new ArrayList<IProblem>(userWhiteList.getSet()));


        if(ignoredAppsInstalledOnSystem.size() > 0)
        {
            IgnoredListFragment newFragment= (IgnoredListFragment) this.slideInFragment(AntivirusActivity.kIgnoredFragmentTag);
            if(newFragment!=null)
                newFragment.setData(this, userWhiteList);
        }
        else
        {
            new AlertDialog.Builder(this)
                    .setTitle(this.getString(R.string.warning))
                    .setMessage(this.getString(R.string.igonred_message_dialog))
                    .setPositiveButton(this.getString(R.string.accept_eula), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    }).show();
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
        if (!appData.getVoted())
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
		Fragment fmt= getSupportFragmentManager().findFragmentByTag(fragmentId);
        if(fmt!=null && fmt.isVisible())
            return null;

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
		transaction.replace(R.id.container, f, fragmentId);
		transaction.addToBackStack(null);

		// Commit the transaction (Si no lo hago con commitAllowingStateLoss se peta la app
        // https://www.google.es/search?q=android+create+list+inplace&ie=utf-8&oe=utf-8&gws_rd=cr&ei=1J20VrXjFIScUYuXt6gI
		transaction.commitAllowingStateLoss();

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
    public void onStart()
    {
        Log.i(_logTag, "============= ONSTART HAS BEEN CALLED============");
        super.onStart();

        //Start service
        if(!ServiceTools.isServiceRunning(this,MonitorShieldService.class))
        {
            Log.d(_logTag, "=====> AntivirusActivity:onCreate: Starting MonitorShieldService because it was not running.");
            Intent i = new Intent(this, MonitorShieldService.class);
            startService(i);

            //Bind to service
            bindService(i, _serviceConnection, Context.BIND_AUTO_CREATE);
        }
        else
        {
            Log.d(_logTag, "=====> AntivirusActivity:onCreate: No need to start MonitorShieldService because it as running previously.");
            Intent i = new Intent(this, MonitorShieldService.class);

            //Bind to service
            bindService(i, _serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onStop()
    {
        Log.i(_logTag, "============= ONSTOP HAS BEEN CALLED============");
        super.onStop();
        // Unbind from the service
        if (_bound && _serviceConnection!=null)
        {
            unbindService(_serviceConnection);
            _bound = false;
        }
    }

    @Override
    public void onDestroy()
    {
        Log.i(_logTag, "============= ONDESTROY HAS BEEN CALLED============");
        super.onDestroy();

    }

    @Override
    public void onResume()
    {
        Log.i(_logTag, "============= ONRESUME HAS BEEN CALLED============");
        super.onResume();
    }


        @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_layout, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        //menu.setGroupVisible(0,false);
        _menu=menu;
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
