package com.example.cyclopath

import android.Manifest
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.cyclopath.TestingActivity.Companion.isPermissionGranted
import com.example.cyclopath.TestingActivity.Companion.lastKnownLocationOrNull
import com.example.cyclopath.databinding.ActivityTestingBinding
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonParser.parseString
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.*
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants.TOP_LEVEL_ROUTE_LINE_LAYER_ID
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources
import com.mapbox.search.*
import com.mapbox.search.autofill.*
import java.util.*
import com.mapbox.search.result.SearchSuggestion

import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.result.SearchResult
import com.mapbox.search.ui.adapter.autofill.AddressAutofillUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView

import java.lang.Exception


class TestingActivity : AppCompatActivity() {
    private lateinit var locationComponent: LocationComponentPlugin

    /**
     * Debug tool used to play, pause and seek route progress events that can be used to produce mocked location updates along the route.
     */
    private val mapboxReplayer = MapboxReplayer()

    /**
     * Debug observer that makes sure the replayer has always an up-to-date information to generate mock updates.
     */
    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)

    /**
     * Debug tool that mocks location updates with an input from the [mapboxReplayer].
     */
    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)

    private val hardCodedRoute by lazy {
        DirectionsRoute.fromJson(
                """{"routeIndex":"0","distance":1302.142,"duration":192.94,"duration_typical":192.94,"geometry":"wfz_gAjo{nhFw@DcT~@oI^yGV_DJ}CNwQx@wc@rB}Or@yDPyKf@yCNMwEKaEQoGImDu@{ZUkHWgHaA{`@m@kTOaGMeF[iMc@kROoFQeIGeCKqDGwCYeLo@mVCmA_@oOOuFOoFGcCoEaoBGcCG_DGuBMsFCsA}@ca@wByx@YwQsAmk@al@vCya@rB]B{Qv@kWhA","weight":279.607,"weight_name":"auto","legs":[{"distance":1302.142,"duration":192.94,"duration_typical":192.94,"summary":"Mission Street, 16th Street","admins":[{"iso_3166_1":"US","iso_3166_1_alpha3":"USA"}],"steps":[{"distance":265.216,"duration":45.219,"duration_typical":45.219,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"wfz_gAjo{nhFw@DcT~@oI^yGV_DJ}CNwQx@wc@rB}Or@yDPyKf@yCN","name":"Mission Street","mode":"driving","maneuver":{"location":[-122.419462,37.762684],"bearing_before":0.0,"bearing_after":356.0,"instruction":"Drive north on Mission Street.","type":"depart"},"voiceInstructions":[{"distanceAlongGeometry":265.216,"announcement":"Drive north on Mission Street. Then, in 900 feet, Turn right onto 16th Street.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eDrive north on Mission Street. Then, in 900 feet, Turn right onto 16th Street.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":81.667,"announcement":"Turn right onto 16th Street.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eTurn right onto 16th Street.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":265.216,"primary":{"text":"16th Street","components":[{"text":"16th Street","type":"text"}],"type":"turn","modifier":"right"}}],"driving_side":"right","weight":61.369,"intersections":[{"location":[-122.419462,37.762684],"bearings":[356],"entry":[true],"out":0,"geometry_index":0,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419465,37.762712],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":1,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419497,37.76305],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":2,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419513,37.763218],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":3,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419525,37.763359],"bearings":[176,357],"entry":[false,true],"in":0,"out":1,"geometry_index":4,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419531,37.763439],"bearings":[177,355],"entry":[false,true],"in":0,"out":1,"geometry_index":5,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419539,37.763518],"bearings":[175,356],"entry":[false,true],"in":0,"out":1,"geometry_index":6,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419568,37.763818],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":7,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419626,37.764406],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":8,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419652,37.764677],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":9,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419661,37.76477],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":10,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419681,37.764975],"bearings":[176,355],"entry":[false,true],"in":0,"out":1,"geometry_index":11,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}}]},{"distance":814.0,"duration":119.249,"duration_typical":119.249,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"wz~_gAp}{nhFMwEKaEQoGImDu@{ZUkHWgHaA{`@m@kTOaGMeF[iMc@kROoFQeIGeCKqDGwCYeLo@mVCmA_@oOOuFOoFGcCoEaoBGcCG_DGuBMsFCsA}@ca@wByx@YwQsAmk@","name":"16th Street","mode":"driving","maneuver":{"location":[-122.419689,37.765052],"bearing_before":355.0,"bearing_after":85.0,"instruction":"Turn right onto 16th Street.","type":"turn","modifier":"right"},"voiceInstructions":[{"distanceAlongGeometry":800.666,"announcement":"Continue for a half mile.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eContinue for a half mile.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":402.336,"announcement":"In a quarter mile, Turn left onto Bryant Street.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eIn a quarter mile, Turn left onto Bryant Street.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":66.667,"announcement":"Turn left onto Bryant Street.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eTurn left onto Bryant Street.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":814.0,"primary":{"text":"Bryant Street","components":[{"text":"Bryant Street","type":"text"}],"type":"turn","modifier":"left"}}],"driving_side":"right","weight":173.587,"intersections":[{"location":[-122.419689,37.765052],"bearings":[85,175],"entry":[true,false],"in":1,"out":0,"geometry_index":12,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419581,37.765059],"bearings":[86,265],"entry":[true,false],"in":1,"out":0,"geometry_index":13,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419484,37.765065],"bearings":[85,266],"entry":[true,false],"in":1,"out":0,"geometry_index":14,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419261,37.765079],"bearings":[86,265],"entry":[true,false],"in":1,"out":0,"geometry_index":16,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.418815,37.765106],"bearings":[85,266],"entry":[true,false],"in":1,"out":0,"geometry_index":17,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.418665,37.765117],"bearings":[84,265],"entry":[true,false],"in":1,"out":0,"geometry_index":18,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.418517,37.765129],"bearings":[86,264],"entry":[true,false],"in":1,"out":0,"geometry_index":19,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.417504,37.765193],"bearings":[86,265],"entry":[true,false],"in":1,"out":0,"geometry_index":22,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.41685,37.765232],"bearings":[86,266],"entry":[true,false],"in":1,"out":0,"geometry_index":25,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.4165,37.765253],"bearings":[85,266],"entry":[true,false],"in":1,"out":0,"geometry_index":28,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.416411,37.765259],"bearings":[86,265],"entry":[true,false],"in":1,"out":0,"geometry_index":29,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.416335,37.765263],"bearings":[86,266],"entry":[true,false],"in":1,"out":0,"geometry_index":30,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.415749,37.7653],"bearings":[86,265],"entry":[true,false],"in":1,"out":0,"geometry_index":32,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.415446,37.765318],"bearings":[85,266],"entry":[true,false],"in":1,"out":0,"geometry_index":34,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.415323,37.765326],"bearings":[85,265],"entry":[true,false],"in":1,"out":0,"geometry_index":35,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.415203,37.765334],"bearings":[86,265],"entry":[true,false],"in":1,"out":0,"geometry_index":36,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.413278,37.765446],"bearings":[86,266],"entry":[true,false],"in":1,"out":0,"geometry_index":39,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.413139,37.765454],"bearings":[86,266],"entry":[true,false],"in":1,"out":0,"geometry_index":41,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.413017,37.765461],"bearings":[86,266],"entry":[true,false],"in":1,"out":0,"geometry_index":42,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.412429,37.765494],"bearings":[85,266],"entry":[true,false],"in":1,"out":0,"geometry_index":44,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.411504,37.765554],"bearings":[87,265],"entry":[true,false],"in":1,"out":0,"geometry_index":45,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.411204,37.765567],"bearings":[86,267],"entry":[true,false],"in":1,"out":0,"geometry_index":46,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}}]},{"distance":222.926,"duration":28.472,"duration_typical":28.472,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"q}_`gAx~inhFal@vCya@rB]B{Qv@kWhA","name":"Bryant Street","mode":"driving","maneuver":{"location":[-122.410493,37.765609],"bearing_before":86.0,"bearing_after":355.0,"instruction":"Turn left onto Bryant Street.","type":"turn","modifier":"left"},"voiceInstructions":[{"distanceAlongGeometry":209.593,"announcement":"In 700 feet, Your destination will be on the right.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eIn 700 feet, Your destination will be on the right.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":55.556,"announcement":"Your destination is on the right.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eYour destination is on the right.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":222.926,"primary":{"text":"Your destination will be on the right","components":[{"text":"Your destination will be on the right","type":"text"}],"type":"arrive","modifier":"right"}},{"distanceAlongGeometry":55.556,"primary":{"text":"Your destination is on the right","components":[{"text":"Your destination is on the right","type":"text"}],"type":"arrive","modifier":"right"}}],"driving_side":"right","weight":44.652,"intersections":[{"location":[-122.410493,37.765609],"bearings":[266,355],"entry":[false,true],"in":0,"out":1,"geometry_index":47,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-122.410569,37.76633],"bearings":[175,355],"entry":[false,true],"in":0,"out":1,"geometry_index":48,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-122.410629,37.766902],"bearings":[175,356],"entry":[false,true],"in":0,"out":1,"geometry_index":50,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-122.410657,37.767204],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":51,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}}]},{"distance":0.0,"duration":0.0,"duration_typical":0.0,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"syc`gAjkjnhF??","name":"Bryant Street","mode":"driving","maneuver":{"location":[-122.410694,37.767594],"bearing_before":356.0,"bearing_after":0.0,"instruction":"Your destination is on the right.","type":"arrive","modifier":"right"},"voiceInstructions":[],"bannerInstructions":[],"driving_side":"right","weight":0.0,"intersections":[{"location":[-122.410694,37.767594],"bearings":[176],"entry":[true],"in":0,"geometry_index":52,"admin_index":0}]}],"annotation":{"distance":[3.1,37.7,18.8,15.7,8.9,8.8,33.5,65.7,30.3,10.4,22.9,8.6,9.5,8.6,12.0,7.7,39.4,13.3,13.1,47.8,30.2,11.4,10.2,20.2,27.4,10.6,14.4,5.9,7.9,6.7,18.6,33.1,3.4,23.3,10.9,10.6,5.8,158.2,5.8,7.1,5.2,10.8,3.7,48.2,81.7,26.4,62.7,80.5,62.2,1.7,33.7,43.6],"duration":[0.554,4.684,2.328,1.953,1.108,1.588,6.029,11.818,5.446,1.869,4.12,1.548,1.716,1.541,2.162,1.382,7.085,2.386,1.746,6.378,4.027,1.518,1.107,2.205,2.984,1.156,1.569,0.645,0.858,0.928,2.579,4.584,0.516,3.495,1.629,1.467,0.807,21.906,0.807,0.977,0.722,1.336,0.46,5.98,7.001,2.322,8.688,8.054,6.221,0.168,2.822,3.566],"speed":[5.6,8.1,8.1,8.1,8.1,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,7.5,7.5,7.5,7.5,9.2,9.2,9.2,9.2,9.2,9.2,9.2,7.2,7.2,7.2,6.7,6.7,6.7,7.2,7.2,7.2,7.2,7.2,7.2,8.1,8.1,8.1,11.7,11.4,7.2,10.0,10.0,10.0,11.9,12.2],"maxspeed":[{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true}],"congestion":["low","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","unknown","unknown","unknown","unknown","unknown","unknown","low","low","low","unknown","low"]}}],"routeOptions":{"baseUrl":"https://api.mapbox.com","user":"mapbox","profile":"driving-traffic","coordinates":"-122.4192,37.7627;-122.4106,37.7676","language":"en","continue_straight":true,"roundabout_exits":true,"geometries":"polyline6","overview":"full","steps":true,"annotations":"congestion,maxspeed,speed,duration,distance,closure","voice_instructions":true,"banner_instructions":true,"voice_units":"imperial","uuid":"gBUUlLJctERT8RrvDM7qCrAvnccdmXLCxVQUmAFsjWf3VRGUNK0lVQ\u003d\u003d"},"voiceLocale":"en-US"}"""
        )
    }

    /**
     * Bindings to the example layout.
     */
    private lateinit var viewBinding: ActivityTestingBinding

    /**
     * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
     * to the Maps SDK in order to update the user location indicator on the map.
     */
    private val navigationLocationProvider by lazy {
        NavigationLocationProvider()
    }

    /**
     * RouteLine: Various route line related options can be customized here including applying
     * route line color customizations.
     */
    private val routeLineResources: RouteLineResources by lazy {
        RouteLineResources.Builder()
                /**
                 * Route line related colors can be customized via the [RouteLineColorResources]. If using the
                 * default colors the [RouteLineColorResources] does not need to be set as seen here, the
                 * defaults will be used internally by the builder.
                 */
                .routeLineColorResources(RouteLineColorResources.Builder().build())
                .build()
    }

    /**
     * RouteLine: Additional route line options are available through the MapboxRouteLineOptions.
     * Notice here the withRouteLineBelowLayerId option. The map is made up of layers. In this
     * case the route line will be placed below the "road-label" layer which is a good default
     * for the most common Mapbox navigation related maps. You should consider if this should be
     * changed for your use case especially if you are using a custom map style.
     */
    private val options: MapboxRouteLineOptions by lazy {
        MapboxRouteLineOptions.Builder(this)
                /**
                 * Remove this line and [onPositionChangedListener] if you don't wish to show the
                 * vanishing route line feature
                 */
                .withVanishingRouteLineEnabled(true)
                .withRouteLineResources(routeLineResources)
                .withRouteLineBelowLayerId("road-label-navigation")
                .build()
    }

    /**
     * RouteLine: This class is responsible for rendering route line related mutations generated
     * by the [routeLineApi]
     */
    private val routeLineView by lazy {
        MapboxRouteLineView(options)
    }

    /**
     * RouteLine: This class is responsible for generating route line related data which must be
     * rendered by the [routeLineView] in order to visualize the route line on the map.
     */
    private val routeLineApi: MapboxRouteLineApi by lazy {
        MapboxRouteLineApi(options)
    }

    /**
     * RouteArrow: This class is responsible for generating data related to maneuver arrows. The
     * data generated must be rendered by the [routeArrowView] in order to apply mutations to
     * the map.
     */
    private val routeArrowApi: MapboxRouteArrowApi by lazy {
        MapboxRouteArrowApi()
    }

    /**
     * RouteArrow: Customization of the maneuver arrow(s) can be done using the
     * [RouteArrowOptions]. Here the above layer ID is used to determine where in the map layer
     * stack the arrows appear. Above the layer of the route traffic line is being used here. Your
     * use case may necessitate adjusting this to a different layer position.
     */
    private val routeArrowOptions by lazy {
        RouteArrowOptions.Builder(this)
                .withAboveLayerId(TOP_LEVEL_ROUTE_LINE_LAYER_ID)
                .build()
    }

    /**
     * RouteArrow: This class is responsible for rendering the arrow related mutations generated
     * by the [routeArrowApi]
     */
    private val routeArrowView: MapboxRouteArrowView by lazy {
        MapboxRouteArrowView(routeArrowOptions)
    }

    /**
     * RouteLine: This is one way to keep the route(s) appearing on the map in sync with
     * MapboxNavigation. When this observer is called the route data is used to draw route(s)
     * on the map.
     */
