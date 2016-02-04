package com.cresan.antivirus;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.cresan.androidprotector.R;
import com.tech.applications.coretools.ActivityTools;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hexdump on 15/01/16.
 */
public class DebugUSBEnabledProblem implements IProblem
{
    static final String kUSBProblemType ="USB";
    static public final String kSerializationType="usb";

    final int kUsbIconResId= R.drawable.usb_icon;
    final int kUsbDescriptionId=R.string.usb_message;
    final int kUsbTitleId=R.string.system_app_usb_menace_title;
    final int kWhiteListAddText= R.string.usb_add_whitelist_message;
    final int kWhiteListRemoveText=R.string.usb_remove_whitelist_message;
    final boolean kUSBIsDangerousMenace=false;



    //Factory method
    public DebugUSBEnabledProblem()
    {
    }

    public String getSystemProblemType() { return kUSBProblemType; }

    public ProblemType getType() { return ProblemType.SystemProblem;}

    public JSONObject buildJSONObject() throws JSONException
    {
        JSONObject jsonObj=new JSONObject();
        jsonObj.put("type",kSerializationType);

        return jsonObj;
    }

    public void loadFromJSON(JSONObject appObject)
    {
        try
        {
            String systemTypeOrdinal = appObject.getString("type");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void writeToJSON(String filePath)
    {
    }

    public String getWhiteListOnAddDescription(Context context)
    {
        return context.getString(kWhiteListAddText);
    }

    public String getWhiteListOnRemoveDescription(Context context)
    {
        return context.getString(kWhiteListRemoveText);
    }

    public String getTitle(Context context)
    {
        return context.getString(kUsbTitleId);
    }

    public String getDescription(Context context)
    {
        return context.getString(kUsbDescriptionId);
    }

    public Drawable getIcon(Context context)
    {
        return ContextCompat.getDrawable(context,kUsbIconResId);
    }

    public boolean isDangerous()
    {
        return false;
    }
    
    static public boolean existsInSystem(Context context)
    {
        return ActivityTools.checkIfUSBDebugIsEnabled(context);
    }
}
