package com.example.abuser_stop_abusing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

public class WifiDirectBroadcast extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mMain;

    public WifiDirectBroadcast(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MainActivity mMain) {
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.mMain = mMain;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "Wifi is On", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(context, "Wifi is not on, please turn on your wifi to use this app", Toast.LENGTH_LONG).show();
            }
        }
        else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //TODO: implement task happen when peer change occur
        }
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            //TODO: implement task happen when connection state change occur
        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            //TODO: respond to this device wifi state changing
        }
    }
}
