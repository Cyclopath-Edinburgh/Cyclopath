/* Copyright (c) 2006-2013 Regents of the University of Minnesota.
 * For licensing terms, see the file LICENSE.
 */
package com.example.cyclopath.gwis;

import android.os.Message;

import com.example.cyclopath.G;
import com.example.cyclopath.conf.Constants;
import com.example.cyclopath.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * Checks whether the Landmarks experiment is active or not.
 * @author Fernando Torre
 */
public class GWIS_LandmarkExpActiveGet extends GWIS {

   /**
    * Constructor
    */
   public GWIS_LandmarkExpActiveGet() {
      super("landmark_exp_active_get");
   }
   
   /**
    * If the experiment is active, refreshes the UI.
    */
   @Override
   protected void processResultset(Document rset) {
      NodeList conditions = rset.getElementsByTagName("lmrk_exp");
      NamedNodeMap atts = conditions.item(0).getAttributes();
      if (XmlUtils.getInt(atts, "active", 0) == 1) {
         G.LANDMARKS_EXP_ON = true;
         // refresh main activity
         if (G.cyclopath_handler != null) {
            Message msg = Message.obtain();
            msg.what = Constants.REFRESH_LANDMARK_EXPERIMENT;
            msg.setTarget(G.cyclopath_handler);
            msg.sendToTarget();
         }
      }
      G.exp_active_loaded = true;
   }
}
