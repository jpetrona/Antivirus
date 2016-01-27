package com.cresan.antivirus;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tech.applications.coretools.MediaTools;
import com.tech.applications.coretools.SerializationTools;


public class AppData implements Serializable
{
    transient final static String filePath="state.data";

    private boolean _voted;
	public boolean getVoted() { return _voted; }
	public void setVoted(boolean voted) { _voted=voted; }

	private boolean _firstScanDone=false;
	public boolean getFirstScanDone() { return _firstScanDone;}
	public void setFirstScanDone(boolean firstScanDone) { _firstScanDone=firstScanDone; }

	private boolean _eulaAccepted=false;
	public boolean getEulaAccepted() { return _eulaAccepted;}
	public void setEulaAccepted(boolean eulaAccepted) { _eulaAccepted=eulaAccepted; }

    /*List<String> _pendingMenaces=null;
    public List<String> getPendingMenaces() { return _pendingMenaces; }
    public void setPendingMenaces(Set<BadPackageData> bprdl)
	{
		_pendingMenaces=new ArrayList<String>();
		for(BadPackageData bprd : bprdl)
		{
			_pendingMenaces.add(bprd.getPackageName());
		}
	}*/

	public AppData() 
	{
		
	}
	
	
	public void serialize(Context ctx)
	{
		try
		{
			SerializationTools.serializeToDataFolder(ctx, this, filePath);
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
