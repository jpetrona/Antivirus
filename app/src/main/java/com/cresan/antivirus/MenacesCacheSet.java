package com.cresan.antivirus;

import android.content.Context;

import java.util.Set;


/**
 * Created by hexdump on 22/01/16.
 */
public class MenacesCacheSet extends JSONDataSet<IProblem>
{
    public MenacesCacheSet(Context context)
    {
        super(context,"menacescache.json",new ProblemFactory());
    }

    public boolean removeAppProblemByPackage(String packageName)
    {
        Set<IProblem> set=getSet();

        AppProblem problem=null;

        for(IProblem p: set)
        {
            if(p.getType()== IProblem.ProblemType.AppProblem)
            {
                problem=(AppProblem) p;
                if(packageName.equals(((AppProblem) p).getPackageName()))
                {
                    return removeItem(p);
                }
            }
        }

        return false;
    }
}
