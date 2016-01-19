package com.cresan.antivirus;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cresan.androidprotector.R;
import com.tech.applications.coretools.ActivityTools;

/**
 * Created by Magic Frame on 18/01/2016.
 */
public class InfoAppFragment extends Fragment
{


    public static ListView _listview;
    BadPackageResultData _suspiciousApp = null;
    boolean _isUninstalling = false;
    IOnAppEvent _appEventListener = null;

    public void setAppEventListener(IOnAppEvent appEventListener)
    {

        _appEventListener = appEventListener;

    }


    AntivirusActivity getMainActivity()
    {
        return (AntivirusActivity) getActivity();
    }

    public void setData(BadPackageResultData suspiciousAppList)
    {
        _suspiciousApp = suspiciousAppList;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.app_info_fragment, container, false);

        _setupFragment(rootView);
        return rootView;
    }

    protected void _setupFragment(View view)
    {

        TextView textView = (TextView) view.findViewById(R.id.titleApp);
        TextView warningLevel = (TextView) view.findViewById(R.id.warningLevel);
        ImageView iconApp = (ImageView) view.findViewById(R.id.iconGeneral);
        Drawable s = ActivityTools.getIconFromPackage(_suspiciousApp.getPackageName(), getContext());
        Button button = (Button) view.findViewById(R.id.buttonUninstall);


        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                _isUninstalling = true;
                Uri uri = Uri.fromParts("package", _suspiciousApp.getPackageName(), null);
                Intent it = new Intent(Intent.ACTION_DELETE, uri);
                startActivity(it);

            }
        });


        textView.setText(ActivityTools.getAppNameFromPackage(getContext(), _suspiciousApp.getPackageName()));
        iconApp.setImageDrawable(s);
        warningLevel.setText("RIESGO MEDIO");

        _listview = (ListView) view.findViewById(R.id.listView);

        _listview.setAdapter(new WarningsAdapter(getMainActivity(), _suspiciousApp));


    }

    @Override
    public void onResume()
    {
        super.onResume();

        if(_isUninstalling)
        {
            _isUninstalling = false;
            getMainActivity().goBack();

            if(_appEventListener!=null)
            {
                _appEventListener.onAppUninstalled(_suspiciousApp);
            }

        }

    }
}

