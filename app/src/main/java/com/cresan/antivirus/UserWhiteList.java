package com.cresan.antivirus;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.provider.MediaStore;

import com.tech.applications.coretools.FileTools;
import com.tech.applications.coretools.JSonTools;
import com.tech.applications.coretools.MediaTools;

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
public class UserWhiteList
{
    private HashSet<PackageData> _whiteList;
    HashSet<PackageData> getUserWhiteList() {return _whiteList;}

    String _filePath=null;

    Context _context= null;

    public UserWhiteList(Context context)
    {
        _context=context;
        _whiteList=new HashSet<PackageData>();
        _filePath= MediaTools.getInternalDataPath(_context)+ File.separatorChar+"userwhitelist.json";

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

    boolean checkIfPackageInWhiteList(String packageName)
    {
        return PackageData.isPackageInListByName(_whiteList,packageName);
    }

    public void addPackage(PackageData pd)
    {
        _whiteList.add(pd);
    }

    public void clear()
    {
        _whiteList.clear();
    }


    //Load WhiteList
    public void loadData()
    {
        try
        {
            String jsonFile =JSonTools.loadJSONFromFile(_context, _filePath);
            JSONObject obj = new JSONObject(jsonFile);

            JSONArray m_jArry = obj.getJSONArray("data");

            for (int i = 0; i < m_jArry.length(); i++)
            {
                JSONObject temp = m_jArry.getJSONObject(i);
                PackageData pd = new PackageData();
                pd.setPackageName(temp.getString("packageName"));
                _whiteList.add(pd);
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    //Load WhiteList
    public void writeData()
    {
        try
        {
            JSONObject jo;
            JSONArray jsonArray=new JSONArray();
            for(PackageData pd : _whiteList)
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


    public List<PackageInfo> removePackagesFromPackageList(List<PackageInfo> packagesToPurge)
    {
        boolean found=false;

        List<PackageInfo> trimmedPackageList=new ArrayList<PackageInfo>(packagesToPurge);

        //Check against whitelist
        for(PackageData pd : _whiteList)
        {
            PackageInfo p = null;
            int index = 0;
            String packageName = pd.getPackageName();
            found = false;

            while (found == false && index < trimmedPackageList.size())
            {
                p = trimmedPackageList.get(index);
                if (MainFragment.packageNameBelongsToPackageMask(p.packageName,pd.getPackageName()))
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
