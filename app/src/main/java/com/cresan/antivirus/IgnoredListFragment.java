package com.cresan.antivirus;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.cresan.androidprotector.R;
import com.tech.applications.coretools.ActivityTools;
import com.tech.applications.coretools.StringTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Magic Frame on 27/01/2016.
 */
public class IgnoredListFragment extends Fragment
{

    AntivirusActivity getMainActivity() {return (AntivirusActivity) getActivity();}
    IgnoredAdapter _ignoredAdapter =null;
    List<IProblem> _userWhiteList =null;
    private ListView _listView;
    private TextView _ignoredCounter;

    public void setData(List<IProblem> userWhiteList)
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

        _ignoredAdapter.setOnAdapterItemRemovedListener(new IOnAdapterItemRemoved<IProblem>()
        {
            @Override
            public void onItemRemoved(IProblem item)
            {
                UserWhiteList userWhiteList= getMainActivity().getUserWhiteList();
                MenacesCacheSet menaceCacheSet= getMainActivity().getMenacesCacheSet();
                userWhiteList.removeItem(item);
                userWhiteList.writeToJSON();
                menaceCacheSet.addItem(item);
                menaceCacheSet.writeToJSON();
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


    //getExistant problems
    static public List<IProblem> getExistingProblems(Context context, List<IProblem> ignoredList)
    {
        //Create list of apps that are whitelisted and installed
        List<IProblem> existingProblems=new ArrayList<IProblem>();

        for(IProblem p : ignoredList)
        {
            if(p.getType()== IProblem.ProblemType.AppProblem)
            {
                AppProblem appProblem= (AppProblem) p;
                if(ActivityTools.isPackageInstalled(context, appProblem.getPackageName()))
                    existingProblems.add(appProblem);
            }
            else
                existingProblems.add(p);
        }

        return existingProblems;
    }



    @Override
    public void onResume()
    {
        super.onResume();

        List<IProblem> list=getExistingProblems(getMainActivity(), new ArrayList(getMainActivity().getUserWhiteList().getSet()));
        _userWhiteList=list;
        _ignoredAdapter.refresh(list);

        _updateFoundThreatsText(_ignoredCounter,_userWhiteList.size());

    }
}
