package com.cresan.antivirus;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

    BadPackageData _suspiciousApp = null;
    boolean  _uninstallingPackage = false;

/*    IOnAppEvent _appEventListener = null;

    public void setAppEventListener(IOnAppEvent appEventListener)
    {

        _appEventListener = appEventListener;

    }
*/

    AntivirusActivity getMainActivity()
    {
        return (AntivirusActivity) getActivity();
    }

    public void setData(BadPackageData suspiciousAppList)
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
        Button buttonTrust = (Button) view.findViewById(R.id.buttonTrust);


        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                _uninstallingPackage = true;
                Uri uri = Uri.fromParts("package", _suspiciousApp.getPackageName(), null);
                Intent it = new Intent(Intent.ACTION_DELETE, uri);
                startActivity(it);
            }
        });

        buttonTrust.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.warning))
                        .setMessage(getString(R.string.dialog_trust_app))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                UserWhiteList userWhiteList=getMainActivity().getUserWhiteList();

                                //PackageData pdo=new PackageData(_suspiciousApp.getPackageName());

                                userWhiteList.addPackage(_suspiciousApp);
                                userWhiteList.writeData();

                                MenacesCacheSet menacesCacheSet=getMainActivity().getMenacesCacheSet();
                                menacesCacheSet.removePackage(_suspiciousApp);
                                menacesCacheSet.writeData();

                               /* //Se llama este listener aunque no sea semánticamente lo que pasa (desinstalar app)
                                //Porque asi aprovechamos y lo quita de la lista.
                                if(_appEventListener!=null && _suspiciousApp !=null)
                                {
                                    _appEventListener.onAppUninstalled(_suspiciousApp);
                                }*/

                                getMainActivity().goBack();

                            }
                        }).setNegativeButton("no", new DialogInterface.OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                }).show();

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

        AntivirusActivity antivirusActivity=getMainActivity();

        //Returned from an uninstallation
        if( _uninstallingPackage==true)
        {
            if(_suspiciousApp!=null)
            {
                if (!ActivityTools.isPackageInstalled(getMainActivity(), _suspiciousApp.getPackageName()))
                {
                    /*if(_appEventListener!=null)
                        _appEventListener.onAppUninstalled(_suspiciousApp);*/

                    MenacesCacheSet menacesCacheSet = antivirusActivity.getMenacesCacheSet();
                    menacesCacheSet.removePackage(_suspiciousApp);
                    menacesCacheSet.writeData();
                }
            }

            _uninstallingPackage=false;
            getMainActivity().goBack();

        }
        else
        {
            //User could have deleted app from file sytem while in this screen.
            //Check if it exists. If not update menacesCacheSet
            MenacesCacheSet menacesCacheSet=antivirusActivity.getMenacesCacheSet();
            if(!menacesCacheSet.checkIfPackageInList(_suspiciousApp.getPackageName()))
            {
                //It is in menaces cacheset. Check if it is really in the system
                if(!ActivityTools.isPackageInstalled(antivirusActivity, _suspiciousApp.getPackageName()))
                {
                    //If it isn't remove it
                    menacesCacheSet = antivirusActivity.getMenacesCacheSet();
                    menacesCacheSet.removePackage(_suspiciousApp);
                    menacesCacheSet.writeData();
                }

                antivirusActivity.goBack();
            }
        }
    }

}