//    private val routesObserver: RoutesObserver = RoutesObserver { routeUpdateResult ->
//        // RouteLine: wrap the NavigationRoute objects and pass them
//        // to the MapboxRouteLineApi to generate the data necessary to draw the route(s)
//        // on the map.
//        routeLineApi.setNavigationRoutes(
//                routeUpdateResult.navigationRoutes
//        ) { value ->
//            // RouteLine: The MapboxRouteLineView expects a non-null reference to the map style.
//            // the data generated by the call to the MapboxRouteLineApi above must be rendered
//            // by the MapboxRouteLineView in order to visualize the changes on the map.
//            viewBinding.mapView.getMapboxMap().getStyle()?.apply {
//                routeLineView.renderRouteDrawData(this, value)
//            }
//        }
//    }

    /**
     * RouteLine: This listener is necessary only when enabling the vanishing route line feature
     * which changes the color of the route line behind the puck during navigation. If this
     * option is set to `false` (the default) in MapboxRouteLineOptions then it is not necessary
     * to use this listener.
     */
//    private val onPositionChangedListener = OnIndicatorPositionChangedListener { point ->
//        val result = routeLineApi.updateTraveledRouteLine(point)
//        viewBinding.mapView.getMapboxMap().getStyle()?.apply {
//            // Render the result to update the map.
//            routeLineView.renderRouteLineUpdate(this, result)
//        }
//    }

