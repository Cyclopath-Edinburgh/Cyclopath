/* Copyright (c) 2006-2011 Regents of the University of Minnesota.
   For licensing terms, see the file LICENSE.
 */

package com.example.cyclopath;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.cyclopath.conf.Constants;
import com.example.cyclopath.gwis.GWIS_Commit;
import com.example.cyclopath.items.ConflationJob;
import com.example.cyclopath.items.Geopoint;
import com.example.cyclopath.items.ItemUserAccess;
import com.example.cyclopath.items.LandmarkNeed;
import com.example.cyclopath.util.TrackExporter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Main Class for Cyclopath Mobile. <br>
 * Grouplens Research<br>
 * University of Minnesota
 * @author Phil Brown
 * @author Fernando Torre
 */
public class Cyclopath<LOCATION_PERMISSION_REQUEST_CODE>  extends AppCompatActivity { // Base Activity
                      // implements //LocationListener,
                                  //GpsStatus.Listener,
                                  //OnClickListener,
                                  //TrackDeletionHandler,
                                  //GWIS_RouteGetCallback,
                                  //SensorEventListener,
                                  //GWIS_CheckoutCallback,
                                  //DialogInterface.OnClickListener,
                                  //GWIS_LandmarkTrialGetCallback {
   
   /** True if the app is currently tracking GPS coordinates. 
    * Used for various features, including menu configuration.*/
   public boolean is_recording;
   /** This is used to avoid opening multiple dialogs asking the user
    * if they want to save a track. */
   public boolean stop_recording_msg_sent = false;
   
   /** Mapping of user preferences. This is derived from the XML file.*/
   protected Map<String,Boolean> saved_user_settings;
   /** Provides editing capabilities for the user settings file.*/
   protected SharedPreferences.Editor user_settings_editor;
   
   /** Provides access to location services.*/
   protected LocationManager locationManager;
   
   /** Throbber animation */
   protected AnimationDrawable throbber_anim;
   
   /** Button used to open the context menu*/
   private ImageButton contextButton;
   /** View containing directions*/
   private View directionsView;
   /** If using version 3.0 or higher, this will invoke the method
    * invalidateOptionsMenu() from Activity. */
   private static Method nullifyOptionsMenu;
   
   /** Landmarks Experiment prompt */
   AlertDialog landmark_prompt;

   /** Manager for listening to sensor updates */
   private SensorManager mSensorManager;
   /** accelerometer sensor (used for calculating orientation)*/
   private Sensor accelerometerSensor;
   /** magnetic field sensor (used for calculating orientation)*/
   private Sensor magneticFieldSensor;
   /** acceleration values returned by the accelerometer sensor */
   private float[] accelerationValues;
   /** magnetic field values returned by the magnetic field sensor */
   private float[] magneticFieldValues;
   
   /** Constant meaning that GPS location is required for some action. */
   protected static final int GPS_REQUIRED = 1;
   /** Constant meaning that GPS location is required specifically for track
    * recording. */
   protected static final int GPS_REQUIRED_RECORDING = 2;
   /** Constant meaning that Network location is required for some action. */
   protected static final int NETWORK_REQUIRED = 3;
   /** Constant meaning that both GPS and Network locations are required for
    * some action. */
   protected static final int ANY_REQUIRED = 4;
   /** Constant meaning that a track is to be discarded */
   protected static final int DISCARD = 0;
   /** Constant meaning that a track is to be saved */
   protected static final int SAVE = 1;

   // *** Request Codes (alphabetical)

   /** Request code for directions list activity */
   private static final int DIRECTION_ACTIVITY_REQUEST_CODE = 0;
   /** Request code for login activity */
   private static final int LOGIN_ACTIVITY_REQUEST_CODE = 1;
   /** Location Settings Activity Request Code for enabling GPS while 
    * recording */
   private static final int LOCATION_SETTINGS_REQUEST_CODE_MID = 2;
   /** Location Settings Activity Request Code for enabling GPS before 
    * recording */
   private static final int LOCATION_SETTINGS_REQUEST_CODE_START = 3;
   /** Track Editor Activity Request Code */
   private static final int TRACK_EDITOR_REQUEST_CODE = 4;
   /** Track Manager Activity Request Code */
   private static final int TRACK_MANAGER_REQUEST_CODE = 5;
   /** Request code for forcing the user to agree to terms of service */
   public static final int USER_MUST_AGREE = 6;
   /** Request code for viewing landmarks trial information */
   public static final int TRIAL_VIEW_CODE = 7;

