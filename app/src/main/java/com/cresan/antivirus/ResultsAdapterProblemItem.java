package com.cresan.antivirus;

/**
 * Created by hexdump on 29/01/16.
 */
class ResultsAdapterProblemItem extends ResultsAdapterItem
{
    IProblem _problem=null;

    public ResultsAdapterProblemItem(IProblem problem)
    {
        _problem=problem;
    }

    public IProblem getProblem() { return _problem; }

    public AppProblem getAppProblem() throws ClassCastException
    {
        if(_problem.getClass()==AppProblem.class)
            return (AppProblem)_problem;
        else
            throw new ClassCastException();
    }

    public SystemProblem getSystemProblem() throws ClassCastException
    {
        if(_problem.getClass()==SystemProblem.class)
            return (SystemProblem)_problem;
        else
            throw new ClassCastException();
    }


    public ResultsAdapterItemType getType() { return _problem.getType()== IProblem.ProblemType.AppProblem ? ResultsAdapterItemType.AppMenace : ResultsAdapterItemType.SystemMenace;}
}
