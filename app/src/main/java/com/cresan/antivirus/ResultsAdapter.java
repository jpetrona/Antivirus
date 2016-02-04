package com.cresan.antivirus;

import android.content.Context;
import android.support.v4.content.ContextCompat;
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

public class ResultsAdapter extends ArrayAdapter<IResultsAdapterItem>
{
    final int kHEADER_TYPE=0;
    final int kAPP_TYPE=1;
    final int kSYSTEM_TYPE=2;

    Context _context;

    int _appHeaderIndex=-1;
    int _systemMenacesHeaderIndex =-1;

    private List<IResultsAdapterItem> _selectedProblems=new ArrayList<IResultsAdapterItem>();

    private IResultItemSelecteStateChanged _onItemChangedStateListener =null;
    public void setResultItemSelectedStateChangedListener(IResultItemSelecteStateChanged listemer) { _onItemChangedStateListener =listemer; }

    /*public void removeApps(List<IProblem> appsToRemove)
    {
        _values.removeAll(appsToRemove);
        notifyDataSetChanged();
    }*/

    public List<IProblem> getSelectedProblems()
    {
        List<IProblem> bpdl=new ArrayList<IProblem>();

        int itemCount=getCount();
        int index=0;

        IResultsAdapterItem rai=null;
        for(int i=0; i<itemCount; ++i)
        {
            rai=getItem(i);
            if(rai.getType()!=IResultsAdapterItem.ResultsAdapterItemType.Header)
            {
                ResultsAdapterProblemItem rapi=(ResultsAdapterProblemItem) rai;
                if (rapi.getChecked())
                    bpdl.add(rapi.getProblem());
            }
        }


        return bpdl;
    }

    public ResultsAdapter(Context context, List<IProblem> problems)
    {
        super(context, R.layout.results_list_app_item, new ArrayList<IResultsAdapterItem>());

        _context=context;

        refreshByProblems(problems);
    }

    public void refreshByProblems(List<IProblem> bpdl)
    {
        clear();

        List<IProblem> appProblems=ProblemsDataSetTools.getAppProblems(bpdl);

        if(appProblems.size()>0)
        {
            _appHeaderIndex=0;
            ResultsAdapterHeaderItem headerItem=new ResultsAdapterHeaderItem("Applications");
            add(headerItem);
            _addProblems(appProblems);
        }
        else
            _appHeaderIndex=-1;

        List<IProblem> systemProblems=ProblemsDataSetTools.getSystemProblems(bpdl);

        if(systemProblems.size()>0)
        {
            _systemMenacesHeaderIndex =getCount();
            ResultsAdapterHeaderItem headerItem=new ResultsAdapterHeaderItem("System");
            add(headerItem);
            _addProblems(systemProblems);
        }
        else
            _systemMenacesHeaderIndex=-1;
    }

    public void refreshByResults(List<ResultsAdapterItem> rail)
    {
        clear();
        addAll(rail);
    }



    public void _addProblems(List<IProblem> problems)
    {
        ResultsAdapterProblemItem rapi=null;
        for(IProblem p : problems)
        {
            rapi=new ResultsAdapterProblemItem(p);
            add(rapi);
        }
    }

/*
    public static List<ResultsAdapterProblemItem> buildResultItemsFromProblems(List<IProblem> bpdl,
                                                                         List<ResultsAdapterProblemItem> recycleList)
    {
        List<ResultsAdapterProblemItem> bpdw=recycleList;

        if(bpdw==null)
            bpdw=new ArrayList<ResultsAdapterProblemItem>();
        else
            recycleList.clear();

        for(IProblem bpd: bpdl)
        {
            bpdw.add(new ResultsAdapterAppItem(bpd,false));
        }
        return bpdw;
    }*/
/*
    public void removeByPackageData(PackageData pd)
    {
        IResultsAdapterItem bpdw= ResultsAdapterAppItem.findByPackageName(pd.getPackageName(), _values);
        remove(bpdw);
    }
*/
    public View _createView(final int position, ViewGroup parent)
    {
        int layoutId=-1;

        if(position==_appHeaderIndex || position== _systemMenacesHeaderIndex)
            layoutId=R.layout.results_list_header;
        else if(position< _systemMenacesHeaderIndex)
            layoutId=R.layout.results_list_app_item;
        else
            layoutId=R.layout.results_list_system_item;

        LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v=inflater.inflate(layoutId, parent, false);

        return v;

    }

