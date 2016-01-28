package com.cresan.antivirus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.cresan.androidprotector.R;

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
            }
        });
        _listView.setAdapter(_ignoredAdapter);
        return rootView;
    }
}
