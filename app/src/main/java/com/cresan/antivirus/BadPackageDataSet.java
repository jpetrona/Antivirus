/**
 * Created by hexdump on 27/01/16.
 */
package com.cresan.antivirus;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.tech.applications.coretools.FileTools;
import com.tech.applications.coretools.JSonTools;
import com.tech.applications.coretools.MediaTools;
import com.tech.applications.coretools.StringTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by hexdump on 22/01/16.
 */
public class BadPackageDataSet
{
    private Set<BadPackageData> _set;
    Set<BadPackageData> getSet() {return _set;}
    void setSet(Set<BadPackageData> set) { _set=set;}

    String _filePath=null;

    Context _context= null;

    public BadPackageDataSet(Context context, String serializeFileName)
    {
        _context=context;
        _set=new HashSet<BadPackageData>();
        _filePath= MediaTools.getInternalDataPath(_context)+ File.separatorChar+serializeFileName;

        //Generate file if it does not exist
        if(!MediaTools.existsFile(_filePath))
        {
            try
            {
                FileTools.writeTextFile(_filePath, "{\n" +
                        "  \"data\": [" +
                        "  ]\n" +
                        "}\n");
            }
            catch(IOException ioEx)
            {
                ioEx.printStackTrace();
            }
        }
        else
            loadData();
    }

    boolean checkIfPackageInList(String packageName)
    {
        for(BadPackageData bpd : _set)
        {
            if(bpd.getPackageName().equals(packageName))
                return true;
        }

        return false;
    }

    public void addPackage(BadPackageData pd)
    {
        _set.add(pd);
    }

    public void addPackages(Set<BadPackageData> packagesDataToAdd) { _set.addAll(packagesDataToAdd);}

    public void removePackage(BadPackageData pd)
    {
        _set.remove(pd);
    }

    public void clear()
    {
        _set.clear();
    }


    //Load WhiteList
    public void loadData()
    {
        try
        {
            String jsonFile = JSonTools.loadJSONFromFile(_context, _filePath);
            JSONObject obj = new JSONObject(jsonFile);

            JSONArray badAppsArray = obj.getJSONArray("data");

            for (int i = 0; i < badAppsArray.length(); i++)
            {
                JSONObject badAppObj = badAppsArray.getJSONObject(i);
                BadPackageData bpd = new BadPackageData("tempname");
                bpd.loadFromJSON(badAppObj);
                _set.add(bpd);
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    //Load WhiteList
    public synchronized void writeData()
    {
        try
        {
            JSONObject jo;
            JSONArray jsonArray=new JSONArray();
            for(BadPackageData pd : _set)
            {
                jo=pd.buildJSONObject();
                jsonArray.put(jo);
            }

            JSONObject rootObj=new JSONObject();
            rootObj.put("data",jsonArray);

            FileTools.writeTextFile(_filePath,rootObj.toString());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
