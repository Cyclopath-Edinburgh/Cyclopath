package com.example.cyclopath

import android.Manifest
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
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.cyclopath.t2Activity.Companion.hideKeyboard
import com.example.cyclopath.databinding.ActivityT2Binding
import com.example.cyclopath.databinding.ActivityTestingBinding
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
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
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources
import com.mapbox.search.autofill.*
import com.mapbox.search.ui.adapter.autofill.AddressAutofillUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import java.lang.ref.WeakReference
import java.sql.SQLOutput
import java.util.*

class t2Activity : AppCompatActivity() {

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
                .withAboveLayerId(RouteLayerConstants.TOP_LEVEL_ROUTE_LINE_LAYER_ID)
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
    private val routesObserver: RoutesObserver = RoutesObserver { routeUpdateResult ->
        // RouteLine: wrap the NavigationRoute objects and pass them
        // to the MapboxRouteLineApi to generate the data necessary to draw the route(s)
        // on the map.
        routeLineApi.setNavigationRoutes(
                routeUpdateResult.navigationRoutes
        ) { value ->
            // RouteLine: The MapboxRouteLineView expects a non-null reference to the map style.
            // the data generated by the call to the MapboxRouteLineApi above must be rendered
            // by the MapboxRouteLineView in order to visualize the changes on the map.
            mapboxMap.getStyle()?.apply {
                routeLineView.renderRouteDrawData(this, value)
            }
        }
    }

    /**
     * RouteLine: This listener is necessary only when enabling the vanishing route line feature
     * which changes the color of the route line behind the puck during navigation. If this
     * option is set to `false` (the default) in MapboxRouteLineOptions then it is not necessary
     * to use this listener.
     */
    private val onPositionChangedListener = OnIndicatorPositionChangedListener { point ->
        val result = routeLineApi.updateTraveledRouteLine(point)
        mapboxMap.getStyle()?.apply {
            // Render the result to update the map.
            routeLineView.renderRouteLineUpdate(this, result)
        }
    }

    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        // RouteLine: This line is only necessary if the vanishing route line feature
        // is enabled.
        routeLineApi.updateWithRouteProgress(routeProgress) { result ->
            mapboxMap.getStyle()?.apply {
                routeLineView.renderRouteLineUpdate(this, result)
            }
        }

        // RouteArrow: The next maneuver arrows are driven by route progress events.
        // Generate the next maneuver arrow update data and pass it to the view class
        // to visualize the updates on the map.
        val arrowUpdate = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
        mapboxMap.getStyle()?.apply {
            // Render the result to update the map.
            routeArrowView.renderManeuverUpdate(this, arrowUpdate)
        }
    }

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
                    mapboxNavigation.registerRoutesObserver(routesObserver)
                    mapboxNavigation.registerLocationObserver(locationObserver)
                    mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
                    mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
                    mapboxNavigation.startTripSession()
                }

                override fun onDetached(mapboxNavigation: MapboxNavigation) {
                    mapboxNavigation.unregisterRoutesObserver(routesObserver)
                    mapboxNavigation.unregisterLocationObserver(locationObserver)
                    mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
                    mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
                }
            },
            onInitialize = this::initNavigation
    )