   /** whether we are currently adding a landmark in the "now" condition */
   public static boolean landmark_editing_mode_now = false;
   /** whether we are currently adding landmarks in the "later" condition */
   public static boolean landmark_editing_mode_later = false;
   /** landmark trial to be retrieved */
   public static int trial_num_toget = -1;
   /** Landmark prompts currently being displayed */
   public static ArrayList<LandmarkNeed> current_prompts;
   /** Landmark prompt currently selected */
   public static int selected_prompt = -1;
   /** Landmark trial mode - before agreeing to the experiment */
   public static final int LANDMARK_MODE_INTRO = 0;
   /** Landmark trial mode - while recording a track */
   public static final int LANDMARK_MODE_RECORD = 1;
   /** Landmark trial mode - while viewing a prompt (now condition) */
   public static final int LANDMARK_MODE_ADD_NOW = 2;
   /** Landmark trial mode - while viewing prompts (later condition) */
   public static final int LANDMARK_MODE_ADD_LATER = 3;

   public final static int LOCATION_PERMISSION_REQUEST_CODE = 1;

   public MapView mapView = null;
   
   /**
    * As soon as the class is loaded, populate the fields used for
    * compatibility with the appropriate values.
    */
   static {
      initCompatibility();
   };
   
   /**
    * Populates the fields used for compatibility with the appropriate values
    * using reflection.
    */
   private static void initCompatibility() {
      System.out.println("AAAAAAINITTT");
     try {
        nullifyOptionsMenu = 
               Activity.class.getMethod("invalidateOptionsMenu");
     } catch (SecurityException e) {
         e.printStackTrace();
     } catch (NoSuchMethodException e) {
         e.printStackTrace();
     }
   }//initCompatibility
   
   /**
    * Causes the activity to call onPrepareOptionsMenu() the next time the menu
    * is opened.
    */
   public void invalidateOptionsMenu() {
      System.out.println("AAAAAAINVALIDATEEEE");
      if(nullifyOptionsMenu == null) {
         return; 
      }
      try {
         if (Constants.DEBUG) {
            Log.i("cyclopath", "handling reflection of invalidateOptionsMenu");
         }
         // TO-DO
         nullifyOptionsMenu.invoke(this);
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      } 
   }//invalidateOptionsMenu
   
   // *** Listeners
    
   /**
    * This method is called when this activity returns to the surface.
    * @param requestCode Integer provided when calling the activity 
    * from this class
    * @param resultCode Integer value set by the called activity that 
    * is used to determine the state of the finished activity
    * @param data The intent returned by the popped activity
    */
   @Override
   public void onActivityResult(int requestCode,
                                int resultCode,
                                Intent data) {

   }//onActivityResult

   /**
    * Set the title and inflate the context menu
    */
   @Override
   public void onCreateContextMenu(ContextMenu menu, View v,
                                   ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      System.out.println("AAAAAAONCREATEEEE");
      menu.setHeaderTitle(getString(R.string.menu));
      MenuInflater inflater = getMenuInflater();
      if (v == G.map) {
         inflater.inflate(R.menu.map_context_menu, menu);
         if (G.LANDMARKS_EXP_ON
             && (landmark_editing_mode_now
                 || landmark_editing_mode_later)) {
            menu.removeItem(R.id.route_to_here);
            menu.removeItem(R.id.route_from_here);
         } else {
            menu.removeItem(R.id.submit_point);
         }
      } else if (G.active_route == null) {
         inflater.inflate(R.menu.track_menu, menu);
         if ((G.user.isLoggedIn() && G.selected_track.hasOwner()) 
             || !G.user.isLoggedIn()) {
            menu.removeItem(R.id.add_track_owner);
         }
         if (!Constants.DEBUG) {
            menu.removeItem(R.id.conflate_track);
         }
      } else if (G.selected_track == null) {
         inflater.inflate(R.menu.route_menu, menu);
      } else {
         menu.setHeaderTitle(getString(R.string.context_menu_title));
         inflater.inflate(R.menu.context_menu, menu);
         if ((G.user.isLoggedIn() && G.selected_track.hasOwner()) 
              || !G.user.isLoggedIn()) {
            menu.findItem(R.id.tracks)
                .getSubMenu()
                .removeItem(R.id.add_track_owner);
         }
         if (!Constants.DEBUG) {
            menu.removeItem(R.id.conflate_track);
         }
      }
   }//onCreateContextMenu
   
