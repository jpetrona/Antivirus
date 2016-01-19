package com.cresan.antivirus;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
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
import java.util.Set;

/**
 * Created by Magic Frame on 18/01/2016.
 */


public class WarningsAdapter extends ArrayAdapter<WarningData>
{
    private final Context _context;
    private  BadPackageResultData _resultData=null;
    private List<WarningData> _convertedData=null;

    public WarningsAdapter(Context context, BadPackageResultData resultData)
    {
        super(context, R.layout.warnings_adapter);

        _context=context;
        _resultData=resultData;
        _convertedData=_fillDataArray(resultData);

        clear();
        addAll(_convertedData);
        notifyDataSetChanged();
    }

    public List<WarningData> _fillDataArray(BadPackageResultData bp)
    {
        List<WarningData> wdl=new ArrayList<WarningData>();

        Set<ActivityData> activityData=bp.getActivityData();
        for(ActivityData ad : activityData)
        {
            WarningData wd=new WarningData();
            wd.iconId=R.drawable.gear;
            wd.title=ad.getActivityInfo().name;
            wd.text="Activity: Aquí viene el tronchaco de texto dependiendo de de lo que queramos poner";
            wdl.add(wd);
        }

        Set<PermissionData> permissionDataList=bp.getPermissionData();
        for(PermissionData ad : permissionDataList)
        {
            WarningData wd=new WarningData();
            wd.iconId=R.drawable.information;
            wd.title=ad.getPermissionName();
            wd.text="Permiso: Aquí viene el tronchaco de texto dependiendo de de lo que queramos poner";
            wdl.add(wd);
        }

        boolean installedGPlay=bp.getInstalledThroughGooglePlay();
        if(!installedGPlay)
        {
            WarningData wd=new WarningData();
            wd.iconId=R.drawable.information;
            wd.title="Unknown sources";
            wd.text="Aplicación instalada fuera de google play";
            wdl.add(wd);
        }

        return wdl;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View rowView;

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.warnings_adapter, parent, false);
        }else
        {
            rowView = convertView;

        }
        final WarningData obj = _convertedData.get(position);

        TextView titleView = (TextView) rowView.findViewById(R.id.titleWarning);
        TextView messageView = (TextView) rowView.findViewById(R.id.messageWarning);
        ImageView iconView = (ImageView) rowView.findViewById(R.id.iconWarning);

        iconView.setImageDrawable(ContextCompat.getDrawable(_context,obj.iconId));
        titleView.setText(obj.title);
        messageView.setText(obj.text);


        return rowView;
    }
}
