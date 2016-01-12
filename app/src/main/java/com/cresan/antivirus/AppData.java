package com.cresan.antivirus;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tech.applications.coretools.MediaTools;
import com.tech.applications.coretools.SerializationTools;


public class AppData implements Serializable
{
	private boolean _voted;
	public boolean getVoted() { return _voted; }
	public void setVoted(boolean voted) { _voted=voted; }
	
	
	public AppData() 
	{
		
	}
	
	
	public void serialize(Context ctx)
	{
		try
		{
			SerializationTools.serializeToSharedPrefs(ctx,"bc_data",this);
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
