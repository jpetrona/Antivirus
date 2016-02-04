package com.cresan.antivirus;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.cresan.androidprotector.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hexdump on 15/01/16.
 */
public abstract class SystemProblem implements IProblem
{

    //Factory method
    public SystemProblem()
    {
    }

    public ProblemType getType() { return ProblemType.SystemProblem;}

    public JSONObject buildJSONObject() throws JSONException
    {
        JSONObject jsonObj=new JSONObject();
        jsonObj.put("type",getSerializationTypeString());

        return jsonObj;
    }

    public void loadFromJSON(JSONObject appObject)
    {
        /*try
        {
            String systemTypeOrdinal = appObject.getString("type");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }*/
    }

    public void writeToJSON(String filePath)
    {
    }

    abstract public String getWhiteListOnAddDescription(Context context);
    abstract public String getWhiteListOnRemoveDescription(Context context);
    abstract public String getTitle(Context context);
    abstract public String getSubTitle(Context context);
    abstract public String getDescription(Context context);
    abstract public Drawable getIcon(Context context);
    abstract public Drawable getSubIcon(Context context);
    abstract public String getSerializationTypeString();

}