//    private lateinit var locationPermissionHelper: LocationPermissionHelper
//
//    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
//        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
//    }
//
//    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
//        mapboxMap.setCamera(CameraOptions.Builder().center(it).build())
//        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
//    }
//
//    private val onMoveListener = object : OnMoveListener {
//        override fun onMoveBegin(detector: MoveGestureDetector) {
//            onCameraTrackingDismissed()
//        }
//
//        override fun onMove(detector: MoveGestureDetector): Boolean {
//            return false
//        }
//
//        override fun onMoveEnd(detector: MoveGestureDetector) {}
//    }

    private lateinit var binding: ActivityT2Binding
    private lateinit var addressAutofill: AddressAutofill

    private lateinit var searchResultsViewOrigin: SearchResultsView
    private lateinit var searchEngineUiAdapterOrigin: AddressAutofillUiAdapter
    private lateinit var searchResultsViewDestination: SearchResultsView
    private lateinit var searchEngineUiAdapterDestination: AddressAutofillUiAdapter

    private lateinit var originText: EditText
    private lateinit var destinationText: EditText

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap

    private var ignoreNextMapIdleEvent: Boolean = false
    private var ignoreNextQueryTextUpdate: Boolean = false

    private lateinit var origin: Point
    private lateinit var destination: Point
    private var isOrigin : Boolean = true

    private lateinit var annotationApi : AnnotationPlugin
    private lateinit var pointAnnotationManager : PointAnnotationManager

    private lateinit var annotationOrigin : PointAnnotation
    private lateinit var annotationDestination : PointAnnotation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityT2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        addressAutofill = AddressAutofill.create(getString(R.string.matoken))

        originText = binding.origin
        destinationText = binding.destination

        mapView = binding.mapView
        mapboxMap = mapView.getMapboxMap()
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS)

//        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
//        locationPermissionHelper.checkPermissions {
//            onMapReady()
//        }
//        mapboxMap.addOnMapIdleListener {
//            if (ignoreNextMapIdleEvent) {
//                ignoreNextMapIdleEvent = false
//                return@addOnMapIdleListener
//            }
////
////            val mapCenter = mapboxMap.cameraState.center
////            findAddress(mapCenter)
//        }

        if (ContextCompat.checkSelfPermission(this@t2Activity,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(this@t2Activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            askForLocationPermissions()
        }

        annotationApi = mapView.annotations
        pointAnnotationManager = annotationApi?.createPointAnnotationManager(mapView)

        searchResultsViewOrigin = binding.searchResultsViewOrigin

        searchResultsViewOrigin.initialize(
                SearchResultsView.Configuration(
                        commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL)
                )
        )

        searchEngineUiAdapterOrigin = AddressAutofillUiAdapter(
                view = searchResultsViewOrigin,
                addressAutofill = addressAutofill
        )

        searchResultsViewDestination = binding.searchResultsViewDestination

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
                )
            }

            override fun onSuggestionsShown(suggestions: List<AddressAutofillSuggestion>) {
                // Nothing to do
            }

            override fun onError(e: Exception) {
                // Nothing to do
            }
        })

        originText.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                isOrigin = true
                if (ignoreNextQueryTextUpdate) {
                    ignoreNextQueryTextUpdate = false
                    return
                }

                val query = Query.create(text.toString())
                if (query != null) {
                    lifecycleScope.launchWhenStarted {
                        searchEngineUiAdapterOrigin.search(query)
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

        destinationText.addTextChangedListener(object : TextWatcher {

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

        LocationEngineProvider.getBestLocationEngine(applicationContext).lastKnownLocationOrNull(this) { point ->
            point?.let {
                println(point)
                mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                                .center(point)
                                .zoom(9.0)
                                .build()
                )
                ignoreNextMapIdleEvent = true
            }
        }

        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) or !isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PERMISSIONS_REQUEST_LOCATION
            )
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
        mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }

//        locationComponent = viewBinding.mapView.location.apply {
//            setLocationProvider(navigationLocationProvider)
//            addOnIndicatorPositionChangedListener(onPositionChangedListener)
//            enabled = true
//        }

