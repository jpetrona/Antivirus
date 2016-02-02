package com.cresan.antivirus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cresan.androidprotector.R;
import com.tech.applications.coretools.ActivityTools;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Magic Frame on 27/01/2016.
 */
public class IgnoredAdapter  extends ArrayAdapter<BadPackageData>
{
    private final Context _context;
    private List<BadPackageData> _values =null;
    IOnAdapterItemRemoved _adapterListener=null;
    void setOnAdapterItemRemovedListener(IOnAdapterItemRemoved listener) { _adapterListener=listener;}

    public IgnoredAdapter(Context context, List<BadPackageData> values)
    {
        super(context, R.layout.ignored_adapter,values);
        this._context = context;
        this._values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {

        final View rowView;

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.ignored_adapter, parent, false);
        }else
        {
            rowView = convertView;

        }

        final BadPackageData obj = _values.get(position);

        TextView textView = (TextView) rowView.findViewById(R.id.nameAppIgnored);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.iconAppIgnored);
        LinearLayout linearLayout = (LinearLayout) rowView.findViewById(R.id.linearLayoutAdapter);
        linearLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new AlertDialog.Builder(getContext())
                        .setTitle(_context.getString(R.string.warning))
                        .setMessage(_context.getString(R.string.remove_ignored_app_message) + " " + ActivityTools.getAppNameFromPackage(getContext(), obj.getPackageName()))
                        .setPositiveButton(_context.getString(R.string.accept_eula), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                remove(obj);
                                _adapterListener.onItemRemoved(obj);
                            }
                        }).setNegativeButton(_context.getString(R.string.cancel), new DialogInterface.OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                }).show();

            }
        });

        textView.setText(ActivityTools.getAppNameFromPackage(getContext(), obj.getPackageName()));
        imageView.setImageDrawable(ActivityTools.getIconFromPackage(obj.getPackageName(), getContext()));

        return rowView;
    }

    public void refresh(List<BadPackageData> bpd)
    {
        clear();
        addAll(bpd);
    }
}
