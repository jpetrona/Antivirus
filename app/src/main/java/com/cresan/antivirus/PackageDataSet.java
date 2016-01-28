package com.cresan.antivirus;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.provider.MediaStore;

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
public class PackageDataSet
{
    private Set<PackageData> _set;
    Set<PackageData> getSet() {return _set;}
    void setSet(Set<PackageData> set) { _set=set;}

    /*void setSetWithBadPackageList(Set<BadPackageResultData> bpdl)
    {
        _set.clear();
        for(BadPackageResultData bpd : bpdl)
        {
            PackageData pd=new PackageData();
            pd.setPackageName(bpd.getPackageName());
            _set.add(pd);
        }
    }*/

    String _filePath=null;

    Context _context= null;

    public PackageDataSet(Context context, String serializeFileName)
    {
        _context=context;
        _set=new HashSet<PackageData>();
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
        return PackageData.isPackageInListByName(_set,packageName);
    }

    public void addPackage(PackageData pd)
    {
        _set.add(pd);
    }
    public void addAllPackages(Set<PackageData> packagesDataToAdd) { _set.addAll(packagesDataToAdd);}

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

            JSONArray m_jArry = obj.getJSONArray("data");

            for (int i = 0; i < m_jArry.length(); i++)
            {
                JSONObject temp = m_jArry.getJSONObject(i);
                PackageData pd = new PackageData(temp.getString("packageName"));
                _set.add(pd);
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
            for(PackageData pd : _set)
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


    public List<PackageInfo> removeMyPackagesFromPackageList(List<PackageInfo> packagesToPurge)
    {
        boolean found=false;

        List<PackageInfo> trimmedPackageList=new ArrayList<PackageInfo>(packagesToPurge);

        //Check against whitelist
        for(PackageData pd : _set)
        {
            PackageInfo p = null;
            int index = 0;
            String packageName = pd.getPackageName();
            found = false;

            while (found == false && index < trimmedPackageList.size())
            {
                p = trimmedPackageList.get(index);
                if (StringTools.stringMatchesMask(p.packageName, pd.getPackageName()))
                {
                    found = true;
                    trimmedPackageList.remove(index);
                }
                else
                    ++index;
            }
        }

        return trimmedPackageList;
    }
}