//        replayOriginLocation()
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
        mapView.camera.easeTo(
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
                .profile("cycling")
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
                        print("HEREEEEEEEEEEEEEE")
                    }

                    override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    }

                    override fun onRoutesReady(
                            routes: List<NavigationRoute>,
                            routerOrigin: RouterOrigin
                    ) {

                        for (i in routes) {
                            println(i)
                        }

                        val route = routes[0].directionsRoute

                        mapboxNavigation.setNavigationRoutes(
                                listOf(route).toNavigationRoutes(RouterOrigin.Offboard)
                        )

//                        println("123456"+requestID)
//                        println(mapboxNavigation.getNavigationRoutes())
//                        println(mapboxNavigation.cancelRouteRequest())
                    }
                }
        )
    }

    private fun showAddressAutofillSuggestion(suggestion: AddressAutofillSuggestion, fromReverseGeocoding: Boolean) {
        val address = suggestion.result().address

        if (!fromReverseGeocoding) {
            ignoreNextMapIdleEvent = true
            mapboxMap.setCamera(
                    CameraOptions.Builder()
                            .center(suggestion.coordinate)
                            .zoom(16.0)
                            .build()
            )
            if (isOrigin) {
                origin = suggestion.coordinate
            } else {
                destination = suggestion.coordinate
            }
        }

        addAnnotationToMap(suggestion.coordinate)
        ignoreNextQueryTextUpdate = true

        if (isOrigin) {
            originText.setText(
                    listOfNotNull(
                            address.houseNumber,
                            address.street
                    ).joinToString()
            )
            originText.clearFocus()
            searchResultsViewOrigin.isVisible = false
            searchResultsViewOrigin.hideKeyboard()
        } else {
            destinationText.setText(
                    listOfNotNull(
                            address.houseNumber,
                            address.street
                    ).joinToString()
            )
            destinationText.clearFocus()
            searchResultsViewDestination.isVisible = false
            searchResultsViewDestination.hideKeyboard()
        }

        if (this::origin.isInitialized && this::destination.isInitialized) {
            fetchARoute(origin, destination)
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
        println("aaaaaskfor")
        if (ActivityCompat.shouldShowRequestPermissionRationale(this@t2Activity,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(this)
                    .setTitle("Location permessions needed")
                    .setMessage("you need to allow this permission!")
                    .setPositiveButton("Sure") { dialog, which ->
                        ActivityCompat.requestPermissions(this@t2Activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                Cyclopath.LOCATION_PERMISSION_REQUEST_CODE)
                    }
                    .setNegativeButton("Not now") { dialog, which ->
                        //                                        //Do nothing
                    }
                    .show()
        } else {
            ActivityCompat.requestPermissions(this@t2Activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    Cyclopath.LOCATION_PERMISSION_REQUEST_CODE)
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
            AlertDialog.Builder(this)
                    .setTitle("Location permessions needed")
                    .setMessage("you need to allow this permission!")
                    .setPositiveButton("Sure") { dialog, which ->
                        ActivityCompat.requestPermissions(this@t2Activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                                Cyclopath.LOCATION_PERMISSION_REQUEST_CODE)
                    }
                    .setNegativeButton("Not now") { dialog, which ->
                        //                                        //Do nothing
                    }
                    .show()
        } else {
            ActivityCompat.requestPermissions(this@t2Activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    Cyclopath.LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun addAnnotationToMap(point : Point) {

        bitmapFromDrawableRes(
                this@t2Activity,
                R.drawable.red_marker
        )?.let {
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                    .withPoint(point)
                    .withIconImage(it)
            if (isOrigin) {
                if (this::annotationOrigin.isInitialized) {
                    pointAnnotationManager.delete(annotationOrigin)
                }
                annotationOrigin = pointAnnotationManager?.create(pointAnnotationOptions)
            } else {
                if (this::annotationDestination.isInitialized) {
                    pointAnnotationManager.delete(annotationDestination)
                }
                annotationDestination = pointAnnotationManager?.create(pointAnnotationOptions)
            }
//            print("THISSSSSS"+requestID)
//            print(mapboxNavigation.getNavigationRoutes())
//            if (requestID.toString() != "-1") {
//                mapboxNavigation.cancelRouteRequest(requestID)
//                requestID = -1
//            }


//            val cameraPosition = CameraOptions.Builder()
//                    .zoom(14.0)
//                    .center(point)
//                    .build()
//            mapboxMap.setCamera(cameraPosition)
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
}
