/* Copyright (c) 2006-2011 Regents of the University of Minnesota.
 * For licensing terms, see the file LICENSE.
 */

package com.example.cyclopath.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Message;

import com.example.cyclopath.G;
import com.example.cyclopath.conf.Constants;
import com.example.cyclopath.gwis.GWIS;

/**
 * Handles network (wifi/cellular) changes by redrawing the map
 * @author Phil Brown
 */
public class NetworkListener extends BroadcastReceiver {

   @Override
   public void onReceive(Context context, Intent intent) {
      if (G.isConnected() &&
          intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
         if (G.cyclopath_handler != null) {
            Message msg = Message.obtain();
            msg.what = Constants.REFRESH_NEEDED;
            msg.setTarget(G.cyclopath_handler);
            msg.sendToTarget();
         }
         GWIS.retryAll();
      }
   }//onReceive

}//NetworkListener
