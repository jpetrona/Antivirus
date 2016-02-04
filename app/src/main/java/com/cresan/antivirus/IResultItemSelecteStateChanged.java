package com.cresan.antivirus;

/**
 * Created by Magic Frame on 19/01/2016.
 */
public interface IResultItemSelecteStateChanged
{
    public void onItemSelectedStateChanged(boolean isChecked, IProblem bpd);
    public void onItemSelected(IProblem bpdw);
}
