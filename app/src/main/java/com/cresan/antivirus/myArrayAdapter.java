package com.cresan.antivirus;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cresan.androidprotector.R;
import com.tech.applications.coretools.ActivityTools;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Magic Frame on 13/01/2016.
 */

public class myArrayAdapter  extends ArrayAdapter<BadPackageResultData>
{

    private final Context context;
    private AntivirusActivity antivirusActivity;
    //private final String[] values;
    private  List<BadPackageResultData> values=null;
    public static List<String> selectedApps = new ArrayList<>();


    public myArrayAdapter(Context context, List<BadPackageResultData> values,AntivirusActivity antivirusActivity)
    {

        super(context, R.layout.list_apps,values);
        this.context = context;
        this.values = values;
        this.antivirusActivity = antivirusActivity;

    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {

        final View rowView;

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_apps, parent, false);
        }else
        {
            rowView = convertView;

        }
        final BadPackageResultData obj = values.get(position);
        TextView textView = (TextView) rowView.findViewById(R.id.Titlelabel);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
        CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);

        Button button = (Button) rowView.findViewById(R.id.buttonInfo);

        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showInfoAppFragment(obj);

            }
        });


        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(false);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    // Si marcamos el checkbox cogemos su nombre de paquete y lo metemos en la lista
                    selectedApps.add(obj.getPackageName());
                    Log.i("MSF", "METIDO A LA LISTA: " + obj.getPackageName());

                } else
                {

                    // Si desmarcamos el checkbox eliminamos el  nombre del paquete de la lista
                    selectedApps.remove(obj.getPackageName());

                }
            }
        });



        textView.setText(ActivityTools.getAppNameFromPackage(getContext(), obj.getPackageName()));
        imageView.setImageDrawable(ActivityTools.getIconFromPackage(obj.getPackageName(), getContext()));




        return rowView;

    }


    void showInfoAppFragment(BadPackageResultData suspiciousAppList)
    {

        // Cuando pulses el boton de info coger su posicion y pasarselo por la variable pos
        InfoAppFragment newFragment = new InfoAppFragment();
        newFragment.setData(suspiciousAppList);
        antivirusActivity.slideInFragment(newFragment);

    }

}
