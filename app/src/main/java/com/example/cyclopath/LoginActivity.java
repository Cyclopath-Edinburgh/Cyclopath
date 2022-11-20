/* Copyright (c) 2006-2011 Regents of the University of Minnesota.
 * For licensing terms, see the file LICENSE.
 */

package com.example.cyclopath;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.cyclopath.conf.Constants;
import com.example.cyclopath.gwis.GWIS_Hello;
import com.example.cyclopath.gwis.GWIS_HelloCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity that handles user login. <br>
 * @author Fernando Torre
 * @author Phil Brown
 */
public class LoginActivity extends BaseActivity
                           implements OnClickListener,
                                      GWIS_HelloCallback {

   // *** Listeners

   public FirebaseFirestore db;
   /**
    * Creates and initialize login window.
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      setContentView(R.layout.login);

      db = FirebaseFirestore.getInstance();

      Button button = (Button)findViewById(R.id.login_btn);
      button.setOnClickListener(this);
      
      ((EditText) findViewById(R.id.login_username_txt)).setText(
                                                             G.user.getName());
      
      G.buildURLLink((TextView) findViewById(R.id.new_user_link),
                     Constants.CREATE_ACCOUNT_URL,
                     getResources().getString(R.string.sign_up),
                     Constants.URL_LINK_COLOR);
      G.buildURLLink((TextView) findViewById(R.id.forgot_password_link),
                     Constants.FORGOT_PASSWORD_URL,
                     getResources().getString(R.string.forgot_password),
                     Constants.URL_LINK_COLOR);
   }

   /**
    * Handles button clicks.
    */
   @Override
   public void onClick(View v) {
      if (v == findViewById(R.id.login_btn)) {
         submit();
      }
   }

   /**
    * Cleans up before closing the activity. Cannot be done in onDestroy,
    * because onDestroy is also called when rotating the device.
    */
   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_BACK) {
         if (G.user.reauthenticating)
            G.user.logout();
         setResult(Activity.RESULT_CANCELED);
      }
      return super.onKeyDown(keyCode, event);
   }
   
   // *** Other methods
   
   /**
    * Handles the completion of the login request. Sends the result back to
    * the activity that called this one.
    */
   @Override
   public void handleGWIS_HelloComplete(String username,
                                        String token,
                                        Node preferences,
                                        boolean rememberme) {
      G.user.finishLogin(username, token, preferences, rememberme);
      if (getParent() == null) {
         setResult(Activity.RESULT_OK, null);
      } else {
         getParent().setResult(Activity.RESULT_OK, null);
      }
      finish();
   }

   /**
    * Begins the login process.
    */
   public void loginStart(String username,
                          String password,
                          boolean rememberme) {
      GWIS_Hello g = new GWIS_Hello(username, password, rememberme, this);
      g.fetch();
   }
   
   /**
    * If the username and password are validated, begins loging process.
    */
   public void submit() {
      String username =
        ((EditText) findViewById(R.id.login_username_txt)).getText().toString();
      String password =
        ((EditText) findViewById(R.id.login_password_txt)).getText().toString();
      CheckBox cb = (CheckBox) findViewById(R.id.login_remember);
      if (username.length() == 0) {
         showAlert(
           this.getResources().getString(R.string.login_username_not_validated),
           this.getResources().getString(R.string.error));
         return;
      } else if (password.length() == 0) {
         showAlert(
           this.getResources().getString(R.string.login_password_not_validated),
           this.getResources().getString(R.string.error));
         return;
      }
      login(username, password);
      //this.loginStart(username.toLowerCase(), password, cb.isChecked());
   }

   public void login(String username, String password) {
      System.out.println(isNetworkAvailable(LoginActivity.this));
      DocumentReference docRef = db.collection("users").document(username);
      docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
         @Override
         public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
               DocumentSnapshot document = task.getResult();
               if (document.exists()) {
                  Toast.makeText(LoginActivity.this, "Successfully login!", Toast.LENGTH_SHORT).show();
               } else {
                  Map<String, Object> user = new HashMap<>();
                  user.put("password", password);
                  db.collection("users").document(username)
                          .set(user)
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                             @Override
                             public void onSuccess(Void aVoid) {
                                Toast.makeText(LoginActivity.this, "Successfully sign up!", Toast.LENGTH_SHORT).show();
                             }
                          })
                          .addOnFailureListener(new OnFailureListener() {
                             @Override
                             public void onFailure(@NonNull Exception e) {

                             }
                          });
               }
            } else {

            }
         }
      });
   }

   public boolean isNetworkAvailable (final Context context) {
      final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
      return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
   }
}
