/* Copyright (c) 2006-2011 Regents of the University of Minnesota.
 * For licensing terms, see the file LICENSE.
 */
package com.example.cyclopath.net;

/**
 * An interface for classes that can handle a bit.ly url shorten request.
 * @author Phil Brown
 */
public interface LinkShortenerCallback {
   /**
    * This method is called once the bit.ly request has completed
    * @param newURL
    */
   public void handleLinkShortenerComplete(String newURL);
}//LinkShortenerCallback