//    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
//        // RouteLine: This line is only necessary if the vanishing route line feature
//        // is enabled.
//        routeLineApi.updateWithRouteProgress(routeProgress) { result ->
//            viewBinding.mapView.getMapboxMap().getStyle()?.apply {
//                routeLineView.renderRouteLineUpdate(this, result)
//            }
//        }
//
//        // RouteArrow: The next maneuver arrows are driven by route progress events.
//        // Generate the next maneuver arrow update data and pass it to the view class
//        // to visualize the updates on the map.
//        val arrowUpdate = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
//        viewBinding.mapView.getMapboxMap().getStyle()?.apply {
//            // Render the result to update the map.
//            routeArrowView.renderManeuverUpdate(this, arrowUpdate)
//        }
//    }

    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {}
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                    enhancedLocation,
                    locationMatcherResult.keyPoints,
            )
            updateCamera(
                    Point.fromLngLat(
                            enhancedLocation.longitude, enhancedLocation.latitude
                    ),
                    enhancedLocation.bearing.toDouble()
            )
        }
    }

    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
            onResumedObserver = object : MapboxNavigationObserver {
                @SuppressLint("MissingPermission")
                override fun onAttached(mapboxNavigation: MapboxNavigation) {
                    //mapboxNavigation.registerRoutesObserver(routesObserver)
                    mapboxNavigation.registerLocationObserver(locationObserver)
                    //mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
                    mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
                    mapboxNavigation.startTripSession()
                }

                override fun onDetached(mapboxNavigation: MapboxNavigation) {
                    //mapboxNavigation.unregisterRoutesObserver(routesObserver)
                    mapboxNavigation.unregisterLocationObserver(locationObserver)
                    //mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
                    mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
                }
            },
            onInitialize = this::initNavigation
    )

    private lateinit var origintext : EditText
    private lateinit var destinationtext : EditText

    private lateinit var searchbutton : Button

    private lateinit var searchEngine: SearchEngine
    private lateinit var searchRequestTask: AsyncOperationTask

    private lateinit var addressAutofill: AddressAutofill
    private lateinit var mapPinOrigin: View
    private lateinit var mapPinDestination: View
    private lateinit var searchResultsViewOrigin: SearchResultsView
    private lateinit var searchEngineUiAdapterOrigin: AddressAutofillUiAdapter
    private lateinit var searchResultsViewDestination: SearchResultsView
    private lateinit var searchEngineUiAdapterDestination: AddressAutofillUiAdapter