   /**
    * Perform actions based on which context menu item is selected.
    */
   @Override
   public boolean onContextItemSelected(MenuItem item) {
      Intent intent;
      System.out.println("AAAAAAONCONTEXTTTT");
      switch (item.getItemId()) {
      case R.id.view_track_details:
         intent = new Intent(this, TrackDetailsActivity.class);
         intent.putExtra(Constants.STACK_ID_STR, G.selected_track.stack_id);
         startActivity(intent);
         return true;
      case R.id.delete_track:
         //G.trackDeletionHandle(G.selected_track, this, this);
         return true;
      case R.id.add_track_owner:
         G.selected_track.owner = G.user.getName();
         ArrayList<ItemUserAccess> items = new ArrayList<ItemUserAccess>();
         items.add(G.selected_track);
         new GWIS_Commit(items,
                         getResources().getString(
                               R.string.track_put_progress_dialog_title),
                         getResources().getString(
                               R.string.track_put_progress_dialog_content),
                         null).fetch();
         return true;
      case R.id.show_track:
         G.map.lookAt(G.selected_track, 0);
         return true;
      case R.id.save_track_gpx:
         new TrackExporter(G.selected_track, this).exportGPX();
         return true;
      case R.id.conflate_track:
         new ConflationJob(G.selected_track.stack_id).runJob();
         return true;
      case R.id.show_route:
         G.map.lookAt(G.active_route, 0);
         return true;
      case R.id.show_directions:
         intent = new Intent(this, DirectionsActivity.class);
         startActivityForResult(intent, DIRECTION_ACTIVITY_REQUEST_CODE);
         return true;
      case R.id.share_route:
         G.active_route.shareRoute();
         return true;
      case R.id.route_to_here:
         intent = new Intent(this, FindRouteActivity.class);
         intent.putExtra(Constants.FINDROUTE_ACTION,
                            Constants.FINDROUTE_ROUTE_TO_ACTION);
         intent.putExtra("x", G.map.long_press_map_x);
         intent.putExtra("y", G.map.long_press_map_y);
         startActivity(intent);
         return true;
      case R.id.route_from_here:
         intent = new Intent(this, FindRouteActivity.class);
         intent.putExtra(Constants.FINDROUTE_ACTION,
                              Constants.FINDROUTE_ROUTE_FROM_ACTION);
         intent.putExtra("x", G.map.long_press_map_x);
         intent.putExtra("y", G.map.long_press_map_y);
         startActivity(intent);
         return true;
      case R.id.submit_point:
         intent = new Intent(this, ItemDetailsActivity.class);
         intent.putExtra(Constants.EDIT_MODE, true);
         Geopoint g = new Geopoint(G.map.long_press_map_x,
                                   G.map.long_press_map_y);
         g.init();
         intent.putExtra(Constants.STACK_ID_STR, g.stack_id);
         startActivity(intent);
      default:
         return super.onContextItemSelected(item);
      }
   }//onContextItemSelected