    public void _fillRowData(int position, View rootView)
    {
        if(position==_appHeaderIndex || position== _systemMenacesHeaderIndex)
        {
            ResultsAdapterHeaderItem obj = (ResultsAdapterHeaderItem)getItem(position);
            ResultsAdapterHeaderItem header=(ResultsAdapterHeaderItem) obj;
            TextView headerText=(TextView) rootView.findViewById(R.id.Titlelabel);
            headerText.setText(header.getDescription());
        }
        else if(position< _systemMenacesHeaderIndex)
        {
            final ResultsAdapterProblemItem ri  = (ResultsAdapterProblemItem)getItem(position);
            final AppProblem ap=ri.getAppProblem();

            TextView textView = (TextView) rootView.findViewById(R.id.Titlelabel);
            TextView riskText = (TextView) rootView.findViewById(R.id.qualityApp);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.logo);
            CheckBox checkBox = (CheckBox) rootView.findViewById(R.id.checkBox);
            checkBox.setChecked(ri.getChecked());

            if(ap.isDangerous())
            {
                riskText.setTextColor(ContextCompat.getColor(getContext(),R.color.HighRiskColor));
                riskText.setText(R.string.high_risk);
            }
            else
            {
                riskText.setTextColor(ContextCompat.getColor(getContext(),R.color.MediumRiskColor));
                riskText.setText(R.string.medium_risk);
            }

            LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
            linearLayout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(_onItemChangedStateListener!=null)
                        _onItemChangedStateListener.onItemSelected(ap);
                }
            });
            imageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (_onItemChangedStateListener != null)
                        _onItemChangedStateListener.onItemSelected(ap);
                }
            });



            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    ri.setChecked(isChecked);
                    if (_onItemChangedStateListener != null)
                    {
                        _onItemChangedStateListener.onItemSelectedStateChanged(isChecked, ap);
                    }
                }
            });

            textView.setText(ActivityTools.getAppNameFromPackage(getContext(), ap.getPackageName()));
            imageView.setImageDrawable(ActivityTools.getIconFromPackage(ap.getPackageName(), getContext()));
        }
        else
        {
            final ResultsAdapterProblemItem ri  = (ResultsAdapterProblemItem)getItem(position);
            final SystemProblem sp=ri.getSystemProblem();

            TextView textView = (TextView) rootView.findViewById(R.id.Titlelabel);
            TextView riskText = (TextView) rootView.findViewById(R.id.qualityApp);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.logo);
            CheckBox checkBox = (CheckBox) rootView.findViewById(R.id.checkBox);

            if(sp.isDangerous())
            {
                riskText.setTextColor(ContextCompat.getColor(getContext(),R.color.HighRiskColor));
                riskText.setText(R.string.high_risk);
            }
            else
            {
                riskText.setTextColor(ContextCompat.getColor(getContext(),R.color.MediumRiskColor));
                riskText.setText(R.string.medium_risk);
            }

            LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
            linearLayout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(_onItemChangedStateListener!=null)
                        _onItemChangedStateListener.onItemSelected(sp);
                }
            });
            imageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (_onItemChangedStateListener != null)
                        _onItemChangedStateListener.onItemSelected(sp);
                }
            });

            textView.setText(sp.getTitle(getContext()));
            imageView.setImageDrawable(sp.getIcon(getContext()));
        }
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {

        final View rowView;

        if(convertView == null)
            rowView=_createView(position,parent);
        else
            rowView = convertView;


        _fillRowData(position, rowView);

        /*final ResultsAdapterAppItem obj = _values.get(position);
        TextView textView = (TextView) rowView.findViewById(R.id.Titlelabel);
        TextView riskText = (TextView) rowView.findViewById(R.id.qualityApp);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
        CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);
        checkBox.setChecked(obj.checked);*/



        return rowView;

    }

    @Override
    public int getViewTypeCount() { return 2; }

    @Override
    public int getItemViewType(int position)
    {
        if(position==_appHeaderIndex || position== _systemMenacesHeaderIndex)
            return kHEADER_TYPE;
        else if(position< _systemMenacesHeaderIndex)
            return kAPP_TYPE;
        else
            return kSYSTEM_TYPE;
    }


    public int getRealCount()
    {
        int count=super.getCount();
        if(_appHeaderIndex!=-1)
            --count;
        if(_systemMenacesHeaderIndex !=-1)
            --count;

        return count;
    }
}