//    private lateinit var pinCorrectionNote: TextView

    private var ignoreNextMapIdleEvent: Boolean = false
    private var ignoreNextQueryTextUpdate: Boolean = false

    private lateinit var origin: Point
    private lateinit var destination: Point
    private var isOrigin : Boolean = true

//    private val searchCallback = object : SearchSelectionCallback {
//
//        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
//            if (suggestions.isEmpty()) {
//                Log.i("SearchApiExample", "No suggestions found")
//            } else {
//                Log.i("SearchApiExample", "Search suggestions: $suggestions.\nSelecting first suggestion...")
//                searchRequestTask = searchEngine.select(suggestions.first(), this)
//            }
//        }
//
//        override fun onResult(
//                suggestion: SearchSuggestion,
//                result: SearchResult,
//                responseInfo: ResponseInfo
//        ) {
//            if (isOrigin) {
//                origin = result.coordinate
//                isOrigin = false
//            } else {
//                destination = result.coordinate
//                isOrigin = true
//                fetchARoute(origin, destination)
//            }
//            Log.i("SearchApiExample", "Search result: $result")
//        }
//
//        override fun onCategoryResult(
//                suggestion: SearchSuggestion,
//                results: List<SearchResult>,
//                responseInfo: ResponseInfo
//        ) {
//            println("333")
//            Log.i("SearchApiExample", "Category search results: $results")
//        }
//
//        override fun onError(e: Exception) {
//            println("444")
//            Log.i("SearchApiExample", "Search error", e)
//        }
//    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityTestingBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (ContextCompat.checkSelfPermission(this@TestingActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(this@TestingActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            askForLocationPermissions()
        }

        addressAutofill = AddressAutofill.create(getString(R.string.matoken))

        mapPinOrigin = findViewById(R.id.map_pin_origin)
        mapPinDestination = findViewById(R.id.map_pin_destination)