   /**
    * Called when the activity is first created, and does the following:<br>
    * - Makes available the values stored in res/values<br>
    * - Sets the location provider as GPS<br>
    * - Sets the content view to the custom Cyclopath map.<br>
    * - Generates a log file for writing GPS points.<br>
    * - Gets GPS points saved from previous session.<br>
    * @see com.example.cyclopath.MapSurface
    * @param saveInstanceState saved information from the last time this
    * activity was run.
    */
   @Override
   public void onCreate(Bundle in_state) {
      super.onCreate(in_state);

      if (ContextCompat.checkSelfPermission(Cyclopath.this,
              Manifest.permission.ACCESS_FINE_LOCATION)
              != PackageManager.PERMISSION_GRANTED
              &&
              ContextCompat.checkSelfPermission(Cyclopath.this,
                      Manifest.permission.ACCESS_COARSE_LOCATION)
                      != PackageManager.PERMISSION_GRANTED) {
         System.out.println("HEREEE2");
         askForLocationPermissions();
      } else {

         System.out.println("HEREEEEEEEE");
         // If an update is required, be sure the user has updated.
         G.checkForMandatoryUpdate();

         Boolean has_agreed =
                 G.cookie_anon.getBoolean(Constants.COOKIE_HAS_AGREED_TO_TERMS, false);

         if (!has_agreed) {
            Intent intent = new Intent(this, UserAgreement.class);
            startActivityForResult(intent, USER_MUST_AGREE);
         }

         // show map
         setContentView(R.layout.main);

         mapView = findViewById(R.id.mapView);
         mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);
         System.out.println("Mapview" + mapView);

         // make the map canvas more easily accessible
         G.map = (MapSurface) findViewById(R.id.map_canvas);
         // retrieve bundle information
         if (in_state != null) {
            G.map.new_map_x = in_state.getDouble("map_center_x");
            G.map.new_map_y = in_state.getDouble("map_center_y");
         }

//         this.setLoggedInTitle(false);
//
//         this.user_settings_editor = G.cookie_anon.edit();
//
//         this.is_recording =
//                 G.cookie_anon.getBoolean(Constants.COOKIE_IS_RECORDING, false);
//
//         // Landmarks Experiment
//         findViewById(R.id.experiment_help_btn).setOnClickListener(this);
//         findViewById(R.id.experiment_no_btn).setOnClickListener(this);
//         findViewById(R.id.experiment_stop_btn).setOnClickListener(this);
//         findViewById(R.id.experiment_landmark_done_btn)
//                 .setOnClickListener(this);
//         findViewById(R.id.experiment_landmark_done_later_btn)
//                 .setOnClickListener(this);
//         findViewById(R.id.experiment_prompt_text).setOnClickListener(this);
//         findViewById(R.id.prev_direction_landmark).setOnClickListener(this);
//         findViewById(R.id.next_direction_landmark).setOnClickListener(this);
//
//         ZoomControls zoom = (ZoomControls) findViewById(R.id.zoomcontrols);
//         zoom.setOnZoomInClickListener(this.zoomInListener);
//         zoom.setOnZoomOutClickListener(this.zoomOutListener);
//
//         // Set up throbber.
//         ImageView throbberImage = (ImageView) findViewById(R.id.throbber_view);
//         throbberImage.setBackgroundResource(R.anim.throbber_anim);
//         throbber_anim = (AnimationDrawable) throbberImage.getBackground();
//         G.cyclopath_handler = new CyclopathHandler(this);
//         this.updateThrobber();
//
//         // Set up direction buttons
//         findViewById(R.id.directions_btn).setOnClickListener(this);
//         findViewById(R.id.direction_text).setOnClickListener(this);
//         findViewById(R.id.prev_direction).setOnClickListener(this);
//         findViewById(R.id.next_direction).setOnClickListener(this);
//
//         this.directionsView = findViewById(R.id.directions_panel);
//
//         // Set up back_to_map button
//         G.map.back_to_map = (Button) findViewById(R.id.back_to_map_btn);
//         G.map.back_to_map.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//               G.map.panto(G.map.xform_x_map2cv(Constants.MAP_CENTER_X),
//                       G.map.xform_y_map2cv(Constants.MAP_CENTER_Y));
//               G.map.update();
//               G.map.back_to_map.setVisibility(View.GONE);
//               G.map.back_to_map.setEnabled(false);
//            }
//         });
//
//         // Set up context button
//         contextButton = (ImageButton) findViewById(R.id.context_button);
//         registerForContextMenu(contextButton);
//         registerForContextMenu(G.map);
//         contextButton.setOnClickListener(this);
//
//         // If we came from the tracking notification, show the track
//         if (getIntent().getAction().equals(Constants.ACTION_SHOW_TRACK_END)) {
//            if (G.current_track == null) {
//               // This happens when the main activity was force closed, but the
//               // service notification was never removed.
//               NotificationManager notification_manager = (NotificationManager)
//                       getSystemService(Context.NOTIFICATION_SERVICE);
//               notification_manager.cancel(
//                       TrackingService.TRACKING_NOTIFICATION_ID);
//            } else {
//               this.showTrackEnd();
//            }
//         } else if (getIntent().getAction().equals(
//                 Constants.ACTION_SHOW_LANDMARK_NEED)
//                 && G.LANDMARKS_EXP_ON
//                 && G.isLandmarkConditionNow()) {
//            this.showLandmarkPrompt();
//         } else if (getIntent().getAction().equals(
//                 Constants.ACTION_VIEW)) {
//            this.viewHandle();
//         }
//
//         this.accelerationValues = new float[3];
//         this.magneticFieldValues = new float[3];
//         this.mSensorManager =
//                 (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
//         this.accelerometerSensor =
//                 this.mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//         this.magneticFieldSensor =
//                 this.mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//
//         if (!G.track_save_attempted) {
//            G.saveUnsavedTracks(this);
//         }
//
//         // If there is an unsaved track, ask the user if they want to save it.
//         Track track = G.db.getForceClosedTrack();
//         if (G.current_track == null
//                 && track != null) {
//            this.is_recording = false;
//            if (track.points.isEmpty()) {
//               G.db.deleteTrack(track);
//            } else {
//               this.showSaveForceClosedTrackDialog();
//            }
//         }
//
//         // Display "What's new" dialog if needed.
//         ChangeLog cl = new ChangeLog(this);
//         if (cl.firstRun() && has_agreed) {
//            AlertDialog log_dialog = cl.getLogDialog();
//            if (log_dialog != null) {
//               log_dialog.show();
//            }
//         }
//
//         this.stop_recording_msg_sent = false;
      }

   }//onCreate

   @Override
   public void onStart() {
      super.onStart();
      mapView.onStart();
   }

   @Override
   public void onStop() {
      super.onStop();
      mapView.onStop();
   }

   @Override
   public void onLowMemory() {
      super.onLowMemory();
      mapView.onLowMemory();
   }

   /**
    * Called when activity is destroyed (not only when the activity is no
    * longer in the activity stack, but also when rotating the device)
    */
   @Override
   public void onDestroy(){
      System.out.println("AAAAAADESTORY");
      super.onDestroy();
      G.cyclopath_handler = null;
      mapView.onDestroy();
   }

   /**
    * Creates Main Menu
    * @param menu from res/menu/main.xml
    * @return true so the menu will still work
    */
   @Override
   //Menu Events
   public boolean onCreateOptionsMenu(Menu menu) {
      System.out.println("AAAAAAONCREATE3");
      super.onCreateOptionsMenu(menu);
      if (!G.LANDMARKS_EXP_ON) {
         menu.removeItem(R.id.help);
      }
      this.getMenuInflater().inflate(R.menu.main_menu, menu);
      return true;
   }//onCreateOptionsMenu

   /**
    //     * Called when a menu button is pressed. Based on which button is pressed,
    //     * either a method will be called or some commands will be run. For instance,
    //     * 'My Location' pans the map to the current location. 'start_record' begins
    //     * recording GPS locations, and 'stop_record' stops the recording.
    //     * @param item The button item selected
    //     * @return true so the menu will still work
    //     * @see Settings
    //     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("AAAAAAAAONOPTIONSSSS");
        if (item.getGroupId() == R.id.tile_options) {
            // aerial tiles
            if (item.getItemId() == G.aerial_state) {
                return true;
            }
            G.aerial_state = item.getItemId();
            item.setChecked(true);
            G.map.tiles_refetch_aerial();
            return true;
        }
        switch (item.getItemId()) {
            case R.id.login: {
                if (G.user.isLoggedIn()) {
                    // TODO: In the future, once users can save stuff, make sure
                    // to warn users if they have unsaved changes before they log
                    // out.
                    G.user.logout();
//                    this.setLoggedInTitle(true);
//                    this.clearMap();
                } else {
                    Intent intent = new Intent(this, LoginActivity2.class);
                    startActivityForResult(intent, LOGIN_ACTIVITY_REQUEST_CODE);
                }
                invalidateOptionsMenu();
                return true;
            }
            case R.id.manage_tracks: {
                //start Track Manager activity
                startActivityForResult(new Intent(this, TrackManager.class),
                        TRACK_MANAGER_REQUEST_CODE);
                return true;
            }
            case R.id.route_library: {
                //start Route Library activity
                startActivity(new Intent(this, RouteLibrary.class));
                return true;
            }
            case R.id.my_location: {
                //Pan map canvas to location
                Location loc = G.currentLocation();
                if (loc == null) {
                    return false;
                }
                MapSurface.has_panned = false;
                G.map.goToLocation(loc);
                G.map.pointer.setPosition(loc);
                G.map.redraw();
                return true;
            }
            case R.id.start_record: {
                if (G.LANDMARKS_EXP_ON) {
                    G.landmark_condition = Constants.LANDMARK_CONDITION_NONE;
                }
//                startRecording(-1);
                return true;
            }
            case R.id.stop_record: {
//                stopRecording(true);
                return true;
            }
            case R.id.find_route: {
//                Intent intent = new Intent(this, FindRouteActivity.class);
               Intent intent = new Intent(this, NavigationActivity.class);
               startActivity(intent);
                return true;
            }
            case R.id.clear_map: {
                // clear routes and tracks
//                this.clearMap();
                invalidateOptionsMenu();
                return true;
            }
            case R.id.about: {
//                this.handleAbout();
                return true;
            }
            case R.id.help: {
                startActivity(new Intent(this, ExperimentAgreement.class));
                return true;
            }
        }
        return false;
    }//onOptionsItemSelected

   private void askForLocationPermissions() {
        System.out.println("aaaaaskfor");
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Location permessions needed")
                    .setMessage("you need to allow this permission!")
                    .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Cyclopath.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATION_PERMISSION_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
//                                        //Do nothing
                        }
                    })
                    .show();

            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

        } else {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);

            // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }
}//Cyclopath
