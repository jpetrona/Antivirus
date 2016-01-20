package com.cresan.antivirus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import com.cresan.androidprotector.R;
import com.tech.applications.coretools.ActivityTools;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;

/**
 * Created by hexdump on 02/11/15.
 */

public class ResultsFragment extends Fragment
{
    final String _logTag=this.getClass().getSimpleName();

    AntivirusActivity getMainActivity() {return (AntivirusActivity) getActivity();}

    //String[] packagename = new String[]{"com.magic.sanfulgencio","com.dropbox.android","com.appspot.swisscodemonkeys.detector","com.nolanlawson.logcat"};

    List<BadPackageResultData> _suspiciousAppList=null;
    /*
    ArrayList<String> packagename = new ArrayList<String>(){{
        add("com.magic.sanfulgencio");
        add("com.dropbox.android");
        add("com.appspot.swisscodemonkeys.detector");
        add("com.nolanlawson.logcat");
    }};*/
    private ListView _listview;
    private Button _buttonRemove;

    List<BadPackageResultData> _selectedApps = new ArrayList<>();

    ResultsAdapter _resultAdapter=null;

    public void setData(List<BadPackageResultData> suspiciousAppList)
    {
        _suspiciousAppList=suspiciousAppList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.results_fragment, container, false);
        _buttonRemove = (Button) rootView.findViewById(R.id.btnKill);
        TextView textView = (TextView) rootView.findViewById(R.id.counterApps);
        textView.setText("Amenazas encontradas: " + _suspiciousAppList.size());

        _buttonRemove.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                for (BadPackageResultData pck : _selectedApps)
                {
                    Uri uri = Uri.fromParts("package", pck.getPackageName(), null);
                    Intent it = new Intent(Intent.ACTION_DELETE, uri);
                    startActivity(it);


                    Log.i("LISTA", "Lista: " + pck);
                }


                _selectedApps.clear();

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
            public void onItemSelectedStateChanged(boolean isChecked, BadPackageResultData bpd)
            {
                if (isChecked)
                {
                    // Si marcamos el checkbox cogemos su nombre de paquete y lo metemos en la lista
                    _selectedApps.add(bpd);
                    Log.i("MSF", "METIDO A LA LISTA: " + bpd.getPackageName());

                }
                else
                {

                    // Si desmarcamos el checkbox eliminamos el  nombre del paquete de la lista
                    _selectedApps.remove(bpd);
                    Log.i("MSF", "SACADO DE A LA LISTA: " + bpd.getPackageName());
                }
            }
        });



        _listview.setAdapter(_resultAdapter);
    }




}

