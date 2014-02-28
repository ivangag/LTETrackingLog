package com.gagi.app.ltetrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

    final  String TAG = "com.gagi.app.ltetrack";
    public NetworkChangeReceiver()
    {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");

        if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
        {

            NetworkInfo networkInfo = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if(networkInfo.getSubtypeName().contains("LTE")
                    && networkInfo.isConnected())
            {
                Vibrator v = (Vibrator) context
                        .getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
            }
            sendMessage(context,networkInfo);
        }
    }

        // Send an Intent with an action named "custom-event-name". The Intent sent should
        // be received by the ReceiverActivity.
    private void sendMessage(Context context, NetworkInfo networkInfo)
    {
        Log.i(TAG, "Broadcasting message sending -> CUSTOM_INTENT_CONNECTIVITY_CHANGED");
        Intent intent = new Intent(MainActivity.CUSTOM_INTENT_CONNECTIVITY_CHANGED);
        // You can also include some extra data.
        intent.putExtra(MainActivity.CUSTOM_EXTRA_NETWORK_INFO, networkInfo);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
