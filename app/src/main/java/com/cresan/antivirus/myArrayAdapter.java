package com.cresan.antivirus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cresan.androidprotector.R;


/**
 * Created by Magic Frame on 13/01/2016.
 */

public class myArrayAdapter  extends ArrayAdapter<String>
{

    private final Context context;
    private final String[] values;



    public myArrayAdapter(Context context, String[] values)
    {

        super(context, R.layout.list_apps,values);
        this.context = context;
        this.values = values;


    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
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

        TextView textView = (TextView) rowView.findViewById(R.id.Titlelabel);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);


        String s = values[position];

        IconExtractor ic = new IconExtractor();

        textView.setText(ic.getAppName(getContext(),s,textView));

        ic.getIcon(s, imageView, getContext());



        return rowView;

    }
}
