package com.cresan.antivirus;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cresan.androidprotector.R;
import com.gelitenight.waveview.library.WaveView;
import com.liulishuo.magicprogresswidget.MagicProgressBar;
import com.tech.applications.coretools.ActivityTools;
import com.tech.applications.coretools.BatteryData;
import com.tech.applications.coretools.BatteryTools;
import com.tech.applications.coretools.NetworkTools;
import com.tech.applications.coretools.time.PausableCountDownTimer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by hexdump on 02/11/15.
 */

public class MainFragment extends Fragment
{
    final String _logTag=this.getClass().getSimpleName();
    final Random _random=new Random();

    AntivirusActivity getMainActivity() {return (AntivirusActivity) getActivity();}
    //AppData getAppData() { return getMainActivity().getAppData();}

    Button _runAntivirusNow=null;

    //Scrollable data chunk data
    ImageView _progressPanelIconImageView;
    TextView _progressPanelTextView;
    MagicProgressBar _progressPanelprogressBar;
    RelativeLayout _progressContainer;
    RelativeLayout _buttonContainer;
    RelativeLayout _informationContainer;
    RelativeLayout _superContainer;

    PausableCountDownTimer cdTimer=null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.main_fragment, container, false);

        _setupFragment(rootView);

        return rootView;
    }



    protected void _setupFragment(View view)
    {
        AntivirusActivity ac=getMainActivity();
        _progressPanelIconImageView =(ImageView)ac.findViewById(R.id.progressPanelIconImageView);
        _progressPanelTextView =(TextView)ac.findViewById(R.id.progressPanelTextView);;
        _progressPanelprogressBar =(MagicProgressBar)ac.findViewById(R.id.progressPanelProgressBar);
        _buttonContainer=(RelativeLayout)ac.findViewById(R.id.buttonLayout);
        _progressContainer=(RelativeLayout)ac.findViewById(R.id.progressPanel);
        _informationContainer=(RelativeLayout)ac.findViewById(R.id.informationPanel);
        _superContainer=(RelativeLayout)ac.findViewById(R.id.superContainer);

        _runAntivirusNow=(Button)view.findViewById(R.id.runAntivirusNow);
        _runAntivirusNow.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {

                if(!NetworkTools.isNetworkAvailable(getMainActivity()))
                {
                    getMainActivity().showNoInetDialog();
                    return;
                }

                _scanFileSystem();

            }
        });

        //Set form data
        BatteryData bd = BatteryTools.getBatteryData(getMainActivity());

        final WaveView waveView = (WaveView) view.findViewById(R.id.wave);
        waveView.setWaterLevelRatio(bd.getLevelPercent() / 100.0f);
        waveView.setBorder(15, ContextCompat.getColor(getMainActivity(), R.color.wave_widget_stroke));
        waveView.setText1Color(ContextCompat.getColor(getMainActivity(), android.R.color.white));
        waveView.setShowWave(true);
        waveView.setWaveColor(
                ContextCompat.getColor(getMainActivity(), R.color.wave_starting_wave_color),
                ContextCompat.getColor(getMainActivity(), R.color.wave_widget_back_wave),
                ContextCompat.getColor(getMainActivity(), R.color.wave_starting_wave_color),
                ContextCompat.getColor(getMainActivity(), R.color.wave_widget_front_wave));
        waveView.startAnimation(1000);
        waveView.setText1(""+bd.getLevelPercent()+"%");
        TextView tv = (TextView) view.findViewById(R.id.voltageValue);
        DecimalFormat df = new DecimalFormat("0.00");
        df.setMaximumFractionDigits(2);
        tv.setText(df.format(bd.getVoltage()) + " v");
        tv = (TextView) view.findViewById(R.id.temperatureValue);
        df = new DecimalFormat("0.0");
        df.setMaximumFractionDigits(1);
        tv.setText(df.format(bd.getTemperature()) + "º");
    }


    protected void _scanFileSystem()
    {
        //Scan installed packages
        List<PackageInfo> allPackages= ActivityTools.getApps(getMainActivity(), PackageManager.GET_ACTIVITIES | PackageManager.GET_PERMISSIONS);
        List<PackageInfo> nonSystemAppsPackages= ActivityTools.getNonSystemApps(getMainActivity(), allPackages);

        List<GoodPackageResultData> goodPackageResultData =new ArrayList<GoodPackageResultData>();

        Set<PackageData> whiteListPackages=getMainActivity().getWhiteListPackages();
        Set<PackageData> blackListPackages=getMainActivity().getBlackListPackages();
        Set<PackageData> blackListActivities=getMainActivity().getBlackListActivities();
        Set<PermissionData> suspiciousPermissions=getMainActivity().getSuspiciousPermissions();

        //Packages with problems will be stored here
        Set<GoodPackageResultData> tempGoodResults=new HashSet<GoodPackageResultData>();
        Set<BadPackageResultData> tempBadResults=new HashSet<BadPackageResultData>();

        _scanForWhiteListedApps(nonSystemAppsPackages, whiteListPackages, tempGoodResults);

        Log.d(_logTag, "=====> Showing whitelisted apps");
        for (GoodPackageResultData p : tempGoodResults)
        {
            Log.d(_logTag, p.getPackageName());
        }

        Log.d(_logTag, " ");

        _scanForBlackListedActivityApps(nonSystemAppsPackages, blackListActivities, tempBadResults);
        _scanForSuspiciousPermissionsApps(nonSystemAppsPackages, suspiciousPermissions, tempBadResults);
        _fillInstalledFromGooglePlay(tempBadResults);

        for (BadPackageResultData p : tempBadResults)
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

        /*Log.d(_logTag, "=====> Showing blacklisted activities");
        _scanForBlackListedActivityApps(nonSystemAppsPackages, blackListActivities, tempBadResults);
        for (BadPackageResultData p : tempBadResults)
        {
            Log.d(_logTag, p.getPackageName());
        }

        Log.d(_logTag, "=====> Showing activities with suspicious permissions");
        _scanForSuspiciousPermissionsApps(nonSystemAppsPackages, suspiciousPermissions, tempBadResults);
        for (BadPackageResultData p : tempBadResults)
        {
            Log.d(_logTag,"=====> "+ p.getPackageName());
            for(PermissionData pd : p.getPermissionData())
            {
                Log.d(_logTag,"=========> "+ pd.getPermissionName());
            }
        }*/
    }

    protected Set<BadPackageResultData> _fillInstalledFromGooglePlay(Set<BadPackageResultData> prd)
    {
        for (BadPackageResultData p : prd)
        {
            if(ActivityTools.checkIfAppWasInstalledThroughGooglePlay(getActivity(),p.getPackageInfo().packageName))
            {
                p.setInstalledThroughGooglePlay(true);
            }
            else
                p.setInstalledThroughGooglePlay(false);
        }

        return prd;
    }

    protected BadPackageResultData getBadPackageResultByPackageName(Set<BadPackageResultData> prd, String packageName)
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

    protected Set<GoodPackageResultData> _scanForWhiteListedApps(List<PackageInfo> packagesToSearch, Set<PackageData> whiteListPackages,
                                                              Set<GoodPackageResultData> result)
    {
        Set<GoodPackageResultData> subResult=new HashSet<GoodPackageResultData>();

        //Check against whitelist
        for(PackageData pd : whiteListPackages)
        {
            _getPackagesByNameFilter(packagesToSearch, pd.getPackageName(), subResult);

            result.addAll(subResult);
        }

        return result;
    }

    //In setToUpdate we receive a set of BadPackageResultData ready to be update with newly detected menaces
    protected Set<BadPackageResultData> _scanForBlackListedActivityApps(List<PackageInfo> packagesToSearch, Set<PackageData> blackListedActivityPackages,
                                                                     Set<BadPackageResultData> setToUpdate)
    {
        List<ActivityInfo> subResult=new ArrayList<ActivityInfo>();

        ActivityInfo[] activities;

        //Check against black listed activity apps
        for(PackageData pd : blackListedActivityPackages)
        {
            for(PackageInfo pi: packagesToSearch)
            {
                //Update or create new if it does not exist
                BadPackageResultData bprd=getBadPackageResultByPackageName(setToUpdate, pi.packageName);
                if(bprd==null)
                {
                    bprd = new BadPackageResultData(pi);
                    setToUpdate.add(bprd);
                }

                //In subResult we have now all the ActivityInfo entries resulting in a menace
                _getActivitiesByNameFilter(pi, pd.getPackageName(), subResult);

                //If we found bad activities in the package fill the bad package information into result
                if(subResult.size()>0)
                {
                    for(ActivityInfo ai: subResult)
                    {
                        bprd.addActivityData(new ActivityData(ai, 0));
                    }
                }

            }
        }

        return setToUpdate;
    }

    //In setToUpdate we receive a set of BadPackageResultData ready to be update with newly detected menaces
    protected Set<BadPackageResultData> _scanForSuspiciousPermissionsApps(List<PackageInfo> packagesToSearch, Set<PermissionData> suspiciousPermissions,
                                                                        Set<BadPackageResultData> setToUpdate)
    {
        //Check against whitelist
        for(PackageInfo pi : packagesToSearch)
        {
            //Update or create new if it does not exist
            BadPackageResultData bprd=getBadPackageResultByPackageName(setToUpdate, pi.packageName);
            if(bprd==null)
            {
                bprd = new BadPackageResultData(pi);
                setToUpdate.add(bprd);
            }

            for(PermissionData permData : suspiciousPermissions)
            {
                if(ActivityTools.packageInfoHasPermission(pi, permData.getPermissionName()))
                {
                    bprd.addPermissionData(permData);
                }
            }
        }

        return setToUpdate;
    }


    Set<GoodPackageResultData> _getPackagesByNameFilter(List<PackageInfo> packages, String filter, Set<GoodPackageResultData> result)
    {
        boolean wildcard=false;

        result.clear();

        if(filter.charAt(filter.length()-1)=='*')
        {
            wildcard=true;
            filter=filter.substring(0,filter.length()-2);
        }
        else
            wildcard=false;

        PackageInfo packInfo =null;

        for (int i=0; i < packages.size(); i++)
        {
            packInfo=packages.get(i);

            if(packInfo.packageName.startsWith(filter))
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
    List<ActivityInfo> _getActivitiesByNameFilter(PackageInfo pi, String filter, List<ActivityInfo> result)
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



    void showResultFragment()
    {

        Fragment newFragment = new ResultsFragment();
        getMainActivity().slideInFragment(newFragment);

    }
}
