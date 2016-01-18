package com.cresan.antivirus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cresan.androidprotector.R;

/**
 * Created by Magic Frame on 18/01/2016.
 */
public class infoAppFragment extends Fragment
{



    AntivirusActivity getMainActivity()
    {
        return (AntivirusActivity) getActivity();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.app_info_fragment, container, false);


        return rootView;
    }


}
