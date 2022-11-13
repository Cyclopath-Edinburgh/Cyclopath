/* Copyright (c) 2006-2013 Regents of the University of Minnesota.
 * For licensing terms, see the file LICENSE.
 */

package com.example.cyclopath.gwis;

import com.example.cyclopath.items.ItemUserAccess;

import java.util.ArrayList;

/**
 * A callback interface for handling checkout.
 * @author Fernando Torre
 */
public interface GWIS_CheckoutCallback {

   /**
    * This method is called once a checkout request has been processed.
    */
   public void handleGWIS_CheckoutComplete(
                     ArrayList<ItemUserAccess> items);
   
}
