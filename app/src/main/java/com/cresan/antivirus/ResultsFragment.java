package com.cresan.antivirus;

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


import com.cresan.androidprotector.R;
import com.tech.applications.coretools.ActivityTools;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;

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
    public static ListView _listview;
    private Button _buttonRemove;


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
        _buttonRemove.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                for (String pck : myArrayAdapter.selectedApps)
                {


                    Uri uri = Uri.fromParts("package", pck, null);
                    Intent it = new Intent(Intent.ACTION_DELETE, uri);
                    startActivity(it);
                    //packagename.remove(pck);
                    //((BaseAdapter) _listview.getAdapter()).notifyDataSetChanged();
                    Log.i("LISTA", "Lista: " + pck);
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

    @Override
    public void onPause()
    {
        super.onPause();
        Log.i("PARA","ENTRO EN PAUSEEEEEE");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.i("resume","RESUMEEEEEEEEE");
    }

    protected void _setupFragment(View view)
    {
        _listview = (ListView) view.findViewById(R.id.list);

        _listview.setAdapter(new myArrayAdapter(getMainActivity().getApplicationContext(), _suspiciousAppList,getMainActivity()));
    }




}

