package com.cresan.antivirus;

import android.content.pm.PackageInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by hexdump on 15/01/16.
 */
public class BadPackageData extends PackageData
{
    public BadPackageData(String packageName)
    {
        super(packageName);
    }

    private Set<ActivityData> _activities=new HashSet<ActivityData>();
    public void addActivityData(ActivityData activityData)  { _activities.add(activityData);  }
    public Set<ActivityData> getActivityData() { return _activities; }

    private Set<PermissionData> _permissions=new HashSet<PermissionData>();
    public void addPermissionData(PermissionData bad) { _permissions.add(bad);  }
    public Set<PermissionData> getPermissionData() { return _permissions; }

    private boolean _installedThroughGooglePlay=false;
    public boolean getInstalledThroughGooglePlay() { return _installedThroughGooglePlay; }
    public void setInstalledThroughGooglePlay(boolean installed) { _installedThroughGooglePlay=installed;}


    public boolean isMenace()
    {
        return !getInstalledThroughGooglePlay() || getActivityData().size()>0 || getPermissionData().size()>0;
    }

    public boolean isDangerousMenace()
    {
        for(PermissionData pd : _permissions)
        {
            if(pd.getDangerous()==1)
                return true;
        }

        return false;
    }

    public JSONObject buildJSONObject() throws JSONException
    {
        JSONObject jsonObj=new JSONObject();
        jsonObj.put("packageName",getPackageName());

        //Add activities
        JSONArray activitiesArray=new JSONArray();
        for(ActivityData ad : _activities)
        {
            JSONObject activityObject=new JSONObject();
            activityObject.put("packageName",ad.getPackage());
            activitiesArray.put(activityObject);
        }
        jsonObj.put("activities",activitiesArray);

        //Add permissions
        JSONArray permissionsArray=new JSONArray();
        for(PermissionData pd : _permissions)
        {
            JSONObject permissionObject=new JSONObject();
            permissionObject.put("permissionName", pd.getPermissionName());
            permissionObject.put("dangerous",pd.getDangerous());
            permissionsArray.put(permissionObject);
        }
        jsonObj.put("permissions",permissionsArray);

        return jsonObj;
    }

    public void loadFromJSON(JSONObject appObject)
    {
        try
        {
            String appPackageName = appObject.getString("packageName");
            setPackageName(appPackageName);
            _loadActivitesFromJSON(appObject);
            _loadPermissionsFromJSON(appObject);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }


    private void _loadActivitesFromJSON(JSONObject appObject)
    {
        try
        {
            JSONArray activitiesArray = appObject.getJSONArray("activities");

            for (int i = 0; i < activitiesArray.length(); i++)
            {
                JSONObject temp = activitiesArray.getJSONObject(i);
                String packageName = temp.getString("packageName");
                ActivityData ad = new ActivityData(packageName);
                addActivityData(ad);
            }
        }
        catch(JSONException ex)
        {
            ex.printStackTrace();
        }
    }

    private void _loadPermissionsFromJSON(JSONObject appObject)
    {
        try
        {
            JSONArray activitiesArray = appObject.getJSONArray("permissions");

            for (int i = 0; i < activitiesArray.length(); i++)
            {
                JSONObject temp = activitiesArray.getJSONObject(i);
                String permissionName = temp.getString("permissionName");
                int dangerous=temp.getInt("dangerous");
                PermissionData pd = new PermissionData(permissionName,dangerous);
                addPermissionData(pd);
            }
        }
        catch(JSONException ex)
        {
            ex.printStackTrace();
        }
    }
}
