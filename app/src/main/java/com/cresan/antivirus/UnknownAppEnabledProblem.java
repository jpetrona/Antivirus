package com.cresan.antivirus;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.cresan.androidprotector.R;

/**
 * Created by Magic Frame on 04/02/2016.
 */
public class UnknownAppEnabledProblem extends  SystemProblem
{

    static public final String kSerializationType="unknownApp";


    final int kUnknownAppIconResId = R.drawable.information;
    final int kUnknownAppDescriptionId =R.string.unknownApp_message;
    final int kUnknownAppTitleId =R.string.usb_title;
    final int kWhiteListAddText= R.string.unknownApp_add_whitelist_message;
    final int kWhiteListRemoveText=R.string.unknownApp_remove_whitelist_message;


    public UnknownAppEnabledProblem()
    {


    }


    public ProblemType getType() { return ProblemType.SystemProblem;}

    public String getSerializationTypeString() {return kSerializationType; }


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
        return context.getString(kUnknownAppTitleId);
    }

    public String getDescription(Context context)
    {
        return context.getString(kUnknownAppDescriptionId);
    }

    public Drawable getIcon(Context context)
    {
        return ContextCompat.getDrawable(context, kUnknownAppIconResId);
    }

    public boolean isDangerous()
    {
        return false;
    }
}
