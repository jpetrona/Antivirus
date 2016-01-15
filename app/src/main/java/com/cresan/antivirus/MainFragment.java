package com.cresan.antivirus;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
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
import com.tech.applications.coretools.NotificationTools;
import com.tech.applications.coretools.time.PausableCountDownTimer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
        _runAntivirusNow.setOnClickListener(
                new View.OnClickListener()
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

    private void _logSearchResult(Set<PackageResultData> prd)
    {
        for (PackageResultData p : prd)
        {
            Log.d(_logTag, p.getPackageName());
        }
    }
    protected void _scanFileSystem()
    {
        //Scan installed packages
        List<PackageInfo> allPackages= ActivityTools.getApps(getMainActivity());
        List<PackageInfo> nonSystemAppsPackages= ActivityTools.getNonSystemApps(getMainActivity(), allPackages);

        List<PackageResultData> packageResultData=new ArrayList<PackageResultData>();

        Set<PackageData> whiteListPackages=getMainActivity().getWhiteListPackages();
        Set<PackageData> blackListPackages=getMainActivity().getBlackListPackages();
        Set<PackageData> blackListActivities=getMainActivity().getBlackListActivities();

        //Packages with problems will be stored here
        Set<PackageResultData> tempResults=new HashSet<PackageResultData>();

        _scanForWhiteListedApps(nonSystemAppsPackages,whiteListPackages,tempResults);

        Log.d(_logTag,"=====> Showing whitelisted apps");
        _logSearchResult(tempResults);
    }

    protected Set<PackageResultData> _scanForWhiteListedApps(List<PackageInfo> packagesToSearch, Set<PackageData> whiteListPackages,
                                                              Set<PackageResultData> result)
    {
        result.clear();

        Set<PackageResultData> subResult=new HashSet<PackageResultData>();

        //Check against whitelist
        for(PackageData pd : whiteListPackages)
        {
            getPackagesByNameFilter(packagesToSearch, pd.getPackageName(), subResult);

            result.addAll(subResult);
        }

        return result;
    }

    Set<PackageResultData> getPackagesByNameFilter(List<PackageInfo> packages, String filter, Set<PackageResultData> result)
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
                result.add(new PackageResultData(packInfo));

                //Just one package if we were not using a wildcard
                if (!wildcard)
                    break;
            }
        }

        return result;
    }

}
