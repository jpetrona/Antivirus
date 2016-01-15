package com.cresan.antivirus;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

public class myArrayAdapter  extends ArrayAdapter<String>
{

    private final Context context;
    //private final String[] values;
    private  ArrayList<String> values = new ArrayList<>();
    public static ArrayList<String> selectedApps = new ArrayList<>();


    public myArrayAdapter(Context context, ArrayList<String> values)
    {

        super(context, R.layout.list_apps,values);
        this.context = context;
        this.values = values;


    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {

        View rowView;

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_apps, parent, false);
        }else
        {
            rowView = convertView;

        }
        final String s = values.get(position);//[position];
        TextView textView = (TextView) rowView.findViewById(R.id.Titlelabel);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
        CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);

        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(false);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    // Si marcamos el checkbox cogemos su nombre de paquete y lo metemos en la lista
                    selectedApps.add(s);
                    Log.i("MSF","METIDO A LA LISTA: " + s);

                }else
                {

                    // Si desmarcamos el checkbox eliminamos el  nombre del paquete de la lista
                    selectedApps.remove(s);

                }
            }
        });



        textView.setText(ActivityTools.getAppNameFromPackage(getContext(), s));
        imageView.setImageDrawable(ActivityTools.getIconFromPackage(s, getContext()));




        return rowView;

    }
}
