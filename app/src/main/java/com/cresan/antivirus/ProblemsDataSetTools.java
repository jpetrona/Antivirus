package com.cresan.antivirus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by hexdump on 04/02/16.
 */
public class ProblemsDataSetTools
{
    public static List<IProblem> getAppProblems(IDataSet<? extends IProblem> problems)
    {
        List<IProblem> appProblem=new ArrayList<IProblem>();

        Set<? extends IProblem> problemSet=problems.getSet();

        for(IProblem p: problemSet)
        {
            if(p.getType()== IProblem.ProblemType.AppProblem)
                appProblem.add(p);
        }

        return appProblem;
    }

    public static List<IProblem> getSystemProblems(IDataSet<? extends IProblem> problems)
    {
        List<IProblem> appProblem=new ArrayList<IProblem>();

        Set<? extends IProblem> problemSet=problems.getSet();

        for(IProblem p: problemSet)
        {
            if(p.getType()== IProblem.ProblemType.SystemProblem)
                appProblem.add(p);
        }

        return appProblem;
    }

    public static List<IProblem> getAppProblems(Collection<IProblem> problems)
    {
        List<IProblem> appProblem=new ArrayList<IProblem>();

        for(IProblem p: problems)
        {
            if(p.getType()== IProblem.ProblemType.AppProblem)
                appProblem.add(p);
        }

        return appProblem;
    }

    public static List<IProblem> getSystemProblems(Collection<IProblem> problems)
    {
        List<IProblem> appProblem=new ArrayList<IProblem>();

        for(IProblem p: problems)
        {
            if(p.getType()== IProblem.ProblemType.SystemProblem)
                appProblem.add(p);
        }

        return appProblem;
    }

    static boolean checkIfPackageInCollection(String packageName, Collection<IProblem> problems)
    {
        for(IProblem p : problems)
        {
            if(p.getType()== IProblem.ProblemType.AppProblem)
            {
                if(((AppProblem)p).getPackageName().equals(packageName))
                    return true;
            }
        }

        return false;
    }
/*
    static boolean isSystemProblemInCollection(Class<? extends SystemProblem> problem, Collection<IProblem> problems)
    {
        for(IProblem p : problems)
        {
            if(p.getType()== IProblem.ProblemType.SystemProblem && p.getClass()==problem.getClass())
            {
                return true;
            }
        }

        return false;
    }*/
}
