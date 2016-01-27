package com.cresan.antivirus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import com.cresan.androidprotector.R;
import com.tech.applications.coretools.ActivityTools;
import com.tech.applications.coretools.StringTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hexdump on 02/11/15.
 */

public class ResultsFragment extends Fragment
{
    final String _logTag=this.getClass().getSimpleName();

    AntivirusActivity getMainActivity() {return (AntivirusActivity) getActivity();}

    //String[] packagename = new String[]{"com.magic.sanfulgencio","com.dropbox.android","com.appspot.swisscodemonkeys.detector","com.nolanlawson.logcat"};

    List<BadPackageData> _suspiciousAppList=null;

    private ListView _listview;
    private Button _buttonRemove;

    List<BadPackageData> _selectedApps = new ArrayList<>();

    ResultsAdapter _resultAdapter=null;

    TextView _threatsFoundSummary=null;

    public void setData(List<BadPackageData> suspiciousAppList)
    {
        _suspiciousAppList=suspiciousAppList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.results_fragment, container, false);
        _buttonRemove = (Button) rootView.findViewById(R.id.btnKill);
        _threatsFoundSummary = (TextView) rootView.findViewById(R.id.counterApps);

        _buttonRemove.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(_selectedApps.size() > 0)
                {

                    for (BadPackageData pck : _selectedApps)
                    {
                        Uri uri = Uri.fromParts("package", pck.getPackageName(), null);
                        Intent it = new Intent(Intent.ACTION_DELETE, uri);
                        startActivity(it);

                        Log.i("LISTA", "Lista: " + pck);
                    }
                }else
                {

                    new AlertDialog.Builder(getContext())
                            .setTitle(getString(R.string.warning))
                            .setMessage(getString(R.string.dialog_message_no_app_selected))
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {


                                }
                            }).show();




                }
            }
        });
        _setupFragment(rootView);

        if(ActivityTools.checkIfUSBDebugIsEnabled(getContext()))
        {
           Log.i("usb","USB ENABLEEEE");

        }else

        {
            Log.i("usb","USB NOT ENABLEEEE");
        }


        return rootView;
    }



    protected void _setupFragment(View view)
    {
        _listview = (ListView) view.findViewById(R.id.list);
        _resultAdapter=new ResultsAdapter(getMainActivity(), _suspiciousAppList, getMainActivity());

        _resultAdapter.setResultItemSelectedStateChangedListener(new IResultItemSelecteStateChanged()
        {
            @Override
            public void onItemSelectedStateChanged(boolean isChecked, BadPackageData bpd)
            {
                if (isChecked)
                {
                    // Si marcamos el checkbox cogemos su nombre de paquete y lo metemos en la lista
                    _selectedApps.add(bpd);
                    Log.i("MSF", "METIDO A LA LISTA: " + bpd.getPackageName());

                } else
                {

                    // Si desmarcamos el checkbox eliminamos el  nombre del paquete de la lista
                    _selectedApps.remove(bpd);
                    Log.i("MSF", "SACADO DE A LA LISTA: " + bpd.getPackageName());
                }
            }
        });



        _listview.setAdapter(_resultAdapter);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        MenacesCacheSet menacesCache = getMainActivity().getMenacesCacheSet();

        List<BadPackageData> toDelete=new ArrayList<BadPackageData>();

        for (BadPackageData pd : _selectedApps )
        {
            if(!ActivityTools.isPackageInstalled(getMainActivity(),pd.getPackageName()))
            {
                _resultAdapter.remove(pd);
                toDelete.add(pd);

                menacesCache.removePackage(pd);
                menacesCache.writeData();
            }
        }

        _selectedApps.removeAll(toDelete);

        _updateFoundThreatsText(_threatsFoundSummary, _resultAdapter.getCount());

        if(menacesCache.getMenaceCount()<=0)
        {
            getMainActivity().goBack();
        }
    }

    void _updateFoundThreatsText(TextView textView, int appCount)
    {
        String finalStr=getString(R.string.threats_found);
        finalStr=StringTools.fillParams(finalStr, "#", Integer.toString(appCount));
        textView.setText(finalStr);
    }
}

