package com.cresan.antivirus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cresan.androidprotector.R;
import com.liulishuo.magicprogresswidget.MagicProgressBar;
import com.tech.applications.coretools.ActivityTools;
import com.tech.applications.coretools.BatteryData;
import com.tech.applications.coretools.BatteryTools;
import com.tech.applications.coretools.NetworkTools;
import com.tech.applications.coretools.StringTools;
import com.tech.applications.coretools.time.PausableCountDownTimer;

import at.grabner.circleprogress.CircleProgressView;

import java.util.ArrayList;
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
    Button _resolvePersistProblems = null;

    //Scrollable data chunk data
    ImageView _progressPanelIconImageView;
    TextView _progressPanelTextView;
    CircleProgressView _circleProgressBar;
    TextView _bottomMenacesCounterText;
    TextView _bottomScannedAppsText;
    RelativeLayout _progressContainer;
    RelativeLayout _buttonContainer;
    RelativeLayout _superContainer;
    RelativeLayout _noMenacesInformationContainer;
    ImageView _riskIcon;
    LinearLayout _deviceRiskPanel;
    LinearLayout _scanningProgressPanel;
    TextView _topMenacesCounterText;


    private boolean firstScan = false;
    final int kProgressBarRefressTime=50;

    final int kEnterAppTime=50;
    final int kScanningAppTime=100;
    final int kIconChangeToGoodOrBadTime =100;

    PausableCountDownTimer _cdTimer =null;

    /*Set<BadPackageData> _foundMenaces=null;*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.main_fragment, container, false);

        _setupFragment(rootView);
        controlInitialStates();
        return rootView;
    }



    protected void _setupFragment(View root)
    {

        _progressPanelIconImageView =(ImageView)root.findViewById(R.id.animationProgressPanelIconImageView);
        _progressPanelTextView =(TextView)root.findViewById(R.id.animationProgressPanelTextView);;
        _circleProgressBar=(CircleProgressView) root.findViewById(R.id.circleView);
        _bottomMenacesCounterText=(TextView) root.findViewById(R.id.bottomFoundMenacesCount);
        _bottomScannedAppsText=(TextView) root.findViewById(R.id.bottomScannedApp);
        _buttonContainer=(RelativeLayout)root.findViewById(R.id.buttonLayout);
        _progressContainer=(RelativeLayout)root.findViewById(R.id.animationProgressPanel);
        _superContainer=(RelativeLayout)root.findViewById(R.id.superContainer);
        _noMenacesInformationContainer=(RelativeLayout) root.findViewById(R.id.noMenacesFoundPanel);
        _riskIcon = (ImageView) root.findViewById(R.id.iconRisk);
        _deviceRiskPanel = (LinearLayout) root.findViewById(R.id.deviceRiskPanel);
        _scanningProgressPanel=(LinearLayout) root.findViewById(R.id.scanningProgressPanel);
        _resolvePersistProblems = (Button) root.findViewById(R.id.button_resolve_problems);
        _resolvePersistProblems.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Set<BadPackageData> foundMenaces=getMainActivity().getBadResultPackageDataFromMenaceSet();

                if (foundMenaces !=null && foundMenaces.size() !=0)
                {

                    showResultFragment(new ArrayList<BadPackageData>(foundMenaces));

                }
            }
        });


        _topMenacesCounterText = (TextView)root.findViewById(R.id.topMenacesCounter);
        _runAntivirusNow=(Button)root.findViewById(R.id.runAntivirusNow);
        _runAntivirusNow.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                _runAntivirusNow.setEnabled(false);

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

        /*final WaveView waveView = (WaveView) root.findViewById(R.id.wave);
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
        waveView.setText1(""+bd.getLevelPercent()+"%");*/
       // TextView tv = (TextView) root.findViewById(R.id.voltageValue);
        //DecimalFormat df = new DecimalFormat("0.00");
        /*df.setMaximumFractionDigits(2);
        tv.setText(df.format(bd.getVoltage()) + " v");
        tv = (TextView) root.findViewById(R.id.temperatureValue);
        df = new DecimalFormat("0.0");
        df.setMaximumFractionDigits(1);
        tv.setText(df.format(bd.getTemperature()) + "º");
*/
        _resetFormLayout();
    }

    private void _resetFormLayout()
    {
        _progressContainer.setVisibility(View.INVISIBLE);

        _buttonContainer.setVisibility(View.VISIBLE);
        _buttonContainer.setTranslationX(0);

        _runAntivirusNow.setEnabled(true);
    }

    protected void _startRealScan()
    {

        getMainActivity().startMonitorScan(new MonitorShieldService.IClientInterface()
        {
            @Override
            public void onMonitorFoundMenace(BadPackageData menace)
            {
            }

            @Override
            public void onScanResult(List<PackageInfo> allPacakgesToScan, Set<BadPackageData> scanResult)
            {
                AppData appData = getMainActivity().getAppData();
                appData.setFirstScanDone(true);
                appData.serialize(getMainActivity());

                _startScanningAnimation(allPacakgesToScan, scanResult);
            }
        });
    }

    private void _scanFileSystem()
    {
        _scanningProgressPanel.setAlpha(0.0f);
        _scanningProgressPanel.setVisibility(View.VISIBLE);

        ObjectAnimator oa1 = ObjectAnimator.ofFloat(_scanningProgressPanel, "alpha",0.0f,1.0f);
        oa1.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                _startRealScan();
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }
        });
        oa1.setDuration(500);
        oa1.start();

        oa1 = ObjectAnimator.ofFloat(_deviceRiskPanel, "alpha",1.0f,0.0f);
        oa1.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                _deviceRiskPanel.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }
        });

        oa1.setDuration(500);
        oa1.start();
    }

    private void _startScanningAnimation(final List<PackageInfo> allPackages, final Set<BadPackageData> tempBadResults)
    {
        //Animate the button exit
        ObjectAnimator oa1 = ObjectAnimator.ofFloat(_buttonContainer, "translationX",
                0,
                -_superContainer.getWidth()/2.0f-_buttonContainer.getWidth());
        oa1.setDuration(100);
        oa1.setInterpolator(new LinearInterpolator());
        oa1.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                _configureScanningUI();

                ScanningFileSystemAsyncTask task = new ScanningFileSystemAsyncTask(getMainActivity(), allPackages, tempBadResults);
                task.setAsyncTaskCallback(new IOnActionFinished()
                {
                    @Override
                    public void onFinished()
                    {
                        if(tempBadResults.size()>0)
                        {
                            showResultFragment(new ArrayList<BadPackageData>(tempBadResults));

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    _configureNonScanningUI();
                                }
                            }, 400);
                        }
                        else
                        {
                            _playNoMenacesAnimationFound();
                        }

                    }
                });

                task.execute();
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }
        });
        oa1.start();
