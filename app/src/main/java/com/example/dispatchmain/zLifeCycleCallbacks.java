package com.example.dispatchmain;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class zLifeCycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private boolean isAppInForeground = false;

    public boolean isAppInForeground() {
        return isAppInForeground;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        isAppInForeground = true;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        isAppInForeground = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}
