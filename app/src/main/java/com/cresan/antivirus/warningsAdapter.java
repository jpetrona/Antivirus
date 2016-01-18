package com.cresan.antivirus;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cresan.androidprotector.R;
import com.tech.applications.coretools.ActivityTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Magic Frame on 18/01/2016.
 */
public class WarningsAdapter extends ArrayAdapter<BadPackageResultData>
{
    private final Context context;
    private  List<BadPackageResultData> values=null;


    public WarningsAdapter(Context context, List<BadPackageResultData> values)
    {

        super(context, R.layout.warnings_adapter,values);
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
            rowView = inflater.inflate(R.layout.warnings_adapter, parent, false);
        }else
        {
            rowView = convertView;

        }
        final BadPackageResultData obj = values.get(position);

        TextView titleview = (TextView) rowView.findViewById(R.id.titleWarning);
        TextView messageView = (TextView) rowView.findViewById(R.id.messageWarning);
        ImageView iconView = (ImageView) rowView.findViewById(R.id.iconWarning);



        //pasar messaview con la explicacion del warning encontrado

        return rowView;
    }
}