//        pinCorrectionNote = findViewById(R.id.pin_correction_note)

        viewBinding.mapView.getMapboxMap().addOnMapIdleListener {
            if (ignoreNextMapIdleEvent) {
                ignoreNextMapIdleEvent = false
                return@addOnMapIdleListener
            }
            val mapCenter = viewBinding.mapView.getMapboxMap().cameraState.center
            findAddress(mapCenter)
        }

        searchResultsViewOrigin = findViewById(R.id.search_results_view_origin)

        searchResultsViewOrigin.initialize(
                SearchResultsView.Configuration(
                        commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL)
                )
        )

        searchEngineUiAdapterOrigin = AddressAutofillUiAdapter(
                view = searchResultsViewOrigin,
                addressAutofill = addressAutofill
        )

        searchResultsViewDestination = findViewById(R.id.search_results_view_destination)

        searchResultsViewDestination.initialize(
                SearchResultsView.Configuration(
                        commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL)
                )
        )

        searchEngineUiAdapterDestination = AddressAutofillUiAdapter(
                view = searchResultsViewDestination,
                addressAutofill = addressAutofill
        )

        searchEngineUiAdapterOrigin.addSearchListener(object : AddressAutofillUiAdapter.SearchListener {

            override fun onSuggestionSelected(suggestion: AddressAutofillSuggestion) {
                showAddressAutofillSuggestion(
                        suggestion,
                        fromReverseGeocoding = false,
                        true
                )
            }

            override fun onSuggestionsShown(suggestions: List<AddressAutofillSuggestion>) {
                // Nothing to do
            }

            override fun onError(e: Exception) {
                // Nothing to do
            }
        })

        searchEngineUiAdapterDestination.addSearchListener(object : AddressAutofillUiAdapter.SearchListener {

            override fun onSuggestionSelected(suggestion: AddressAutofillSuggestion) {

                showAddressAutofillSuggestion(
                        suggestion,
                        fromReverseGeocoding = false,
                        false
                )
            }

            override fun onSuggestionsShown(suggestions: List<AddressAutofillSuggestion>) {
                // Nothing to do
            }

            override fun onError(e: Exception) {
                // Nothing to do
            }
        })

        origintext = viewBinding.origin
        destinationtext = viewBinding.destination
        searchbutton = viewBinding.search

