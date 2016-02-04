package com.cresan.antivirus;

import java.util.List;

/**
 * Created by hexdump on 29/01/16.
 */
abstract class ResultsAdapterItem implements IResultsAdapterItem
{
    private boolean _checked=false;
    public boolean getChecked() { return _checked; }
    public void setChecked(boolean checked) { _checked=checked;}
}
