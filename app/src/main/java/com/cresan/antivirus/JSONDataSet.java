package com.cresan.antivirus;

import android.content.Context;

import com.tech.applications.coretools.FileTools;
import com.tech.applications.coretools.JSonTools;
import com.tech.applications.coretools.MediaTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hexdump on 03/02/16.
 */
abstract public class JSONDataSet<T extends IJSONSerializer> implements IDataSet<T>
{

    public int getItemCount() {return _set.size(); }

    private Set<T> _set;
    public Set<T> getSet() {return _set;}
    void setSet(Set<T> set) { _set=set;}

    String _filePath=null;
    Context _context= null;

    IFactory<T> _nodeFactory=null;

    IDataSetChangesListener _dataSetChangesListener=null;
    public void setDataSetChangesListener(IDataSetChangesListener listener)  { _dataSetChangesListener=listener;   }
    public void unregisterDataSetChangesListener() { _dataSetChangesListener=null;}

    private JSONDataSet()
    {
    }

    public JSONDataSet(Context context, String serializeFileName, IFactory<T> nodeFactory)
    {
        _context=context;
        _nodeFactory=nodeFactory;
        _set=new HashSet<T>();
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
            loadFromJSON();
    }

    //Load WhiteList
    public void loadFromJSON()
    {
        try
        {
            String jsonFile = JSonTools.loadJSONFromFile(_context, _filePath);
            JSONObject obj = new JSONObject(jsonFile);

            JSONArray badAppsArray = obj.getJSONArray("data");

            for (int i = 0; i < badAppsArray.length(); i++)
            {
                JSONObject badAppObj = badAppsArray.getJSONObject(i);
                T bpd = _nodeFactory.createInstance(badAppObj.getString("type"));
                bpd.loadFromJSON(badAppObj);
                _set.add(bpd);
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    //Load WhiteList
    public synchronized void writeToJSON()
    {
        try
        {
            JSONObject jo;
            JSONArray jsonArray=new JSONArray();
            for(T pd : _set)
            {
                jo=pd.buildJSONObject();
                jsonArray.put(jo);
            }

            JSONObject rootObj=new JSONObject();
            rootObj.put("data",jsonArray);

            FileTools.writeTextFile(_filePath, rootObj.toString());
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

    public boolean addItem(T item)
    {
        boolean b=_set.add(item);

        if(b)
        {
            if (_dataSetChangesListener != null)
                _dataSetChangesListener.onSetChanged();
        }
        return b;
    }
    public boolean removeItem(T item)
    {
        boolean b=_set.remove(item);
        if(b)
        {
            if (_dataSetChangesListener != null)
                _dataSetChangesListener.onSetChanged();
        }
        return b;
    }

    public boolean addItems(Collection<? extends T> item)
    {
        boolean b=_set.addAll(item);

        if(b)
        {
            if (_dataSetChangesListener != null)
                _dataSetChangesListener.onSetChanged();
        }
        return b;
    }
}