//        viewBinding.mapView.getMapboxMap().loadStyleUri(NavigationStyles.NAVIGATION_DAY_STYLE) {
//            viewBinding.startNavigation.visibility = View.VISIBLE
//            viewBinding.startNavigation.text = "Start Navigation"
//            viewBinding.startNavigation.setOnClickListener {
//                mapboxNavigation.setNavigationRoutes(
//                        listOf(hardCodedRoute).toNavigationRoutes(RouterOrigin.Offboard)
//                )
//                viewBinding.startNavigation.visibility = View.INVISIBLE
//                // RouteLine: Hiding the alternative routes when navigation starts.
//                viewBinding.mapView.getMapboxMap().getStyle()?.apply {
//                    routeLineView.hideAlternativeRoutes(this)
//                }
//            }
//        }

        searchbutton.setOnClickListener {
            if (origin != null && destination != null) {
                fetchARoute(origin, destination)
            }
//                searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
//                        SearchEngineSettings(getString(R.string.matoken))
//                )
//
//                searchRequestTask = searchEngine.search(
//                        origintext.toString(),
//                        SearchOptions(limit = 5),
//                        searchCallback
//                )
//
//                searchRequestTask = searchEngine.search(
//                        destinationtext.toString(),
//                        SearchOptions(limit = 5),
//                        searchCallback
//                )
        }

        origintext.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                isOrigin = true
                if (ignoreNextQueryTextUpdate) {
                    ignoreNextQueryTextUpdate = false
                    return
                }

                val query = Query.create(text.toString())
                if (query != null) {
                    lifecycleScope.launchWhenStarted {
                        val result = searchEngineUiAdapterOrigin.search(query)
                    }
                }
                searchResultsViewOrigin.isVisible = query != null
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Nothing to do
            }

            override fun afterTextChanged(s: Editable) {
                // Nothing to do
            }
        })

        destinationtext.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                isOrigin = false
                if (ignoreNextQueryTextUpdate) {
                    ignoreNextQueryTextUpdate = false
                    return
                }

                val query = Query.create(text.toString())
                if (query != null) {
                    lifecycleScope.launchWhenStarted {
                        searchEngineUiAdapterDestination.search(query)
                    }
                }
                searchResultsViewDestination.isVisible = query != null
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Nothing to do
            }

            override fun afterTextChanged(s: Editable) {
                // Nothing to do
            }
        })

        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(
                    this@TestingActivity,
                    arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    TestingActivity.PERMISSIONS_REQUEST_LOCATION
            )
            LocationEngineProvider.getBestLocationEngine(applicationContext).lastKnownLocationOrNull(this) { point ->
                point?.let {
                    viewBinding.mapView.getMapboxMap().setCamera(
                            CameraOptions.Builder()
                                    .center(point)
                                    .zoom(9.0)
                                    .build()
                    )
                    ignoreNextMapIdleEvent = true
                }
            }
        } else {
            LocationEngineProvider.getBestLocationEngine(applicationContext).lastKnownLocationOrNull(this) { point ->
                point?.let {
                    viewBinding.mapView.getMapboxMap().setCamera(
                            CameraOptions.Builder()
                                    .center(point)
                                    .zoom(9.0)
                                    .build()
                    )
                    ignoreNextMapIdleEvent = true
                }
            }
        }

    }

    private fun initNavigation() {
        MapboxNavigationApp.setup(
                NavigationOptions.Builder(this)
                        .accessToken(getString(R.string.matoken))
                        // comment out the location engine setting block to disable simulation
                        .locationEngine(replayLocationEngine)
                        .build()
        )

//        locationComponent = viewBinding.mapView.location.apply {
//            setLocationProvider(navigationLocationProvider)
//            addOnIndicatorPositionChangedListener(onPositionChangedListener)
//            enabled = true
//        }

        replayOriginLocation()
    }

    private fun replayOriginLocation() {
        mapboxReplayer.pushEvents(
                listOf(
                        ReplayRouteMapper.mapToUpdateLocation(
                                Date().time.toDouble(),
                                Point.fromLngLat(-122.4192, 37.7627)
                        )
                )
        )
        mapboxReplayer.playFirstLocation()
        mapboxReplayer.playbackSpeed(3.0)
    }

    private fun updateCamera(point: Point, bearing: Double?) {
        val mapAnimationOptionsBuilder = MapAnimationOptions.Builder()
        viewBinding.mapView.camera.easeTo(
                CameraOptions.Builder()
                        .center(point)
                        .bearing(bearing)
                        .pitch(45.0)
                        .zoom(17.0)
                        .padding(EdgeInsets(1000.0, 0.0, 0.0, 0.0))
                        .build(),
                mapAnimationOptionsBuilder.build()
        )
    }

    /**
     * The method instantiates a [RouteOptions] object and fetches route between the origin and
     * destination pair. There are several [RouteOptions] that you can specify, but this example
     * mentions only what is relevant.
     */
    @SuppressLint("SetTextI18n")
    private fun fetchARoute(origin : Point, destination : Point) {

        val routeOptions = RouteOptions.builder()
                // applies the default parameters to route options
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                // lists the coordinate pair i.e. origin and destination
                // If you want to specify waypoints you can pass list of points instead of null
                .coordinatesList(listOf(origin, destination))
                // set it to true if you want to receive alternate routes to your destination
                .alternatives(false)
//                .profile("cycling")
                // provide the bearing for the origin of the request to ensure
                // that the returned route faces in the direction of the current user movement
                .bearingsList(
                        listOf(
                                Bearing.builder()
                                        .angle(10F.toDouble())
                                        .degrees(45.0)
                                        .build(),
                                null
                        )
                )
                .annotationsList(
                        listOf(
                                DirectionsCriteria.ANNOTATION_CONGESTION,
                                DirectionsCriteria.ANNOTATION_MAXSPEED,
                                DirectionsCriteria.ANNOTATION_SPEED,
                                DirectionsCriteria.ANNOTATION_DURATION,
                                DirectionsCriteria.ANNOTATION_DISTANCE
                        )
                )
                .build()
        mapboxNavigation.requestRoutes(
                routeOptions,
                object : NavigationRouterCallback {
                    override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
//                        // This particular callback is executed if you invoke
//                        // mapboxNavigation.cancelRouteRequest()
//                        binding.responseTextView.text = "route request canceled"
//                        binding.fetchARouteButton.visibility = View.VISIBLE
                    }

                    override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
//                        viewBinding.responseTextView.text =
//                                """
//                            route request failed with:
//                            $reasons
//                        """.trimIndent()
//                        Log.e(TestingActivity.LOG_TAG, "route request failed with $reasons")
//                        binding.fetchARouteButton.visibility = View.VISIBLE
                    }

                    override fun onRoutesReady(
                            routes: List<NavigationRoute>,
                            routerOrigin: RouterOrigin
                    ) {
                        // GSON instance used only to print the response prettily
//                        val gson = GsonBuilder().setPrettyPrinting().create()
//                        val json = routes.map {
//                            gson.toJson(
//                                    JsonParser.parseString(it.directionsRoute.toJson()),JsonObject::class.java
//                            )
//                        }

                        val route = routes[0].directionsRoute

                        mapboxNavigation.setNavigationRoutes(
                                listOf(route).toNavigationRoutes(RouterOrigin.Offboard)
                        )
                        viewBinding.startNavigation.visibility = View.INVISIBLE
//                        viewBinding.mapView.getMapboxMap().getStyle()?.apply {
//                            routeLineView.hideAlternativeRoutes(this)
//                        }

                    }
                }
        )
    }

    ////
    private fun findAddress(point: Point) {
        lifecycleScope.launchWhenStarted {
            when (val response = addressAutofill.suggestions(point, AddressAutofillOptions()))  {
                is AddressAutofillResponse.Suggestions -> {
                    if (response.suggestions.isEmpty()) {
                        showToast(R.string.address_autofill_error_pin_correction)
                    } else {
                        showAddressAutofillSuggestion(
                                response.suggestions.first(),
                                fromReverseGeocoding = true,
                                isOrigin
                        )
                    }
                }
                is AddressAutofillResponse.Error -> {
                    val error = response.error
                    Log.d("Test.", "Test. $error", error)
                    showToast(R.string.address_autofill_error_pin_correction)
                }
            }
        }
    }

    private fun showAddressAutofillSuggestion(suggestion: AddressAutofillSuggestion, fromReverseGeocoding: Boolean, isOrigin: Boolean) {
        val address = suggestion.result().address
        if (isOrigin) {
            origintext.setText(suggestion.formattedAddress)
        } else {
            destinationtext.setText(suggestion.formattedAddress)
        }

        //pinCorrectionNote.isVisible = true

//        if (!fromReverseGeocoding) {
//            ignoreNextMapIdleEvent = true
//            viewBinding.mapView.getMapboxMap().setCamera(
//                    CameraOptions.Builder()
//                            .center(suggestion.coordinate)
//                            .zoom(16.0)
//                            .build()
//            )
//            println("HERE1")
//            println(viewBinding.mapView.getMapboxMap().cameraState.center.longitude())
//            println(viewBinding.mapView.getMapboxMap().cameraState.center.latitude())
//            println(suggestion.coordinate.longitude())
//            println(suggestion.coordinate.latitude())
//            if (isOrigin) {
//                mapPinOrigin.isVisible = true
//                origin = suggestion.coordinate
//            } else {
//                mapPinDestination.isVisible = true
//                destination = suggestion.coordinate
//            }
//            addAnnotationToMap(suggestion.coordinate)
//        }

        ignoreNextMapIdleEvent = true
        addAnnotationToMap(suggestion.coordinate)

        ignoreNextQueryTextUpdate = true
        if (isOrigin) {
            origintext.setText(
                    listOfNotNull(
                            address.houseNumber,
                            address.street
                    ).joinToString()
            )
            origintext.clearFocus()

            searchResultsViewOrigin.isVisible = false
            searchResultsViewOrigin.hideKeyboard()
        } else {
            destinationtext.setText(
                    listOfNotNull(
                            address.houseNumber,
                            address.street
                    ).joinToString()
            )
            destinationtext.clearFocus()

            searchResultsViewDestination.isVisible = false
            searchResultsViewDestination.hideKeyboard()
        }

    }

    private fun showToast(@StringRes resId: Int) {
        Toast.makeText(applicationContext, getString(resId), Toast.LENGTH_SHORT).show()
    }

    private companion object {

        const val PERMISSIONS_REQUEST_LOCATION = 0

        fun Context.isPermissionGranted(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                    this, permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        val Context.inputMethodManager: InputMethodManager
            get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        fun View.hideKeyboard() {
            context.inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
        }

        @SuppressLint("MissingPermission")
        fun LocationEngine.lastKnownLocationOrNull(context: Context, callback: (Point?) -> Unit) {
            if (!PermissionsManager.areLocationPermissionsGranted(context)) {
                callback(null)
            }

            val locationCallback = object : LocationEngineCallback<LocationEngineResult> {
                override fun onSuccess(result: LocationEngineResult?) {
                    val location = (result?.locations?.lastOrNull() ?: result?.lastLocation)?.let { location ->
                        Point.fromLngLat(location.longitude, location.latitude)
                    }
                    callback(location)
                }

                override fun onFailure(exception: Exception) {
                    callback(null)
                }
            }
            getLastLocation(locationCallback)
        }
    }

    open fun askForLocationPermissions() {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this@TestingActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(this@TestingActivity)
                    .setTitle("Location permessions needed")
                    .setMessage("you need to allow this permission!")
                    .setPositiveButton("Sure") { dialog, which ->
                        ActivityCompat.requestPermissions(this@TestingActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                Cyclopath.LOCATION_PERMISSION_REQUEST_CODE)
                    }
                    .setNegativeButton("Not now") { dialog, which ->
                        //                                        //Do nothing
                    }
                    .show()

            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this@TestingActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
            AlertDialog.Builder(this@TestingActivity)
                    .setTitle("Location permessions needed")
                    .setMessage("you need to allow this permission!")
                    .setPositiveButton("Sure") { dialog, which ->
                        ActivityCompat.requestPermissions(this@TestingActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                                Cyclopath.LOCATION_PERMISSION_REQUEST_CODE)
                    }
                    .setNegativeButton("Not now") { dialog, which ->
                        //                                        //Do nothing
                    }
                    .show()

        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Cyclopath.LOCATION_PERMISSION_REQUEST_CODE)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    Cyclopath.LOCATION_PERMISSION_REQUEST_CODE)

            // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    private fun addAnnotationToMap(point : Point) {
        bitmapFromDrawableRes(
                this@TestingActivity,
                R.drawable.red_marker
        )?.let {
            println("DRAW")
            val annotationApi = viewBinding.mapView.annotations
            val pointAnnotationManager = annotationApi?.createPointAnnotationManager(viewBinding.mapView)
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                    .withPoint(point)
                    .withIconImage(it)
            pointAnnotationManager?.create(pointAnnotationOptions)
            val cameraPosition = CameraOptions.Builder()
                    .zoom(14.0)
                    .center(point)
                    .build()
            viewBinding.mapView.getMapboxMap().setCamera(cameraPosition)
            println("REACH")
            println(viewBinding.mapView.getMapboxMap().cameraState.center.longitude())
            println(viewBinding.mapView.getMapboxMap().cameraState.center.latitude())
        }
    }
    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
            convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
// copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth, drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxReplayer.finish()
        routeLineView.cancel()
        routeLineApi.cancel()
        //locationComponent.removeOnIndicatorPositionChangedListener(onPositionChangedListener)
        searchRequestTask.cancel()
    }
}
