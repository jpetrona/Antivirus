package com.cresan.antivirus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cresan.androidprotector.R;
import com.tech.applications.coretools.ActivityTools;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Magic Frame on 13/01/2016.
 */

public class ResultsAdapter extends ArrayAdapter<BadPackageDataWrapper>
{

    Context _context;

    private List<BadPackageDataWrapper> _values =null;

    private IResultItemSelecteStateChanged _onItemChangedStateListener =null;
    public void setResultItemSelectedStateChangedListener(IResultItemSelecteStateChanged listemer) { _onItemChangedStateListener =listemer; }

    public void removeApps(List<BadPackageDataWrapper> appsToRemove)
    {
        _values.removeAll(appsToRemove);
        notifyDataSetChanged();
    }

    public List<BadPackageData> getSelectedApps()
    {
        List<BadPackageData> bpdl=new ArrayList<BadPackageData>();

        for(BadPackageDataWrapper bpdw : _values)
        {
            if(bpdw.checked)
                bpdl.add(bpdw.bpd);
        }

        return bpdl;
    }

    public ResultsAdapter(Context context, List<BadPackageDataWrapper> values)
    {
        super(context, R.layout.list_apps,values);

        _context=context;
        _values=values;

    }

    public void refresh(List<BadPackageData> bpdl)
    {
        buildBadPackageDataWrapper(bpdl, _values);
        notifyDataSetChanged();
    }

    public static List<BadPackageDataWrapper> buildBadPackageDataWrapper(List<BadPackageData> bpdl,
                                                                         List<BadPackageDataWrapper> recycleList)
    {
        List<BadPackageDataWrapper> bpdw=recycleList;

        if(bpdw==null)
            bpdw=new ArrayList<BadPackageDataWrapper>();
        else
            recycleList.clear();

        for(BadPackageData bpd: bpdl)
        {
            bpdw.add(new BadPackageDataWrapper(bpd,false));
        }

        return bpdw;
    }

    public void removeByPackageData(PackageData pd)
    {
        BadPackageDataWrapper bpdw= BadPackageDataWrapper.findByPackageDataInstance(pd, _values);
        remove(bpdw);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {

        final View rowView;

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_apps, parent, false);
        }else
        {
            rowView = convertView;

        }
        final BadPackageDataWrapper obj = _values.get(position);
        TextView textView = (TextView) rowView.findViewById(R.id.Titlelabel);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
        CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);
        checkBox.setChecked(obj.checked);
        LinearLayout linearLayout = (LinearLayout) rowView.findViewById(R.id.linearLayout);
        linearLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(_onItemChangedStateListener!=null)
                    _onItemChangedStateListener.onItemSelected(obj.bpd);
            }
        });
        imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(_onItemChangedStateListener!=null)
                    _onItemChangedStateListener.onItemSelected(obj.bpd);
            }
        });

       /* Button button = (Button) rowView.findViewById(R.id.buttonInfo);

        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showInfoAppFragment(obj);

            }
        });*/

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                obj.checked=isChecked;
                if(_onItemChangedStateListener != null)
                {
                    _onItemChangedStateListener.onItemSelectedStateChanged(isChecked,obj.bpd);
                }
            }
        });



        textView.setText(ActivityTools.getAppNameFromPackage(getContext(), obj.bpd.getPackageName()));
        imageView.setImageDrawable(ActivityTools.getIconFromPackage(obj.bpd.getPackageName(), getContext()));

        return rowView;

    }
}
