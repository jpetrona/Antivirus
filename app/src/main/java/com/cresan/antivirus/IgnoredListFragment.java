package com.cresan.antivirus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.cresan.androidprotector.R;
import com.tech.applications.coretools.StringTools;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Magic Frame on 27/01/2016.
 */
public class IgnoredListFragment extends Fragment
{

    AntivirusActivity getMainActivity() {return (AntivirusActivity) getActivity();}
    IgnoredAdapter _ignoredAdapter =null;
    List<BadPackageData> _userWhiteList =null;
    private ListView _listView;
    private TextView _ignoredCounter;

    public void setData(List<BadPackageData> userWhiteList)
    {
        _userWhiteList = userWhiteList;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.ignored_list_fragment, container, false);
        _listView = (ListView)rootView.findViewById(R.id.ignoredList);
        _ignoredAdapter=new IgnoredAdapter(getMainActivity(), _userWhiteList);
        _ignoredCounter = (TextView) rootView.findViewById(R.id.ignoredCounterText);

        _ignoredAdapter.setOnAdapterItemRemovedListener(new IOnAdapterItemRemoved<BadPackageData>()
        {
            @Override
            public void onItemRemoved(BadPackageData item)
            {
                UserWhiteList userWhiteList= getMainActivity().getUserWhiteList();
                MenacesCacheSet menaceCacheSet= getMainActivity().getMenacesCacheSet();
                userWhiteList.removePackage(item);
                userWhiteList.writeData();
                menaceCacheSet.addPackage(item);
                menaceCacheSet.writeData();
                _updateFoundThreatsText(_ignoredCounter,_userWhiteList.size());
            }
        });
        _listView.setAdapter(_ignoredAdapter);
        _updateFoundThreatsText(_ignoredCounter,_userWhiteList.size());
        return rootView;
    }

    void _updateFoundThreatsText(TextView textView, int appCount)
    {
        String finalStr=getString(R.string.ignored_counter);
        finalStr= StringTools.fillParams(finalStr, "#", Integer.toString(appCount));
        textView.setText(finalStr);
    }
}
