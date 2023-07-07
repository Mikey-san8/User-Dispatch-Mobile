package com.example.dispatchmain;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

public class zUtil
{
    public static boolean isMyServiceRunning(Class<?> serviceClass, Activity mActivity)
    {
        ActivityManager manager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }
}
