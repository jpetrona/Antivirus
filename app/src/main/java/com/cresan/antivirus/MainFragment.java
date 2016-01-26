package com.cresan.antivirus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
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
    Button _resolvePersistProblems = null;

    //Scrollable data chunk data
    ImageView _progressPanelIconImageView;
    TextView _progressPanelTextView;
    MagicProgressBar _progressPanelprogressBar;
    RelativeLayout _progressContainer;
    RelativeLayout _buttonContainer;
    RelativeLayout _superContainer;
    ImageView _riskIcon;
    LinearLayout _backgroundRisk;
    TextView _menacesCounterText;
    private boolean _firstUse = false;
    final int kProgressBarRefressTime=50;


    PausableCountDownTimer _cdTimer =null;

    Set<BadPackageResultData> _foundMenaces=null;

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

        _progressPanelIconImageView =(ImageView)root.findViewById(R.id.progressPanelIconImageView);
        _progressPanelTextView =(TextView)root.findViewById(R.id.progressPanelTextView);;
        _progressPanelprogressBar =(MagicProgressBar)root.findViewById(R.id.progressPanelProgressBar);
        _buttonContainer=(RelativeLayout)root.findViewById(R.id.buttonLayout);
        _progressContainer=(RelativeLayout)root.findViewById(R.id.progressPanel);
        _superContainer=(RelativeLayout)root.findViewById(R.id.superContainer);
        _riskIcon = (ImageView) root.findViewById(R.id.iconRisk);
        _backgroundRisk = (LinearLayout) root.findViewById(R.id.BackgroundColorRisk);
        _resolvePersistProblems = (Button) root.findViewById(R.id.button_resolve_problems);
        _resolvePersistProblems.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (_foundMenaces !=null && _foundMenaces.size() !=0)
                {

                    showResultFragment(new ArrayList<BadPackageResultData>(_foundMenaces));

                }
            }
        });


        _menacesCounterText = (TextView)root.findViewById(R.id.menacesCounter);
        _runAntivirusNow=(Button)root.findViewById(R.id.runAntivirusNow);
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

    protected void _scanFileSystem()
    {
        _firstUse = true;
        //Scan installed packages
        List<PackageInfo> allPackages= ActivityTools.getApps(getMainActivity(), PackageManager.GET_ACTIVITIES | PackageManager.GET_PERMISSIONS);
        List<PackageInfo> nonSystemAppsPackages= ActivityTools.getNonSystemApps(getMainActivity(), allPackages);

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

        List<PackageInfo> potentialBadApps=_removeWhiteListPackagesFromPackageList(nonSystemAppsPackages, whiteListPackages);
        potentialBadApps=getMainActivity().getUserWhiteList().removePackagesFromPackageList(potentialBadApps);

        _scanForBlackListedActivityApps(potentialBadApps, blackListActivities, tempBadResults);
        _scanForSuspiciousPermissionsApps(potentialBadApps, suspiciousPermissions, tempBadResults);
        _fillInstalledFromGooglePlay(potentialBadApps, tempBadResults);

        /*for (BadPackageResultData p : tempBadResults)
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

        showResultFragment(new ArrayList<BadPackageResultData>(tempBadResults));*/

        List<PackageInfo> _packageInfo=new ArrayList<PackageInfo>();
        _packageInfo.add(allPackages.get(0));
        _packageInfo.add(allPackages.get(1));
        _packageInfo.add(allPackages.get(2));

        _foundMenaces=tempBadResults;

        _startScanningAnimation(_packageInfo,_foundMenaces);

    }

    private void _startScanningAnimation(final List<PackageInfo> packagesToScan, final Set<BadPackageResultData> tempBadResults)
    {
        ObjectAnimator oa1=new ObjectAnimator();
        oa1.setDuration(500);
        oa1.setInterpolator(new AccelerateInterpolator());

        oa1 = ObjectAnimator.ofFloat(_buttonContainer, "translationX",
                0,
                -_superContainer.getWidth()/2.0f-_buttonContainer.getWidth());
        oa1.setDuration(500);
        oa1.setInterpolator(new LinearInterpolator());
        oa1.start();
        oa1.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                //_buttonContainer.setVisibility(View.INVISIBLE);
                //whenFinishedAction.doAction();
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

        final IOnActionFinished _scanFinished= new IOnActionFinished()
        {
            @Override
            public void onFinished()
            {


                if(tempBadResults.size() !=0)
                {
                    showResultFragment(new ArrayList<BadPackageResultData>(tempBadResults));
                }else
                {
                    //##################################Falta volver a sacar el boton de scan, cambiar el tipo de lista por la del servicio y estaria listo ###########################
                    activateProtectedState();

                }
            }
        };

        _onScanFileListener=new IOnFileScanFinished()
        {
            @Override
            public void onFinished(int scannedIndex)
            {
                _continueScanning(packagesToScan,scannedIndex,_scanFinished);
            }
        };

        _scanPackage(packagesToScan,0, _onScanFileListener);
    }

    IOnFileScanFinished _onScanFileListener=null;

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
                _convertStageIconInto(true, 2000, new IOnActionFinished()
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
                _doWaitToScanPackage(3000, packagesToScan, currentPackageIndex, onActionFinished);
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

    protected Set<BadPackageResultData> _fillInstalledFromGooglePlay(List<PackageInfo> packagesToSearch, Set<BadPackageResultData> setToUpdate)
    {

        //Check against whitelist
        for(PackageInfo pi : packagesToSearch)
        {
            if(!ActivityTools.checkIfAppWasInstalledThroughGooglePlay(getActivity(),pi.packageName))
            {
                //Update or create new if it does not exist
                BadPackageResultData bprd=getBadPackageResultByPackageName(setToUpdate, pi.packageName);
                if(bprd==null)
                {
                    bprd = new BadPackageResultData(pi);
                    setToUpdate.add(bprd);
                }

                bprd.setInstalledThroughGooglePlay(false);
            }
            else
            {
                BadPackageResultData bprd=getBadPackageResultByPackageName(setToUpdate, pi.packageName);
                if(bprd!=null)
                    bprd.setInstalledThroughGooglePlay(true);
            }
        }

        return setToUpdate;
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

    protected List<PackageInfo> _removeWhiteListPackagesFromPackageList(List<PackageInfo> packagesToSearch, Set<PackageData> whiteListPackages)
    {
        boolean found=false;

        List<PackageInfo> trimmedPackageList=new ArrayList<PackageInfo>(packagesToSearch);

        //Check against whitelist
        for(PackageData pd : whiteListPackages)
        {
            PackageInfo p = null;
            int index = 0;
            String packageName = pd.getPackageName();
            found = false;

            while (found == false && index < trimmedPackageList.size())
            {
                p = trimmedPackageList.get(index);
                if (packageNameBelongsToPackageMask(p.packageName,pd.getPackageName()))
                {
                    found = true;
                    trimmedPackageList.remove(index);
                }
                else
                    ++index;
            }
        }

        return trimmedPackageList;
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
                //In subResult we have now all the ActivityInfo entries resulting in a menace
                _getActivitiesByNameFilter(pi, pd.getPackageName(), subResult);

                //If we found bad activities in the package fill the bad package information into result
                if(subResult.size()>0)
                {
                    //Update or create new if it does not exist
                    BadPackageResultData bprd=getBadPackageResultByPackageName(setToUpdate, pi.packageName);
                    if(bprd==null)
                    {
                        bprd = new BadPackageResultData(pi);
                        setToUpdate.add(bprd);
                    }

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
            for(PermissionData permData : suspiciousPermissions)
            {
                if(ActivityTools.packageInfoHasPermission(pi, permData.getPermissionName()))
                {
                    //Update or create new if it does not exist
                    BadPackageResultData bprd=getBadPackageResultByPackageName(setToUpdate, pi.packageName);
                    if(bprd==null)
                    {
                        bprd = new BadPackageResultData(pi);
                        setToUpdate.add(bprd);
                    }


                    bprd.addPermissionData(permData);
                }
            }
        }

        return setToUpdate;
    }

    static public boolean packageNameBelongsToPackageMask(String packageName, String mask)
    {
        boolean wildcard=false;

        if(mask.charAt(mask.length()-1)=='*')
        {
            wildcard=true;
            mask=mask.substring(0,mask.length()-2);
        }
        else
            wildcard=false;

        if(wildcard==true)
        {
            if (packageName.startsWith(mask))
                return true;
            else
                return false;
        }
        else
        {
            if(packageName.equals(mask))
                return true;
            else
                return false;
        }

    }

    Set<GoodPackageResultData> _getPackagesByNameFilter(List<PackageInfo> packages, String filter, Set<GoodPackageResultData> result)
    {
        boolean wildcard=false;

        result.clear();

        if(filter.charAt(filter.length()-1)=='*')
            wildcard=true;

        PackageInfo packInfo =null;

        for (int i=0; i < packages.size(); i++)
        {
            packInfo=packages.get(i);

            if(packageNameBelongsToPackageMask(packInfo.packageName, filter))
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



    void showResultFragment(List<BadPackageResultData> suspiciousApps)
    {

        ResultsFragment newFragment = new ResultsFragment();
        newFragment.setData(suspiciousApps);
        getMainActivity().slideInFragment(newFragment);

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

        if(_foundMenaces == null && !_firstUse)
        {

            _riskIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shield_medium_risk_icon));
            _menacesCounterText.setText("Ejecutar primer analisis para comprobar amenazas");
            _backgroundRisk.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.MediumRiskColor));
           _resolvePersistProblems.setVisibility(View.GONE);

        }else if(_foundMenaces.size() == 0 && _firstUse)
        {

            activateProtectedState();

        }else if(_foundMenaces.size() !=0) // hay que meter comprobacion de peligrosidad por permiso de este estilo: else if(_foundMenaces.size() !=0 && existDangerous) entonces sacar riesgo alto
        {

            activateHighRiskState(_foundMenaces.size());


        }// hay que meter comprobacion de peligrosidad por permiso de este estilo: else if(_foundMenaces.size() !=0 && !existDangerous) entonces sacar riesgo medio

    }


    void activateProtectedState()
    {


        _riskIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shield_protected_icon));
        _backgroundRisk.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ProtectedRiskColor));
        _menacesCounterText.setText("Estas protegido");
       _resolvePersistProblems.setVisibility(View.GONE);


    }

    void activateMediumRiskState(int menaces)
    {

        _riskIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shield_medium_risk_icon));
        _backgroundRisk.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.MediumRiskColor));
        _updateFoundThreatsText(_menacesCounterText, menaces);
       _resolvePersistProblems.setVisibility(View.VISIBLE);

    }

    void activateHighRiskState(int menaces)
    {

        _riskIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shield_high_risk_icon));
        _backgroundRisk.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.HighRiskColor));
        _updateFoundThreatsText(_menacesCounterText, menaces);
       _resolvePersistProblems.setVisibility(View.VISIBLE);
    }


    void _updateFoundThreatsText(TextView textView, int appCount)
    {
        String finalStr=getString(R.string.menaces_unresolved);
        finalStr= StringTools.fillParams(finalStr, "#", Integer.toString(appCount));
        textView.setText(finalStr);
    }



}