/*
        final IOnActionFinished _scanFinished= new IOnActionFinished()
        {
            @Override
            public void onFinished()
            {


                if(tempBadResults.size() !=0)
                {
                    showResultFragment(new ArrayList<BadPackageData>(tempBadResults));
                }else
                {
                    //##################################Falta volver a sacar el boton de scan, cambiar el tipo de lista por la del servicio y estaria listo ###########################
                    activateProtectedState();

                }
            }
        };*/

//        _scanPackage(packagesToScan,0, _onScanFileListener);

    }

    void _playNoMenacesAnimationFound()
    {
        ObjectAnimator oa = ObjectAnimator.ofFloat(_progressContainer, "rotationY", 0f, 90.0f);

        oa.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                super.onAnimationEnd(animation);

                _noMenacesInformationContainer.setVisibility(View.VISIBLE);
                _progressContainer.setVisibility(View.INVISIBLE);
                _progressContainer.setRotationY(0);
                ObjectAnimator oa = ObjectAnimator.ofFloat(_noMenacesInformationContainer, "rotationY", -90f, 0.0f);
                oa.addListener(new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        Handler handler=new Handler();
                        handler.postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                ObjectAnimator oa = ObjectAnimator.ofFloat(_noMenacesInformationContainer, "rotationY", 0, 90.0f);
                                oa.addListener(new AnimatorListenerAdapter()
                                {
                                    @Override
                                    public void onAnimationEnd(Animator animation)
                                    {
                                        _noMenacesInformationContainer.setRotationY(0);
                                        _noMenacesInformationContainer.setVisibility(View.INVISIBLE);
                                        _buttonContainer.setVisibility(View.VISIBLE);
                                        _buttonContainer.setTranslationX(0);
                                        _runAntivirusNow.setEnabled(true);
                                        ObjectAnimator oa = ObjectAnimator.ofFloat(_buttonContainer, "rotationY", -90f, 0.0f);
                                    }
                                });
                                oa.setDuration(100);
                                oa.setInterpolator(new LinearInterpolator());
                                oa.start();
                            }
                        },2000);
                    }
                });

                oa.setDuration(100);
                oa.setInterpolator(new LinearInterpolator());
                oa.start();
            }
        });

        oa.setDuration(100);
        oa.setInterpolator(new LinearInterpolator());
        oa.start();
    }
