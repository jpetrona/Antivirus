package com.cresan.antivirus;

import android.content.Context;
import android.content.res.Resources;
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
            wd.iconId=R.drawable.adware_icon;
            wd.title=getContext().getResources().getString(R.string.title_ads);
            wd.text=getContext().getResources().getString(R.string.ads_message);
            wdl.add(wd);
        }

        Set<PermissionData> permissionDataList=bp.getPermissionData();
        for(PermissionData ad : permissionDataList)
        {

            WarningData wd=new WarningData();
            wd.iconId=setPermissionIcon(ad.getPermissionName());
            wd.title=getContext().getResources().getString(R.string.title_permission);
            wd.text=setPermissionMessage(ad.getPermissionName());
            wdl.add(wd);
        }

        boolean installedGPlay=bp.getInstalledThroughGooglePlay();
        if(!installedGPlay)
        {
            WarningData wd=new WarningData();
            wd.iconId=R.drawable.information;
            wd.title=getContext().getResources().getString(R.string.title_installedGPlay);
            wd.text=getContext().getResources().getString(R.string.installedGPlay_message);
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


    public String setPermissionMessage (String permissionName)
    {
        String message = "";
        Resources resources = getContext().getResources();

        if(permissionName.contains("READ_PHONE_STATE"))
        {

            message = resources.getString(R.string.read_phone_message);

        }else if (permissionName.contains("ACCESS_FINE_LOCATION"))
        {

            message = resources.getString(R.string.access_fine_message);

        }else if (permissionName.contains("READ_SMS"))
        {

            message = resources.getString(R.string.read_sms_message);

        }else if (permissionName.contains("WRITE_SMS"))
        {

            message = resources.getString(R.string.write_sms_message);

        }else if (permissionName.contains("SEND_SMS"))
        {

            message = resources.getString(R.string.send_sms_message);

        }else if (permissionName.contains("READ_HISTORY_BOOKMARKS"))
        {

            message = resources.getString(R.string.read_history_message);

        }else if (permissionName.contains("WRITE_HISTORY_BOOKMARKS"))
        {

            message = resources.getString(R.string.write_history_message);
        }

        return message;



    }


    public int setPermissionIcon (String permissionName)
    {
        int icon = 0;


        if(permissionName.contains("READ_PHONE_STATE"))
        {

            icon = R.drawable.privacy_icon;

        }else if (permissionName.contains("ACCESS_FINE_LOCATION"))
        {

            icon = R.drawable.privacy_icon;

        }else if (permissionName.contains("READ_SMS"))
        {

            icon = R.drawable.privacy_icon;

        }else if (permissionName.contains("WRITE_SMS"))
        {

            icon = R.drawable.privacy_icon;

        }else if (permissionName.contains("SEND_SMS"))
        {

            icon = R.drawable.privacy_icon;

        }else if (permissionName.contains("READ_HISTORY_BOOKMARKS"))
        {

            icon = R.drawable.privacy_icon;

        }else if (permissionName.contains("WRITE_HISTORY_BOOKMARKS"))
        {

            icon = R.drawable.privacy_icon;
        }

        return icon;



    }
}
