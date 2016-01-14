package com.cresan.antivirus;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.tech.applications.coretools.BatteryData;
import com.tech.applications.coretools.BatteryTools;
import com.tech.applications.coretools.NetworkTools;
import com.tech.applications.coretools.NotificationTools;
import com.tech.applications.coretools.time.PausableCountDownTimer;

import java.text.DecimalFormat;
import java.util.Random;

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

                /*if(!NetworkTools.isNetworkAvailable(getMainActivity()))
                {
                    getMainActivity().showNoInetDialog();
                    return;
                }

                new AlertDialog.Builder(getMainActivity())
                        .setTitle(R.string.warning)
                        .setMessage(R.string.start_battery_calibration_process)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                _scanFileSystem();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();*/
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
        // Create new fragment and transaction
        //Fragment newFragment = new ScanningFragment();
        //getMainActivity().slideInFragment(newFragment);
    }


}
