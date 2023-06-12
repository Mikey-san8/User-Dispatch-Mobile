package com.example.dispatchmain;

import android.app.Application;
import android.content.Intent;

public class zDispatchApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new RestartingExceptionHandler());
    }

    private class RestartingExceptionHandler implements Thread.UncaughtExceptionHandler
    {
        @Override
        public void uncaughtException(Thread thread, Throwable throwable)
        {
            Intent intent = new Intent(getApplicationContext(), FragmentHome.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            System.exit(0);
        }
    }
}
