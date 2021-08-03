package com.example.MVMcR_MA_QCR;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;

public class MyService extends Service
{
    private static BroadcastReceiver broadcastReceiver;

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        receiver();
    }



    @Override
    public void onDestroy()
    {
        unregisterReceiver(new NetworkStateChecker());
        //broadcastReceiver = null;
        //receiver();
    }


    private void receiver()
    {
        broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                //Log.d(TAG, "ACTION_SCREEN_OFF");
                // do something, e.g. send Intent to main app
            }
        };
        //IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        //registerReceiver(broadcastReceiver, filter);

        //registering the broadcast receiver to update sync status
        registerReceiver(broadcastReceiver, new IntentFilter(Main_page.DATA_SAVED_BROADCAST));
        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
}