package com.cresan.antivirus;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.cresan.androidprotector.R;

/**
 * Created by hexdump on 02/11/15.
 */

public class ResultsFragment extends Fragment
{
    final String _logTag=this.getClass().getSimpleName();

    AntivirusActivity getMainActivity() {return (AntivirusActivity) getActivity();}

    String[] packagename = new String[]{"com.magic.sanfulgencio","com.dropbox.android","com.appspot.swisscodemonkeys.detector","com.nolanlawson.logcat","com.dropbox.android","com.appspot.swisscodemonkeys.detector","com.nolanlawson.logcat","com.dropbox.android","com.appspot.swisscodemonkeys.detector","com.nolanlawson.logcat","com.dropbox.android","com.appspot.swisscodemonkeys.detector","com.nolanlawson.logcat","com.dropbox.android","com.appspot.swisscodemonkeys.detector","com.nolanlawson.logcat","com.dropbox.android","com.appspot.swisscodemonkeys.detector","com.nolanlawson.logcat"};

    private ListView _listview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.results_fragment, container, false);

        _setupFragment(rootView);

        return rootView;
    }



    protected void _setupFragment(View view)
    {
        _listview = (ListView) view.findViewById(R.id.list);

        _listview.setAdapter(new myArrayAdapter(getMainActivity().getApplicationContext(),packagename));
    }



}