/*
    //Cambiando texto SCAN SYSTEM PACKAGES
    private void _scanSystemPackagesAnimationWithText(final List<PackageInfo> systemApps,Set<BadPackageData> menaces,
                                                      final IOnActionFinished scanFinishedListener)
    {

        //_progressContainer.setVisibility(View.VISIBLE);
        ObjectAnimator oa1 = ObjectAnimator.ofFloat(_progressContainer, "translationX",
                _superContainer.getWidth()/2.0f+_progressContainer.getWidth(), 0.0f);
        oa1.setDuration(kEnterAppTime);
        oa1.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                _progressContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                _scanSystemPackageText(systemApps,0,menaces,scanFinishedListener);
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }
        });

        oa1.start();
    }

    class ScanPackagesRunnable implements   Runnable
    {
        public List<PackageInfo> systemApps;
        public int currentPackage;
        public IOnActionFinished onActionFinishedListener;
        public Set<BadPackageData> menaces;

        @Override
        public void run()
        {
            _scanSystemPackageText(systemApps,currentPackage, menaces, onActionFinishedListener );
        }
    }
    Handler _handler=new Handler();
    ScanPackagesRunnable _scanSystemPackageTextRunnable=new ScanPackagesRunnable();

    private void _scanSystemPackageText(final List<PackageInfo> systemApps, int currentPackage, Set<BadPackageData> menaces,
                                        final IOnActionFinished fileScanFinishedListener)
    {
        PackageInfo packageToScan=systemApps.get(currentPackage);

        _progressPanelIconImageView.setImageDrawable(ActivityTools.getIconFromPackage(packageToScan.packageName, getMainActivity()));
        _progressPanelTextView.setText(packageToScan.packageName);

        if(currentPackage>=systemApps.size())
            fileScanFinishedListener.onFinished();
        else
        {
            _scanSystemPackageTextRunnable.systemApps=systemApps;
            _scanSystemPackageTextRunnable.currentPackage=++currentPackage;
            _scanSystemPackageTextRunnable.onActionFinishedListener=fileScanFinishedListener;
            _handler.postDelayed(_scanSystemPackageTextRunnable,100);
        }
    }

    void _continueScanning(List<PackageInfo> packagesToScan,int scannedIndex, IOnActionFinished scanFinishedListener)
    {
        ++scannedIndex;
        if(scannedIndex<packagesToScan.size())
            _scanPackage(packagesToScan,scannedIndex,_onScanFileListener);
        else
            scanFinishedListener.onFinished();
    }

    void _doWaitToScanPackage(final int miliseconds, List<PackageInfo> packagesToScan, final int currentPackage, final IOnFileScanFinished listener)
    {
        //_progressDialog=_getProgressDialog();
        //_progressDialog.show();


        _cdTimer =new PausableCountDownTimer(miliseconds,kProgressBarRefressTime)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                _progressPanelprogressBar.setPercent((miliseconds - millisUntilFinished) / (float) miliseconds);
            }

            @Override
            public void onFinish()
            {
                _cdTimer =null;
                _progressPanelprogressBar.setPercent(1.0f);
                _convertStageIconInto(true, kIconChangeToGoodOrBadTime, new IOnActionFinished()
                {
                    @Override
                    public void onFinished()
                    {
                        listener.onFinished(currentPackage);
                    }
                });
            }
        };

        _cdTimer.start();
    }

    private void _convertStageIconInto(final boolean isOkIcon,final int waitTimeToEnd, final IOnActionFinished finishedAction)
    {
        ObjectAnimator oa2 = ObjectAnimator.ofFloat(_progressPanelIconImageView, "rotationY", 0f, 90.0f);

        oa2.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                super.onAnimationEnd(animation);
                if (isOkIcon)
                    _progressPanelIconImageView.setImageResource(R.drawable.ok_100);
                else
                    _progressPanelIconImageView.setImageResource(R.drawable.cancel);

                ObjectAnimator oa1 = ObjectAnimator.ofFloat(_progressPanelIconImageView, "rotationY", -90.0f, 0.0f);
                oa1.setInterpolator(new LinearInterpolator());
                oa1.setDuration(100);
                oa1.start();
                oa1.addListener(new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        Handler handler = new Handler();

                        // Post the task to set it visible in 5000ms
                        handler.postDelayed(
                                new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        finishedAction.onFinished();
                                    }
                                }, waitTimeToEnd);
                    }
                });
            }
        });

        oa2.setDuration(100);
        oa2.setInterpolator(new LinearInterpolator());
        oa2.start();
    }

    private void _scanPackage(final List<PackageInfo> packagesToScan, final int currentPackageIndex, final IOnFileScanFinished onActionFinished)
    {
        PackageInfo packageToScan=packagesToScan.get(currentPackageIndex);

        _progressPanelIconImageView.setImageDrawable(ActivityTools.getIconFromPackage(packageToScan.packageName, getMainActivity()));
        _progressPanelTextView.setText(packageToScan.packageName);
        _progressPanelprogressBar.setPercent(0);

        //_progressContainer.setVisibility(View.VISIBLE);
        ObjectAnimator oa1 = ObjectAnimator.ofFloat(_progressContainer, "translationX",
                _superContainer.getWidth()/2.0f+_progressContainer.getWidth(), 0.0f);
        oa1.setDuration(kEnterAppTime);
        oa1.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                _progressContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                _doWaitToScanPackage(kScanningAppTime, packagesToScan, currentPackageIndex, onActionFinished);
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }
        });

        oa1.start();
    }*/

    void showResultFragment(List<BadPackageData> suspiciousApps)
    {
        ResultsFragment newFragment= (ResultsFragment) getMainActivity().slideInFragment(AntivirusActivity.kResultFragmentTag);
        newFragment.setData(suspiciousApps);
    }

    @Override
    public void onPause()
    {
        super.onResume();
        if(_cdTimer!=null)
            _cdTimer.pause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(_cdTimer!=null)
            _cdTimer.start();
    }


    void controlInitialStates()
    {

        boolean firstScanDone =getMainActivity().getAppData().getFirstScanDone();
        Set<BadPackageData> foundMenaces = getMainActivity().getBadResultPackageDataFromMenaceSet();
        boolean isDangerous = _isDangerousAppInSet(foundMenaces);

        if(foundMenaces.isEmpty() || foundMenaces==null)
        {
            if(!firstScanDone)
            {
                _riskIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shield_medium_risk_icon));
                _topMenacesCounterText.setText("Ejecutar primer analisis para comprobar amenazas");
                _deviceRiskPanel.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.MediumRiskColor));
                _resolvePersistProblems.setVisibility(View.GONE);
            }
            else
                activateProtectedState();

        }
        else
        {
            if(isDangerous)
                activateHighRiskState(foundMenaces.size());
            else
                activateMediumRiskState(foundMenaces.size());


        }
    }

    private boolean _isDangerousAppInSet(Set<BadPackageData> set)
    {
        for(BadPackageData bprd : set)
        {
            if(bprd.isDangerousMenace())
                return true;
        }

        return false;
    }

    void activateProtectedState()
    {
        _configureNonScanningUI();

        _riskIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shield_protected_icon));
        _deviceRiskPanel.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ProtectedRiskColor));
        _topMenacesCounterText.setText("Estas protegido");
       _resolvePersistProblems.setVisibility(View.GONE);


    }

    void activateMediumRiskState(int menaces)
    {
        _configureNonScanningUI();

        _riskIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shield_medium_risk_icon));
        _deviceRiskPanel.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.MediumRiskColor));
        _updateFoundThreatsText(_topMenacesCounterText, menaces);
       _resolvePersistProblems.setVisibility(View.VISIBLE);

    }

    void activateHighRiskState(int menaces)
    {
        _configureNonScanningUI();

        _riskIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shield_high_risk_icon));
        _deviceRiskPanel.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.HighRiskColor));
        _updateFoundThreatsText(_topMenacesCounterText, menaces);
        _resolvePersistProblems.setVisibility(View.VISIBLE);
    }


    void _updateFoundThreatsText(TextView textView, int appCount)
    {
        String finalStr=getString(R.string.menaces_unresolved);
        finalStr= StringTools.fillParams(finalStr, "#", Integer.toString(appCount));
        textView.setText(finalStr);
    }

    void _configureScanningUI()
    {
        _scanningProgressPanel.setAlpha(1.0f);
        
        _progressContainer.setVisibility(View.VISIBLE);
        _progressContainer.setTranslationX(0);
        _buttonContainer.setVerticalGravity(View.INVISIBLE);
    }

    void _configureNonScanningUI()
    {
        _progressContainer.setVisibility(View.INVISIBLE);
        _progressContainer.setTranslationX(0);
        _buttonContainer.setVerticalGravity(View.VISIBLE);
        _buttonContainer.setTranslationX(0);
    }


}
