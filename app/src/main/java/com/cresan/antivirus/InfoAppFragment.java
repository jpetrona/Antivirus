package com.cresan.antivirus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    IProblem _problem = null;
    boolean  _uninstallingPackage = false;
    private LinearLayout _containerButtonsApp = null;
    private LinearLayout _containerBuuttonsConfig = null;


    private Button _button = null;

    AntivirusActivity getMainActivity()
    {
        return (AntivirusActivity) getActivity();
    }

    public void setData(IProblem suspiciousAppList)
    {
        _problem = suspiciousAppList;

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

        if(_problem.isDangerous())
        {
            warningLevel.setTextColor(ContextCompat.getColor(getContext(),R.color.HighRiskColor));
            warningLevel.setText(R.string.high_risk);
        }
        else
        {
            warningLevel.setTextColor(ContextCompat.getColor(getContext(),R.color.MediumRiskColor));
            warningLevel.setText(R.string.medium_risk);
        }

        if(_problem.getType()== IProblem.ProblemType.AppProblem)
        {
            final AppProblem appProblem=(AppProblem) _problem;
            ImageView iconApp = (ImageView) view.findViewById(R.id.iconGeneral);
            Drawable s = ActivityTools.getIconFromPackage(appProblem.getPackageName(), getContext());
            _button = (Button) view.findViewById(R.id.buttonUninstall);
            final Button buttonTrust = (Button) view.findViewById(R.id.buttonTrust);


            _button.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {

                    _uninstallingPackage = true;
                    Uri uri = Uri.fromParts("package", appProblem.getPackageName(), null);
                    Intent it = new Intent(Intent.ACTION_DELETE, uri);
                    startActivity(it);
                    _button.setEnabled(false);
                }
            });

            buttonTrust.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    buttonTrust.setEnabled(false);

                    new AlertDialog.Builder(getContext())
                            .setTitle(getString(R.string.warning))
                            .setMessage(getString(R.string.dialog_trust_app))
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {

                                    UserWhiteList userWhiteList = getMainActivity().getUserWhiteList();

                                    //PackageData pdo=new PackageData(_suspiciousApp.getPackageName());

                                    userWhiteList.addItem(appProblem);
                                    userWhiteList.writeToJSON();

                                    MenacesCacheSet menacesCacheSet = getMainActivity().getMenacesCacheSet();
                                    menacesCacheSet.removeItem(appProblem);
                                    menacesCacheSet.writeToJSON();

                                   /* //Se llama este listener aunque no sea semánticamente lo que pasa (desinstalar app)
                                    //Porque asi aprovechamos y lo quita de la lista.
                                    if(_appEventListener!=null && _suspiciousApp !=null)
                                    {
                                        _appEventListener.onAppUninstalled(_suspiciousApp);
                                    }*/

                                    getMainActivity().goBack();
                                    buttonTrust.setEnabled(true);
                                }
                            }).setNegativeButton("no", new DialogInterface.OnClickListener()
                    {

                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            buttonTrust.setEnabled(true);
                        }
                    }).show();

                }
            });

            textView.setText(ActivityTools.getAppNameFromPackage(getContext(), appProblem.getPackageName()));
            iconApp.setImageDrawable(s);


            _listview = (ListView) view.findViewById(R.id.listView);

            _listview.setAdapter(new WarningsAdapter(getMainActivity(), appProblem));
        }


    }

    @Override
    public void onResume()
    {
        super.onResume();

        AntivirusActivity antivirusActivity=getMainActivity();

        //Returned from an uninstallation
        if( _uninstallingPackage==true)
        {
            if(_problem!=null)
            {
                final AppProblem appProblem=(AppProblem) _problem;

                if (!ActivityTools.isPackageInstalled(getMainActivity(), appProblem.getPackageName()))
                {
                    /*if(_appEventListener!=null)
                        _appEventListener.onAppUninstalled(_suspiciousApp);*/

                    MenacesCacheSet menacesCacheSet = antivirusActivity.getMenacesCacheSet();
                    menacesCacheSet.removeItem(appProblem);
                    menacesCacheSet.writeToJSON();
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
            final AppProblem appProblem=(AppProblem) _problem;
            if(!ProblemsDataSetTools.checkIfPackageInCollection(appProblem.getPackageName(), menacesCacheSet.getSet()))
            {
                //It is in menaces cacheset. Check if it is really in the system
                if(!ActivityTools.isPackageInstalled(antivirusActivity, appProblem.getPackageName()))
                {
                    //If it isn't remove it
                    menacesCacheSet = antivirusActivity.getMenacesCacheSet();
                    menacesCacheSet.removeItem(appProblem);
                    menacesCacheSet.writeToJSON();
                }

                antivirusActivity.goBack();
            }
        }

        // Esto lo hacemos aqui porque no hay otra manera de volverlo a pasar a true, ya que el boton de desinstalar tira un dialogo del sistema y no nuestro.
        _button.setEnabled(true);
    }

}
