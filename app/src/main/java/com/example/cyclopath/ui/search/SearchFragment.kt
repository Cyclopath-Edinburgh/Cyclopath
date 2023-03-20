package com.example.cyclopath.ui.search

import RouteObj
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.cyclopath.*
import com.example.cyclopath.R
import com.example.cyclopath.ui.library.RouteDetailsActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.maps.ElevationApi
import com.google.maps.android.PolyUtil
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
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
import com.mapbox.maps.plugin.locationcomponent.*
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
import okio.ByteString.Companion.encode
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import com.google.maps.GeoApiContext
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.ElevationResult
import com.mapbox.maps.extension.style.expressions.dsl.generated.abs
import com.mapbox.navigation.base.formatter.DistanceFormatter
import com.mapbox.search.*
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.result.SearchResult
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Double.max
import java.lang.Double.min
import kotlin.math.*


class SearchFragment : Fragment() {

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
        MapboxRouteLineOptions.Builder(requireContext())
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
        RouteArrowOptions.Builder(requireContext())
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
            // HERE (3lines)
//            mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(Point.fromLngLat(
//                    enhancedLocation.longitude, enhancedLocation.latitude
//            )).bearing(enhancedLocation.bearing.toDouble()).build())
//            updateCamera(
//                    Point.fromLngLat(
//                            enhancedLocation.longitude, enhancedLocation.latitude
//                    ),
//                    enhancedLocation.bearing.toDouble()
//            )
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

    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        print("HELLO")
//        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
        current = it
        if (isRecord) {
            if (routeCoordinates.size > 0) {
                val startPoint = Location("start")
                startPoint.latitude = routeCoordinates.last().latitude()
                startPoint.longitude = routeCoordinates.last().longitude()
                val endPoint = Location("end")
                endPoint.latitude = it.latitude()
                endPoint.longitude = it.longitude()
                val dist = startPoint.distanceTo(endPoint).toDouble()
                distance += dist
            }
            routeCoordinates.add(it)
        }
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private lateinit var addressAutofill: AddressAutofill

    private lateinit var searchResultsViewOrigin: SearchResultsView
    private lateinit var searchEngineUiAdapterOrigin: AddressAutofillUiAdapter
    private lateinit var searchResultsViewDestination: SearchResultsView
    private lateinit var searchEngineUiAdapterDestination: AddressAutofillUiAdapter

    private lateinit var originText: EditText
    private lateinit var destinationText: EditText
    private lateinit var origin_focus : ImageView
    private lateinit var destination_focus : ImageView
    private lateinit var navigate : Button
    private lateinit var recenter : FloatingActionButton
    private lateinit var swap : ImageView
    private lateinit var record : ImageView
    private lateinit var upload : FloatingActionButton
    private lateinit var elevation : FloatingActionButton
    private lateinit var dropdown : Spinner
    private lateinit var timer : Chronometer
    private lateinit var weatherclick: View

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap

    private var ignoreNextMapIdleEvent: Boolean = false
    private var ignoreNextQueryTextUpdateOrigin: Boolean = false
    private var ignoreNextQueryTextUpdateDestination: Boolean = false

    private lateinit var origin: Point
    private lateinit var destination: Point
    private var isOrigin : Boolean = true


    private lateinit var chart: BarChart
    private var totalUp = 0.0
    private var totalDown = 0.0
    private var barChartFlag = true

    private lateinit var annotationApi : AnnotationPlugin
    private lateinit var pointAnnotationManager : PointAnnotationManager

    private lateinit var annotationOrigin : PointAnnotation
    private lateinit var annotationDestination : PointAnnotation

    private var first : Boolean = true
    private var isRecord : Boolean = false

    private lateinit var route : DirectionsRoute

    private lateinit var current : Point

    private var routeCoordinates = ArrayList<Point>()

    private var storage = Firebase.storage

    private lateinit var start : LocalDateTime
    private lateinit var end : LocalDateTime
    private var distance = 0.0

    var sdf = SimpleDateFormat("yyyy.MM.dd")
    lateinit var today : String
    val ONE_MEGABYTE: Long = 1024 * 1024
    private var sp : SharedPreferences? = null
    private lateinit var name : String
    private lateinit var nameList : ArrayList<String>

    private lateinit var searchEngine: SearchEngine
    private lateinit var searchRequestTask: AsyncOperationTask
    
    var distanceList: HashMap<String, String> = hashMapOf<String, String>()
    var durationList: HashMap<String, String> = hashMapOf<String, String>()
    var caloriesList: HashMap<String, String> = hashMapOf<String, String>()

    private var isPopup = false

    private var firstnolocation = true
    private var finding = ""
    private var originstr = ""
    private var destinationstr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = FragmentSearchBinding.inflate(layoutInflater)

//        this.context?.let { Mapbox.getInstance(it, R.string.matoken.toString()) }
//        Mapbox.getInstance(requireContext(), "sk.eyJ1Ijoia2FuZ2NoZW5neXU1MjkiLCJhIjoiY2xleDZmbnJjMmg2NTNzcnY0b2YzdHR2dSJ9.L5DcbA65cohPI3X1jHRptQ")
//
//        if (Mapbox.getAccessToken() == null) {
//            // Mapbox instance has not been initialized
//            println("kkkkkkkkkkk")
//        } else {
//            // Mapbox instance has been initialized
//            println("ppppppppppp")
//        }


        addressAutofill = AddressAutofill.create(getString(R.string.matoken))

//        mapView = binding.mapView
//        mapboxMap = mapView.getMapboxMap()
//        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS)

//        locationPermissionHelper = com.example.cyclopath.ui.search.LocationPermissionHelper(WeakReference(this))
//        locationPermissionHelper.checkPermissions {
//            onMapReady()
//        }
//        mapboxMap.addOnMapIdleListener {
//            if (ignoreNextMapIdleEvent) {
//                ignoreNextMapIdleEvent = false
//                return@addOnMapIdleListener
//            }
//
//            val mapCenter = mapboxMap.cameraState.center
//            findAddress(mapCenter)
//        }

//        if (ContextCompat.checkSelfPermission(requireContext(),
//                        Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED
//                &&
//                ContextCompat.checkSelfPermission(requireContext(),
//                        Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            askForLocationPermissions()
//        }

//        LocationEngineProvider.getBestLocationEngine(requireContext()).lastKnownLocationOrNull(requireContext()) { point ->
//            point?.let {
//                mapView.getMapboxMap().setCamera(
//                        CameraOptions.Builder()
//                                .center(point)
//                                .zoom(9.0)
//                                .build()
//                )
//                ignoreNextMapIdleEvent = true
//            }
//        }

        if (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            println("this1")
            firstnolocation = true
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_search, container, false)

        originText = root.findViewById(R.id.origin)
        destinationText = root.findViewById(R.id.destination)
        origin_focus = root.findViewById(R.id.origin_focus)
        destination_focus = root.findViewById(R.id.destination_focus)
        navigate = root.findViewById(R.id.navigate)
        recenter = root.findViewById(R.id.recenter)
        swap = root.findViewById(R.id.swap)
        record = root.findViewById(R.id.record)
        mapView = root.findViewById(R.id.mapView)
        mapboxMap = mapView.getMapboxMap()
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS)
        searchResultsViewOrigin = root.findViewById(R.id.search_results_view_origin)
        searchResultsViewDestination = root.findViewById(R.id.search_results_view_destination)
        upload = root.findViewById(R.id.upload)
        elevation = root.findViewById(R.id.elevation)
//        elevation = root.findViewById(R.id.elevation)
        dropdown = root.findViewById(R.id.dropdown)
        timer = root.findViewById(R.id.timer)
        weatherclick = root.findViewById<View>(R.id.weather_clickable)
        
        sp = context?.getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        name = sp!!.getString("username", "user")!!

        mapboxMap.setCamera(CameraOptions.Builder().center(Point.fromLngLat(-3.1870091, 55.9443771)).zoom(14.0).build())

//        initNavigation()

        if (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PERMISSIONS_REQUEST_LOCATION
            )
        }

        if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            askForLocationPermissions()
        } else {
            val lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)

            if (!gps_enabled) {
//                firstnolocation = true
                Toast.makeText(context, "Please enable your location service.", Toast.LENGTH_SHORT).show()
            }
            onMapReady()

//            if (gps_enabled) {
//                locationPermissionHelper = LocationPermissionHelper(WeakReference(activity))
//                locationPermissionHelper.checkPermissions {
//                    onMapReady()
//                }
//            } else {
//                onMapReady()
//                Toast.makeText(context, "Please enable your location service.", Toast.LENGTH_SHORT).show()
//            }
        }

        annotationApi = mapView.annotations
        pointAnnotationManager = annotationApi?.createPointAnnotationManager(mapView)

        searchResultsViewOrigin.initialize(
                SearchResultsView.Configuration(
                        commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL)
                )
        )

        searchResultsViewDestination.initialize(
                SearchResultsView.Configuration(
                        commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL)
                )
        )

        searchEngineUiAdapterOrigin = AddressAutofillUiAdapter(
                view = searchResultsViewOrigin,
                addressAutofill = addressAutofill
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
                if (ignoreNextQueryTextUpdateOrigin) {
                    ignoreNextQueryTextUpdateOrigin = false
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
                if (ignoreNextQueryTextUpdateDestination) {
                    ignoreNextQueryTextUpdateDestination = false
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

        if (first) {
            first = false
            retrieveDistance()

        } else {
            if (this::origin.isInitialized) {
                isOrigin = true
                addAnnotationToMap(origin)
                isOrigin = false
                ignoreNextQueryTextUpdateOrigin = true
                ignoreNextMapIdleEvent = true
                mapboxMap.setCamera(
                        CameraOptions.Builder()
                                .center(origin)
                                .zoom(16.0)
                                .build()
                )
            }
            if (this::destination.isInitialized) {
                isOrigin = false
                addAnnotationToMap(destination)
                isOrigin = true
                ignoreNextQueryTextUpdateDestination = true
                ignoreNextMapIdleEvent = true
                mapboxMap.setCamera(
                        CameraOptions.Builder()
                                .center(destination)
                                .zoom(16.0)
                                .build()
                )
            }
            if (this::origin.isInitialized && this::destination.isInitialized) {
                fetchARoute(origin,destination)
            }
        }

        origin_focus.setOnClickListener{
            isOrigin = true
            ignoreNextMapIdleEvent = true
            origin = current
            ignoreNextQueryTextUpdateOrigin = true

            addAnnotationToMap(current)

            originText.setText("Your Location")
            originText.clearFocus()
            searchResultsViewOrigin.isVisible = false
            searchResultsViewOrigin.hideKeyboard()

            if (this::origin.isInitialized && this::destination.isInitialized) {
                fetchARoute(origin, destination)
            }

        }

        destination_focus.setOnClickListener{
            isOrigin = false
            ignoreNextMapIdleEvent = true
            destination = current
            ignoreNextQueryTextUpdateDestination = true

            addAnnotationToMap(current)

            destinationText.setText("Your Location")
            destinationText.clearFocus()
            searchResultsViewDestination.isVisible = false
            searchResultsViewDestination.hideKeyboard()

            if (this::origin.isInitialized && this::destination.isInitialized) {
                fetchARoute(origin, destination)
            }

        }

        navigate.setOnClickListener {
            if (this::route.isInitialized) {
                if (origin == current) {
                    var intent = Intent(activity, NavigationActivity::class.java)
                    intent.putExtra("origin", origin)
                    intent.putExtra("route", route)
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "Please specify the current location as origin to navigate.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please specify your route.", Toast.LENGTH_SHORT).show()
            }
        }

        recenter.setOnClickListener {
            val lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)

            if (gps_enabled && ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                if (firstnolocation) {
//                    firstnolocation = false
//                    locationPermissionHelper = LocationPermissionHelper(WeakReference(activity))
//                    locationPermissionHelper.checkPermissions {
//                        onMapReady()
//                    }
//                    Toast.makeText(context,"Detecting current location.",Toast.LENGTH_SHORT).show()
//                    Handler().postDelayed({
//                        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(current).build())
//                    }, 1000)
//                } else {
                updateCamera(current,0.0)
//                mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(current).build())
//                }
            } else {
                Toast.makeText(context,"Please enable your location service.",Toast.LENGTH_SHORT).show()
            }
        }

        swap.setOnClickListener {
            if (this::origin.isInitialized && this::destination.isInitialized) {
                val temp = origin
                val temptext = originText.text
                origin = destination
                originText.setText(destinationText.text)
                destination = temp
                destinationText.setText(temptext)
                originText.clearFocus()
                searchResultsViewOrigin.isVisible = false
                searchResultsViewOrigin.hideKeyboard()
                destinationText.clearFocus()
                searchResultsViewDestination.isVisible = false
                searchResultsViewDestination.hideKeyboard()
                isOrigin = true
                addAnnotationToMap(origin)
                isOrigin = false
                addAnnotationToMap(destination)
            }
        }

        record.setOnClickListener {
            if (isRecord) {

                retrieveData()

                isPopup = true

                val inflater = context?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val popupView: View = inflater.inflate(R.layout.popup_record, null)

                val popupWindow = PopupWindow(popupView, 1000, 600)
                popupWindow.isFocusable = true

//                lay.foreground.alpha = 120

                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)

                val cal: Calendar = Calendar.getInstance()
                today = sdf.format(cal.time)

                val curr = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val formatted = curr.format(formatter)

                val formattedstart = start.format(formatter)

                val textinput = popupWindow.contentView.findViewById<TextInputEditText>(R.id.filename_input)
                textinput.setText(formattedstart)

                val yesb = popupWindow.contentView.findViewById<Button>(R.id.record_save)
                yesb.setOnClickListener {
                    if (!nameList.contains(textinput.text.toString())) {
                        timer.stop()
                        timer.setBase(SystemClock.elapsedRealtime())
                        popupWindow.dismiss()
//                        lay.foreground.alpha = 0

                        Toast.makeText(context, "Stop recording", Toast.LENGTH_SHORT).show()
                        isRecord = false
                        record.setImageResource(R.drawable.startrecording)

                        val filename = textinput.text

                        val lineString = LineString.fromLngLats(routeCoordinates)
                        val feature = Feature.fromGeometry(lineString)

                        var sp = context?.getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
                        val name = sp!!.getString("username","empty")
                        var filepath = "history/$name/$filename.geojson"
                        var storageRef = storage.reference
                        var dataRef = storageRef.child(filepath)

                        val baos = ByteArrayOutputStream()
                        baos.write(feature.toJson().toByteArray())
                        val data = baos.toByteArray()
                        dataRef.putBytes(data)

                        end = curr
                        var duration = (end.hour-start.hour)*3600 + (end.minute - start.minute)*60 + (end.second-start.second)
                        var calories = distance*0.032
                        var d = duration.toString()
                        var infopath = "history/$name/$filename.txt"
                        var infoRef = storageRef.child(infopath)

                        if (distanceList.keys.contains(today)) {
                            distanceList[today] = (distanceList[today]!!.toFloat() + distance).toString()
                            durationList[today] = (durationList[today]!!.toFloat() + d.toFloat()).toString()
                            caloriesList[today] = (caloriesList[today]!!.toFloat() + calories).toString()
                        } else {
                            distanceList[today] = distance.toString()
                            durationList[today] = d
                            caloriesList[today] = calories.toString()
                        }

                        uploadDistance()

                        val baos2 = ByteArrayOutputStream()
                        var first = ""
                        var second = ""
                        if (routeCoordinates.size <= 1) {
                            finding = "start"
                            findAddress(current.longitude(), current.latitude())
                            Handler().postDelayed({
                                finding = "end"
                                findAddress(current.longitude(), current.latitude())
                            }, 500)
//                            first = "origin=" + current.latitude().toString() + "," + current.longitude().toString()
//                            second = "destination=" + current.latitude().toString() + "," + current.longitude().toString()
                        } else {
                            println(routeCoordinates)
                            finding = "start"
                            findAddress(routeCoordinates.first().longitude(), routeCoordinates.first().latitude())
                            Handler().postDelayed({
                                finding = "end"
                                findAddress(routeCoordinates.last().longitude(), routeCoordinates.last().latitude())
                            }, 500)
//                            first = "origin=" + routeCoordinates.first().latitude().toString() + "," + routeCoordinates.first().longitude().toString()
//                            second = "destination=" + routeCoordinates.last().latitude().toString() + "," + routeCoordinates.last().longitude().toString()
                        }

                        Handler().postDelayed({
                            println("HELLOOO")
                            println(originstr)
                            println(destinationstr)
                            first = "origin=$originstr"
                            second = "destination=$destinationstr"
                            val third = "duration=$d"
                            val fourth = "distance="+String.format("%.2f",distance)
                            val fifth = "start=$formattedstart"
                            val sixth = "end=$formatted"
                            val seventh = "calories=$calories"
                            var maxLong = -90.0
                            var minLong = 90.0
                            var maxLat = -90.0
                            var minLat = 90.0
                            for (i in routeCoordinates) {
                                maxLong = max(maxLong, i.longitude())
                                minLong = min(minLong, i.longitude())
                                maxLat = max(maxLat, i.latitude())
                                minLat = min(minLat, i.latitude())
                            }
                            baos2.write(first.toByteArray())
                            baos2.write("\n".toByteArray())
                            baos2.write(second.toByteArray())
                            baos2.write("\n".toByteArray())
                            baos2.write(third.toByteArray())
                            baos2.write("\n".toByteArray())
                            baos2.write(fourth.toByteArray())
                            baos2.write("\n".toByteArray())
                            baos2.write(fifth.toByteArray())
                            baos2.write("\n".toByteArray())
                            baos2.write(sixth.toByteArray())
                            baos2.write("\n".toByteArray())
                            baos2.write(seventh.toByteArray())
                            baos2.write("\n".toByteArray())
                            baos2.write(String.format("maxLong=%.8f",maxLong).toByteArray())
                            baos2.write("\n".toByteArray())
                            baos2.write(String.format("minLong=%.8f",minLong).toByteArray())
                            baos2.write("\n".toByteArray())
                            baos2.write(String.format("maxLat=%.8f",maxLat).toByteArray())
                            baos2.write("\n".toByteArray())
                            baos2.write(String.format("minLat=%.8f",minLat).toByteArray())
                            baos2.write("\n".toByteArray())
                            val data2 = baos2.toByteArray()
                            infoRef.putBytes(data2)

                            distance = 0.0
                            routeCoordinates = ArrayList<Point>()

                            Toast.makeText(context, "Successfully saved track!", Toast.LENGTH_SHORT).show()
                        }, 1000)
                    } else {
                        Toast.makeText(context,"This name has been used.", Toast.LENGTH_SHORT).show()
                    }
                }
                val nob = popupWindow.contentView.findViewById<Button>(R.id.record_discard)
                nob.setOnClickListener {
                    popupWindow.dismiss()
                    Toast.makeText(context, "Recording discarded.", Toast.LENGTH_SHORT).show()
                    isRecord = false
                    record.setImageResource(R.drawable.startrecording)
                    timer.stop()
                    timer.setBase(SystemClock.elapsedRealtime())
                    distance = 0.0
                    routeCoordinates = ArrayList<Point>()

                }
            } else {
                timer.setBase(SystemClock.elapsedRealtime())
                timer.start()
                Toast.makeText(context, "Start recording", Toast.LENGTH_SHORT).show()
                isRecord = true
                record.setImageResource(R.drawable.stoprecording)
                start = LocalDateTime.now()
            }
        }


        weatherclick.setOnClickListener{
            val i = Intent(activity, WeatherActivity::class.java)
            startActivity(i)
        }



//        elevation.setOnClickListener{
//            if (this::route.isInitialized) {
//                // TODO route is the DirectionRoute
//                // Create a Mapbox Elevation client
//                val client = MapboxElevation.builder()
//                    .accessToken(R.string.matoken)
//                    .build()
//
//
//                }
//
//        }

        elevation.setOnClickListener{
            if (this::route.isInitialized) {
                val inflater = context?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val popupView: View = inflater.inflate(R.layout.popup_elevation, null)

                val popupWindow = PopupWindow(popupView, 1100, 800)
                popupWindow.isFocusable = true

                popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 20.dpToPx(resources))

                val totalup = popupWindow.contentView.findViewById<TextView>(R.id.up_ele)
                val totaldown = popupWindow.contentView.findViewById<TextView>(R.id.down_ele)
                chart = popupWindow.contentView.findViewById<BarChart>(R.id.elevation_chart)
                val upstr : CharSequence = totalUp.toInt().toString() + " m"
                val downstr : CharSequence = totalDown.toInt().toString() + " m"
                totalup.text = upstr
                totaldown.text = downstr

                // Initialize the GeoApiContext
                val geoApiContext = GeoApiContext()
                geoApiContext.setApiKey("AIzaSyDjAFFs1s-IfgS7-sFsK1E2n9DVtYNIvXU")

                GlobalScope.launch(Dispatchers.IO) {
                    // Get the coordinates for each point along the route
                    val points = mutableListOf<com.google.maps.model.LatLng>()
                    for (leg in route.legs()!!) {
                        for (step in leg.steps()!!) {
                            val stepPoints = step.geometry()
                            if (stepPoints!=null){
                                val stepPoints = LineString.fromPolyline(stepPoints, 6).coordinates()
                                stepPoints.forEach {
                                    points.add(com.google.maps.model.LatLng(it.latitude(),it.longitude()))
                                }
                            }
                        }
                    }
                    // Get the elevation data along the path
                    val batchSize = 512
                    val batches = points.chunked(batchSize)

                    val results = mutableListOf<ElevationResult>()

                    for (batch in batches) {
                        val batchResults = ElevationApi.getByPoints(geoApiContext, *batch.toTypedArray()).await()
                        results.addAll(batchResults)
                    }

                    totalUp = 0.0
                    totalDown = 0.0
                    // Create a list of BarEntry objects to hold the elevation data
                    val entries = ArrayList<BarEntry>()
                    var lastElevation = results.first().elevation.toFloat()
                    var distancePer = (route.distance()/1000/points.size).toFloat()

                    // Loop through the elevation results and add them to the BarEntry list
                    results.forEachIndexed { index, result ->
                        val elevation = result.elevation.toFloat()
                        entries.add(BarEntry(distancePer*index.toFloat(), elevation))
                        if (elevation > lastElevation) {
                            totalUp += elevation - lastElevation
                        } else {
                            totalDown += lastElevation - elevation
                        }
                        lastElevation = elevation
                    }

                    println("Total up: $totalUp meters")
                    println("Total down: $totalDown meters")


                    // Create a BarDataSet from the BarEntry list
                    val dataSet = BarDataSet(entries, "Elevation")

                    // Set the colors of the bars
                    dataSet.colors = listOf(Color.rgb(25,57,26))


                    // Create a BarData object from the BarDataSet
                    val data = BarData(dataSet)
                    // Set the data to the chart
                    chart.data = data

                    // Customize the chart
                    chart.setDrawGridBackground(false)
                    chart.setDrawBorders(false)
                    chart.description.isEnabled = false
                    chart.legend.isEnabled = false
                    chart.axisLeft.axisMinimum = 0f
                    chart.axisRight.isEnabled = false
                    chart.xAxis.isEnabled = true


                    // Set up the Y-axis label
                    val yAxis = chart.axisLeft
                    yAxis.labelCount = 5
                    yAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return String.format("%.0f m", value)
                        }
                    }

                    // Set up the X-axis label
                    val xAxis = chart.xAxis
                    xAxis.labelCount= 5
                    xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return String.format("%.1f km", value)
                        }
                    }


                    // Refresh the chart
                    chart.invalidate()
                    barChartFlag = true


                }

            }else {
                Toast.makeText(context,"Please specify your route.", Toast.LENGTH_SHORT).show()
            }
        }

        upload.setOnClickListener {
//            println(getElevationFromGoogleMaps(-3.361678,55.942617))
            if (this::route.isInitialized) {
                if(this::chart.isInitialized && barChartFlag==true){
                // TODO route is the DirectionRoute
                val inflater = context?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val popupView: View = inflater.inflate(R.layout.popup_shareroute, null)

                val popupWindow = PopupWindow(popupView, 1000, 900)
                popupWindow.isFocusable = true

                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
                var sp = context?.getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
                val name = sp!!.getString("username","empty")
                val form = DateTimeFormatter.ofPattern(" yyyy-MM-dd")
                val routetime = "Route "+LocalDateTime.now().format(form)

                val routeNameInput = popupWindow.contentView.findViewById<TextInputEditText>(R.id.route_name_input)
                routeNameInput.setText(name+routetime)

                val descriptionInput = popupWindow.contentView.findViewById<TextInputEditText>(R.id.description_input)
                descriptionInput.setText("write your descriptions here")

                popupWindow.contentView.findViewById<Button>(R.id.share_to_library).setOnClickListener {
                    // TODO store all the info
                    val temp = RouteObj()

                    temp.route_name_text = routeNameInput.text.toString()
                    temp.route_description_text = descriptionInput.text.toString()
                    temp.route_duration = String.format("%.2f",route.duration()/60)+"mins"
                    temp.route_length_text = String.format("%.2f",route.distance()/1000)+"km"
                    temp.geoJsonurl = "routegeojson/${temp.route_name_text}.geojson"
                    temp.route_distance = route.distance()/1000


                    val storageRef = Firebase.storage.reference

                    // store routeObj.json
                    val gson = Gson()



//                    // snapshot
//                    val snapShotOptions = com.mapbox.mapboxsdk.snapshotter.MapSnapshotter.Options(500, 500)
//                    snapShotOptions.withRegion(mapboxMap.projection.visibleRegion.latLngBounds)
//                    snapShotOptions.withStyle(mapboxMap.style!!.url)
//                    val mapSnapshotter = MapSnapshotter(this, snapShotOptions)

                    val southwest = LatLng(55.942617, -3.361678)
                    val northeast = LatLng(55.985612, -3.176283)
                    val edinburghBounds = LatLngBounds.Builder().include(southwest).include(northeast).build()


                    val options = com.mapbox.mapboxsdk.snapshotter.MapSnapshotter.Options(500, 500)
                    options.withRegion(edinburghBounds)

//
//                    val mapSnapshotter = com.mapbox.mapboxsdk.snapshotter.MapSnapshotter(requireContext(),options)

//                    this.context?.let { Mapbox.getInstance(it, R.string.matoken.toString()) }
//                    Mapbox.getInstance(requireContext(), R.string.matoken.toString())

//                    if (mapSnapshotter != null) {
//                        mapSnapshotter.start { snapshot ->
//                            // Do something with the snapshot, for example save it to Firebase
//                            // The snapshot is ready, you can now use it
//                            val baos = ByteArrayOutputStream()
//                            snapshot!!.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//                            val data = baos.toByteArray()
//                            val imagesRef = storageRef.child("map_snapshots/${temp.route_name_text}.jpg")
//                            val uploadTask = imagesRef.putBytes(data)
//                            uploadTask.addOnSuccessListener {
//                                // The snapshot has been successfully uploaded to Firebase Storage
//                                println("success snapshot 222222222222222222222222")
//                            }.addOnFailureListener {
//                                // Handle any errors that occurred during the upload
//                                println("sssssssssssssss snapshot fail")
//                            }
//                        }
//                    }

                    println("aaaaaaaaaaaaaaaaaa")


//                    // store snapshot.jpg
//                    val snapshot: Bitmap = mapView.snapshot()!!
//                    val imagesRef = storageRef.child("map_snapshots/${temp.route_name_text}.jpg")
////
//                    if(snapshot==null){
//                        println("snapshot")
//                    }
//
//                    val baos = ByteArrayOutputStream()
//                    snapshot!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//                    val data = baos.toByteArray()
//
//                    val uploadTask = imagesRef.putBytes(data)
//                    uploadTask.addOnSuccessListener {
//                        // The snapshot has been successfully uploaded to Firebase Storage
//                    }.addOnFailureListener {
//                        // Handle any errors that occurred during the upload
//                    }

                    // Get the coordinates for each point along the route
//                    val coordinates = mutableListOf<Point>()
//
//                    for (leg in route.legs()!!) {
//                        for (step in leg.steps()!!) {
//                            coordinates.addAll(step.geometry!!.coordinates())
//                        }
//                    }


                    // points1 get the points
                    val points1 = mutableListOf<Point>()
                    for (leg in route.legs()!!) {
                        for (step in leg.steps()!!) {
                            val stepPoints = step.geometry()
                            val decodedPoints = PolylineUtils.decode(stepPoints!!, 6)
                            val latLngPoints = decodedPoints.map { LatLng(it.latitude(), it.longitude()) }
                        }
                    }




                    // Get the coordinates for each point along the route
                    val points = mutableListOf<com.google.maps.model.LatLng>()
                    val points10 = mutableListOf<com.google.maps.model.LatLng>()
                    for (leg in route.legs()!!) {
                        for (step in leg.steps()!!) {
                            val stepPoints = step.geometry()
                            if (stepPoints!=null){
                                val stepPoints = LineString.fromPolyline(stepPoints, 6).coordinates()
                                stepPoints.forEach {
                                    points.add(com.google.maps.model.LatLng(it.latitude(),it.longitude()))
                                }
                            }
                        }
                    }
                    var i = 0
                    for( p in points){
                        if(i%10 == 0){
                            points10.add(p)
                        }
                        i++
                    }


                    // get the list of encoded polylines
                    val encodedPolylines = mutableListOf<String>()
                    for (leg in route.legs()!!) {
                        for (step in leg.steps()!!) {
                            val stepPoints = step.geometry()
//                            println(stepPoints)
                            val polyline = PolylineUtils.decode(stepPoints!!, 5)
                            val encodedPolyline = PolylineUtils.encode(polyline,5)
                            encodedPolylines.add(encodedPolyline)
                        }
                    }


                    val startcoordinate = points[0]
                    val endcoordinate = points[1]

                    val pline_test = "e`{}DcfuvDwTnNaIf`@wW|x@un@tf@oe@|L}UnUwAn`Alf@fo@"

                    // Construct a Mapbox Static Images API URL with the desired parameters

                    val width = "400" // Width of the static image in pixels
                    val height = "400" // Height of the static image in pixels
                    val zoom = "12" // Zoom level of the map
                    val apiKey = "pk.eyJ1Ijoia2FuZ2NoZW5neXU1MjkiLCJhIjoiY2xleDZjZDR3MGFrcDN4bjB2ZnFwNmVyeiJ9.ztnMUP3hoSD75UWAiU55Aw" // Your Mapbox access token
                    val markerColor = "0000FF" // Color of the route marker in hexadecimal format
                    val markerSize = "large" // Size of the route marker
//                    val markerCoordinates = points?.joinToString(";") { "${it.longitude},${it.latitude}" } // String representation of the coordinates for the route marker

                    val startColor = "#FF512F"
                    val endColor = "#F09819"
                    val strokeWidth = 4

//                    val colorA = hexStringToRGB(startColor)
//                    val colorB = hexStringToRGB(endColor)
//                    val spectrumColors = createSpectrum(colorA, colorB, points.size - 1)

//                    val pathStrings = mutableListOf<String>()

//                     for (i in 0 until coords.size - 1) {
//                         val path = PolylineUtils.encode(
//                             listOf(points[i], points[i + 1]),
//                             5
//                         ) // use your preferred precision here
//                         pathStrings.add("path-$strokeWidth+${spectrumColors[i]}($path)")
//                     }

//                    val pathStrings = mutableListOf<String>()
//                    for (i in 0 until points.size - 1) {
//                        val path = PolylineUtils.encode(points1.subList(i, i + 2),5)
//                        pathStrings.add("path-$strokeWidth+${spectrumColors[i]}($path)") // format from https://docs.mapbox.com/api/maps/#path
//                    }
//                    val formatedPathString = pathStrings.joinToString(",")
//
//                    val firstCoord = points.first()
//                    val lastCoord = points.last()
//                    val startMarker = "pin-s-a+${rgbToHexString(colorA)}(${firstCoord.longitude},${firstCoord.latitude})"
//                    val endMarker = "pin-s-b+${rgbToHexString(colorB)}(${lastCoord.longitude},${lastCoord.latitude})"
//
//                    val pathWithGradient = "$formatedPathString,$startMarker,$endMarker"
//                    val url = pathWithGradient.encode()


//                    val url = "$baseUrl$markerSize-pin-s+${markerColor}($markerCoordinates)/$markerCoordinates,$zoom/$width$height@2x?access_token=$apiKey"
//                    val url = "https://api.mapbox.com/styles/v1/mapbox/streets-v11/static/pin-s+0000FF(-122.4200,37.7800)/-122.4200,37.7800,12/800x600?access_token=$apiKey"
//                    val url = "https://api.mapbox.com/styles/v1/mapbox/streets-v11/static/path-5+f44-0.5(0.5)/auto/400x400?access_token=$apiKey&overlay.geometry=geojson({\"type\":\"LineString\",\"coordinates\":[[<lon1>,<lat1>],[<lon2>,<lat2>]]})&overlay.type=line"

//                    // Construct the URL
//                    val urlBuilder = StringBuilder()
//                        .append(baseUrl)
//                        .append("path-5+f44(${points.joinToString(separator = ",") { "${it.longitude},${it.latitude}"}})")
//                        .append("/auto")
//                        .append("($points)")
//                        .append(",$markerColor-$markerSize")
//                        .append("/$width" + "x" + "$height")
//                        .append("?access_token=$apiKey")
//                        .append("&zoom=$zoom")
//                    val url = urlBuilder.toString()

                    val successUrl = "https://api.mapbox.com/styles/v1/mapbox/streets-v12/static/pin-s-a+9ed4bd(-122.46589,37.77343),pin-s-b+000(-122.42816,37.75965),path-5+f44-0.5(${encodedPolylines})/auto/500x300?access_token="
//
                    val baseUrl = "https://api.mapbox.com/styles/v1/mapbox/streets-v11/static/"
                    val staticMapUrlBuilder = StringBuilder()
                    staticMapUrlBuilder.append(baseUrl)
                    staticMapUrlBuilder.append("path-5+f44-0.5(")
                    staticMapUrlBuilder.append(PolylineEncoding.encode(points10))
                    staticMapUrlBuilder.append(")/auto/500x300?access_token=")
                    staticMapUrlBuilder.append(apiKey)
                    val url2 = staticMapUrlBuilder.toString()
                    println("000000000000000000000000000000000000000000000000000000000")
                    println(url2)


//                    println(route.geometry())

                    // Use an image loading library to load the static image into an ImageView
//                    Glide.with(this)
//                        .load(url2)
//                        .into(record)



                    // Initialize the GeoApiContext
                    val geoApiContext = GeoApiContext()
                    geoApiContext.setApiKey("AIzaSyDjAFFs1s-IfgS7-sFsK1E2n9DVtYNIvXU")

                    //push bar chart
                    var chartpath = "routeBarChart/${temp.route_name_text}.png"
                    var chartRef = storageRef.child(chartpath)

                    chart.setDrawingCacheEnabled(true)
                    chart.buildDrawingCache()
                    val bitmap = Bitmap.createBitmap(chart.getDrawingCache())
                    chart.setDrawingCacheEnabled(false)

                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                    val data = baos.toByteArray()

                    val uploadTask = chartRef.putBytes(data)
                    uploadTask.addOnSuccessListener {
                        // Image upload successful
                    }.addOnFailureListener {
                        // Image upload failed
                    }

                    temp.route_up = totalUp.toInt()
                    temp.route_down = totalDown.toInt()
                    temp.startLng = points[0].lng
                    temp.startLat = points[0].lat

                    temp.near = sqrt((temp.startLng-55.944171) *(temp.startLng-55.944171) + (temp.startLat+3.186810)*(temp.startLat+3.186810))

                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(points[0].lat, points[0].lng, 1)
                    if (addresses.isNotEmpty()) {
                        temp.route_start = addresses[0].getAddressLine(0)
                    }
                    val addresses2 = geocoder.getFromLocation(points[points.size-1].lat, points[points.size-1].lng, 1)
                    if (addresses2.isNotEmpty()) {
                        temp.route_end = addresses2[0].getAddressLine(0)
                    }


                    // push route geojson
                    val routeGeometry: LineString = LineString.fromPolyline(route.geometry()!!, 6)
                    val feature = Feature.fromGeometry(routeGeometry)

                    var filepath = "routegeojson/${temp.route_name_text}.geojson"
                    var dataRef = storageRef.child(filepath)

                    val featureData = feature.toJson().toByteArray()
                    dataRef.putBytes(featureData)

                    // push directionsroute
                    val drRoute = gson.toJson(route)
                    val drrouteRef = storageRef.child("drroutes/${temp.route_name_text}.json")
                    drrouteRef.putBytes(drRoute.toByteArray())

                    // return success message
                    Toast.makeText(context, "Successfully share route!", Toast.LENGTH_SHORT).show()

                    // Construct the URL for the static map image
                    // Calculate the bounds of the route
                    var minLat = Double.MAX_VALUE
                    var maxLat = -Double.MAX_VALUE
                    var minLng = Double.MAX_VALUE
                    var maxLng = -Double.MAX_VALUE

                    points10.forEach { point ->
                        minLat = minOf(minLat, point.lat)
                        maxLat = maxOf(maxLat, point.lat)
                        minLng = minOf(minLng, point.lng)
                        maxLng = maxOf(maxLng, point.lng)
                    }

                    // Calculate the center of the bounds
                    val centerLat = (minLat + maxLat) / 2
                    val centerLng = (minLng + maxLng) / 2




                    // Calculate the middle point of the points list
                    val middlePoint = points10[points10.size / 2]
                    // Add the start and end markers to the markers variable
                    val startMarker = "${points.first().lat},${points.first().lng}"
                    val endMarker = "${points.last().lat},${points.last().lng}"
                    val markers = "color:red|label:S|$startMarker&markers=color:blue|label:E|$endMarker"
                    val googleKey = "AIzaSyDjAFFs1s-IfgS7-sFsK1E2n9DVtYNIvXU"
                    val size = "4000x300&scale=2"
                    val center = "$centerLat,$centerLng"
                    val izoom = calculateZoomLevel(maxLat, minLat, maxLng, minLng, 640)-1
                    val path = "color:0xff0000ff|weight:10%7Cenc:"+ PolylineEncoding.encode(points10)

                    println(middlePoint.lat)
                    println(middlePoint.lng)
                    popupWindow.dismiss()

                    temp.focusLng = centerLng
                    temp.focusLat = centerLat
                    temp.zoomlevel = izoom+1
                    temp.difficulty = calculateDifficulty(route.distance(),totalUp)
                    temp.difficulty_level = calculateDifficultyLevel(route.distance(),totalUp)
                    // push route
                    val routeObjJson = gson.toJson(temp)
                    val routeRef = storageRef.child("routes/${temp.route_name_text}.json")
                    routeRef.putBytes(routeObjJson.toByteArray())


                    val url = "https://maps.googleapis.com/maps/api/staticmap?&language=en&key=$googleKey&size=$size&center=$center&zoom=$izoom&markers=$markers&path=$path"
                    // Create a URL object from the image URL
                    val url1 = URL(url)

                    GlobalScope.launch(Dispatchers.IO) {
                        // Open a connection to the URL and read the image data
                        val connection = url1.openConnection() as HttpURLConnection
                        connection.doInput = true
                        connection.connect()
                        val input = connection.inputStream
                        val imageData = input.readBytes()

                        // Save the image data to Firebase storage
                        val staticimageRef = Firebase.storage.reference.child("images/${temp.route_name_text}.png")
                        staticimageRef.putBytes(imageData)

                    }
                }

                val discard = popupWindow.contentView.findViewById<Button>(R.id.share_discard)
                discard.setOnClickListener {
                    popupWindow.dismiss()
//                    lay.foreground.alpha = 0
                }


                }

            else {
                Toast.makeText(context,"Please look at the elevation first", Toast.LENGTH_SHORT).show()
            }}else{
                Toast.makeText(context,"Please specify your route.", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    fun calculateDifficulty(distance: Double, up: Double): Int {
        val climbingPerKm = up / (distance / 1000)  // convert meters to km
        return when {
            climbingPerKm < 10.7 -> 1   // Easy ride
            climbingPerKm < 15.2 -> 2   // Medium
            climbingPerKm < 21.3 -> 3   // Hard
            climbingPerKm < 30.5 -> 4   // Very hard
            else -> 5   // Hardcore
        }
    }

    fun calculateDifficultyLevel(distance: Double, up: Double): String {
        val climbingPerKm = up / (distance / 1000)  // convert meters to km
        return when {
            climbingPerKm < 10.7 -> "Easy ride"
            climbingPerKm < 15.2 -> "Medium"
            climbingPerKm < 21.3 -> "Hard"
            climbingPerKm < 30.5 -> "Very hard"
            else -> "Hardcore"
        }
    }


    fun calculateZoomLevel(maxLat: Double, minLat: Double, maxLng: Double, minLng: Double, mapWidth: Int): Int {
        val latRatio = (maxLat - minLat) / 500
        val lngRatio = (maxLng - minLng) / 1000
        val latZoom = ln(360 * mapWidth / 256 / lngRatio) / ln(2.0)
        val lngZoom = ln(180 * mapWidth / 256 / latRatio) / ln(2.0)
        val zoom = minOf(latZoom, lngZoom).toInt()
        val distance = route.distance()/150
        return when {
            distance >= 1000 -> 8
            distance >= 500 -> 9
            distance >= 256 -> 10
            distance >= 128 -> 11
            distance >= 64 -> 12
            distance >= 32 -> 13
            distance >= 16 -> 14
            distance >= 8 -> 15
            distance >= 4 -> 16
            distance >= 2 -> 17
            distance >= 1 -> 18
            distance >= 0.5 -> 19
            distance >= 0.25 -> 20
            distance >= 0.1 -> 21
            else -> 15 // just in case :)
        }
    }

    fun haversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371.0 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = (sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        println("ddddiiii")
        println(earthRadius * c)
        return earthRadius * c
    }

    inner class weatherTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            /* Showing the ProgressBar, Making the main design GONE */
        }

        override fun doInBackground(vararg params: String?): String? {
            val CITY: String = "edinburgh,uk"
            val API: String = "5c6caee0d64a8dd4571f62c65fc99f6f" // Use API key
            var response:String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API").readText(
                    Charsets.UTF_8
                )
            }catch (e: Exception){
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                val iconId = weather.getString("icon")
                val iconUrl = "https://openweathermap.org/img/w/$iconId.png"

                val updatedAt:Long = jsonObj.getLong("dt")
                val temp = main.getString("temp").dropLast(1)+"°C"
                val humidity = main.getString("humidity")

                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("main")

                /* Populating extracted data into our views */
                // Load the weather icon image using Glide
                Glide.with(this@SearchFragment).load(iconUrl).into(requireView().findViewById<ImageView>(R.id.weather_icon))

                requireView().findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                requireView().findViewById<TextView>(R.id.temp).text = temp

//                findViewById<TextView>(R.id.wind).text = windSpeed
//                findViewById<TextView>(R.id.pressure).text = pressure
//                findViewById<TextView>(R.id.humidity).text = humidity

                /* Views populated, Hiding the loader, Showing the main design */
            } catch (e: Exception) {
            }

        }
    }



    private fun getElevationFromGoogleMaps(longitude: Double, latitude: Double): Double {
        var result = Double.NaN
        val url = ("https://maps.googleapis.com/maps/api/elevation/"
                + "json?locations=" + latitude.toString() + "," + longitude.toString() + "&key=AIzaSyDjAFFs1s-IfgS7-sFsK1E2n9DVtYNIvXU")

        val urlConnection = URL(url).openConnection() as HttpURLConnection
        try {
            urlConnection.connect()
            val responseCode = urlConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = urlConnection.inputStream
                val jsonResponse = Scanner(inputStream).useDelimiter("\\A").next()
                val jsonObject = JSONObject(jsonResponse)
                val status = jsonObject.getString("status")
                if (status == "OK") {
                    val results = jsonObject.getJSONArray("results")
                    if (results.length() > 0) {
                        val elevation = results.getJSONObject(0).getDouble("elevation")
                        result = elevation
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            urlConnection.disconnect()
        }
        return result
    }

    private val searchCallback = object : SearchCallback {

        override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
            if (results.isEmpty()) {

            } else {
                if (finding == "origin") {
                    originText.setText(listOfNotNull(
                            results[0].address!!.houseNumber,
                            results[0].address!!.street
                    ).joinToString())
                    originText.clearFocus()
                    searchResultsViewOrigin.isVisible = false
                    searchResultsViewOrigin.hideKeyboard()
                } else if (finding == "destination") {
                    destinationText.setText(listOfNotNull(
                            results[0].address!!.houseNumber,
                            results[0].address!!.street
                    ).joinToString())
                    destinationText.clearFocus()
                    searchResultsViewDestination.isVisible = false
                    searchResultsViewDestination.hideKeyboard()
                } else if (finding == "start") {
                    originstr = listOfNotNull(
                            results[0].address!!.houseNumber,
                            results[0].address!!.street
                    ).joinToString()
                } else if (finding == "end") {
                    destinationstr = listOfNotNull(
                            results[0].address!!.houseNumber,
                            results[0].address!!.street
                    ).joinToString()
                }
            }
        }

        override fun onError(e: Exception) {
        }
    }

    fun findAddress(longitude: Double, latitude: Double) {

        searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
                SearchEngineSettings(getString(R.string.matoken))
        )

        val options = ReverseGeoOptions(
                center = Point.fromLngLat(longitude, latitude),
                limit = 1
        )
        searchRequestTask = searchEngine.search(options, searchCallback)
    }


    private fun initNavigation() {
        MapboxNavigationApp.setup(
                NavigationOptions.Builder(requireContext())
                        .accessToken(getString(R.string.matoken))
                        // comment out the location engine setting block to disable simulation
                        .locationEngine(replayLocationEngine)
                        .build()
        )
//        mapView.location.apply {
//            setLocationProvider(navigationLocationProvider)
//            enabled = true
//        }

//        locationComponent = mapView.location.apply {
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
//                        .pitch(45.0)
//                        .zoom(17.0)
                        .padding(EdgeInsets(0.0, 0.0, 0.0, 0.0))
                        .build(),
                mapAnimationOptionsBuilder.build()
        )
    }

    private fun fetchARoute(origin : Point, destination : Point) {

        val routeOptions = RouteOptions.builder()
                // applies the default parameters to route options
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(requireContext())
                // lists the coordinate pair i.e. origin and destination
                // If you want to specify waypoints you can pass list of points instead of null
                .coordinatesList(listOf(origin, destination))
                // set it to true if you want to receive alternate routes to your destination
                .alternatives(true)
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
                    }

                    override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    }

                    override fun onRoutesReady(
                            routes: List<NavigationRoute>,
                            routerOrigin: RouterOrigin
                    ) {

                        var strlist : ArrayList<String> = ArrayList<String>()
                        for (i in routes) {
                            val hours = i.directionsRoute.duration() / 3600
                            val minutes = (i.directionsRoute.duration() % 3600) / 60
                            val seconds = i.directionsRoute.duration() % 60

                            var timeString = ""
                            if (hours >= 1) {
                                timeString = String.format("%.0fH %.0fM %.0fS", hours, minutes, seconds)
                            } else if (minutes >= 1) {
                                timeString = String.format("%.0fM %.0fS", minutes, seconds)
                            } else {
                                timeString = String.format("%.0fS", seconds)
                            }
                            val distance = String.format("%.2f",i.directionsRoute.distance()/1000)
                            strlist.add("$distance KM ($timeString)")
                        }

                        route = routes[0].directionsRoute
                        barChartFlag = false

                        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(context!!,
                                R.layout.spinner_item, strlist)

                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                        text.visibility = View.INVISIBLE
                        dropdown.visibility = View.VISIBLE
                        dropdown.isClickable = true
                        dropdown.setAdapter(adapter)
                        dropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                route = routes[0].directionsRoute
                                (parent!!.getChildAt(0) as TextView).setTextColor(Color.WHITE)
                                mapboxNavigation.setNavigationRoutes(
                                        listOf(route).toNavigationRoutes(RouterOrigin.Offboard)
                                )
                            }
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                route = routes[position].directionsRoute
//                                (parent!!.getChildAt(position) as TextView).setTextColor(Color.WHITE)
                                mapboxNavigation.setNavigationRoutes(
                                        listOf(routes[position].directionsRoute).toNavigationRoutes(RouterOrigin.Offboard)
                                )
                            }
                        }

                        // add here

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
                ignoreNextQueryTextUpdateOrigin = true
            } else {
                destination = suggestion.coordinate
                ignoreNextQueryTextUpdateDestination = true
            }
        }

        addAnnotationToMap(suggestion.coordinate)

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
        Toast.makeText(requireContext(), getString(resId), Toast.LENGTH_SHORT).show()
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
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(requireContext())
                    .setTitle("Location permessions needed")
                    .setMessage("you need to allow this permission!")
                    .setPositiveButton("Sure") { dialog, which ->
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                Cyclopath.LOCATION_PERMISSION_REQUEST_CODE)
                    }
                    .setNegativeButton("Not now") { dialog, which ->
                        //                                        //Do nothing
                    }
                    .show()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    Cyclopath.LOCATION_PERMISSION_REQUEST_CODE)
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
            AlertDialog.Builder(requireContext())
                    .setTitle("Location permessions needed")
                    .setMessage("you need to allow this permission!")
                    .setPositiveButton("Sure") { dialog, which ->
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                                Cyclopath.LOCATION_PERMISSION_REQUEST_CODE)
                    }
                    .setNegativeButton("Not now") { dialog, which ->
                        //                                        //Do nothing
                    }
                    .show()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    Cyclopath.LOCATION_PERMISSION_REQUEST_CODE)
        }
        locationPermissionHelper = LocationPermissionHelper(WeakReference(activity))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
    }

    private fun addAnnotationToMap(point : Point) {
        if (isOrigin) {
            bitmapFromDrawableRes(
                    requireContext(),
                    R.drawable.start
            )?.let {
                val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                        .withPoint(point)
                        .withIconImage(it)
                if (this::annotationOrigin.isInitialized) {
                    pointAnnotationManager.delete(annotationOrigin)
                }
                annotationOrigin = pointAnnotationManager?.create(pointAnnotationOptions)
                mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(origin).build())
            }
        } else {
            bitmapFromDrawableRes(
                    requireContext(),
                    R.drawable.red_marker
            )?.let {
                val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                        .withPoint(point)
                        .withIconImage(it)
                if (this::annotationDestination.isInitialized) {
                    pointAnnotationManager.delete(annotationDestination)
                }
                annotationDestination = pointAnnotationManager?.create(pointAnnotationOptions)
                mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(destination).build())
            }
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

    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(
                CameraOptions.Builder()
                        .zoom(14.0)
                        .build()
        )
        mapView.getMapboxMap().loadStyleUri(
                Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
        mapView.gestures.addOnMapLongClickListener { point ->
            if (originText.text.toString() == "") {
                origin = point
                isOrigin = true
                ignoreNextMapIdleEvent = true
                ignoreNextQueryTextUpdateOrigin = true
                addAnnotationToMap(origin)
                isOrigin = false
                finding = "origin"
                findAddress(point.longitude(),point.latitude())
                originText.setText(String.format("%.4f", point.latitude()) + "," + String.format("%.4f", point.longitude()))
                originText.clearFocus()
            } else if (destinationText.text.toString() == "") {
                destination = point
                isOrigin = false
                ignoreNextMapIdleEvent = true
                ignoreNextQueryTextUpdateDestination = true
                addAnnotationToMap(destination)
                isOrigin = true
                finding = "destination"
                findAddress(point.longitude(),point.latitude())
                destinationText.setText(String.format("%.4f", point.latitude()) + "," + String.format("%.4f", point.longitude()))
                destinationText.clearFocus()
            }

            if (this::origin.isInitialized && this::destination.isInitialized) {
                fetchARoute(origin, destination)
            }

            true
        }

    }

    fun decToHex(dec: Int): String = if (dec < 16) "0" + dec.toString(16) else dec.toString(16)

    fun hexToDec(hex: String): Int = Integer.parseInt(hex, 16)

    fun rgbToHexString(rgb: List<Int>): String = decToHex(rgb[0]) + decToHex(rgb[1]) + decToHex(rgb[2])

    fun hexStringToRGB(hexString: String): List<Int> {
        val s = hexString.replace("#", "")
        return listOf(hexToDec(s.substring(0, 2)), hexToDec(s.substring(2, 4)), hexToDec(s.substring(4, 6)))
    }

    // sRGB: starting RGB color, like [255, 0, 0]
// eRGB: ending RGB color, like [122, 122, 122]
// numSteps: number of steps in the gradient
    fun createSpectrum(sRGB: List<Int>, eRGB: List<Int>, numSteps: Int): List<String> {
        val colors = mutableListOf<String>()
        for (i in 0 until numSteps) {
            val r = Math.round(((eRGB[0] - sRGB[0]).toFloat() * i / numSteps)) + sRGB[0]
            val g = Math.round(((eRGB[1] - sRGB[1]).toFloat() * i / numSteps)) + sRGB[1]
            val b = Math.round(((eRGB[2] - sRGB[2]).toFloat() * i / numSteps)) + sRGB[2]
            colors.add(rgbToHexString(listOf(r, g, b)))
        }
        return colors
    }



    fun getTime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatted = current.format(formatter)
        return formatted
    }

    fun retrieveDistance() {
        distanceList = HashMap<String, String>()
        durationList = HashMap<String, String>()
        caloriesList = HashMap<String, String>()
        val storageRef = storage.reference
        var dataRef = storageRef.child("distanceData/$name.txt")
        dataRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { it ->
            val data = String(it).lines()
            data.forEach{
                if (it != "") {
                    val strs = it.split(",").toTypedArray()
                    distanceList[strs[0]] = strs[1]
                    durationList[strs[0]] = strs[2]
                    caloriesList[strs[0]] = strs[3]
                }
            }
        }.addOnFailureListener {
            // Handle any errors
        }
    }

    fun retrieveData() {
        nameList = ArrayList<String>()
        val storageRef = storage.reference
        var dataRef = storageRef.child("history/$name")
        dataRef.listAll()
                .addOnSuccessListener { (items, prefixes) ->
                    items.forEach { item ->
                        if (item.name.endsWith(".geojson")) {
                            nameList.add(item.name.dropLast(8))
                        }
                    }
                }
                .addOnFailureListener {
                }
    }

    fun uploadDistance() {
        val storageRef = storage.reference
        val dataRef = storageRef.child("distanceData/$name.txt")
        val baos = ByteArrayOutputStream()
        for ((key, item) in distanceList) {
            val d = key
            val s = item
            val c = durationList[key]
            val ca = caloriesList[key]
            baos.write("$d,$s,$c,$ca".toByteArray())
            baos.write("\n".toByteArray())
        }
        val data = baos.toByteArray()
        dataRef.putBytes(data)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                    topImage = AppCompatResources.getDrawable(
                            requireContext(),
                            com.mapbox.maps.plugin.locationcomponent.R.drawable.mapbox_user_icon
                    ),
//                    bearingImage = AppCompatResources.getDrawable(
//                            requireContext(),
//                            com.mapbox.maps.plugin.locationcomponent.R.drawable.mapbox_user_bearing_icon
//                    ),
                    shadowImage = AppCompatResources.getDrawable(
                            requireContext(),
                            com.mapbox.maps.plugin.locationcomponent.R.drawable.mapbox_user_stroke_icon
                    ),
                    scaleExpression = interpolate {
                        linear()
                        zoom()
                        stop {
                            literal(0.0)
                            literal(0.6)
                        }
                        stop {
                            literal(20.0)
                            literal(1.0)
                        }
                    }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
//        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(Point.fromLngLat(-3.187194, 55.947388)).build())
//        Handler().postDelayed({
//            current = Point.fromLngLat(
//                    mapboxMap.cameraState.center.longitude(), mapboxMap.cameraState.center.latitude()
//            )
//        }, 1000)
    }

    private fun onCameraTrackingDismissed() {
//        Toast.makeText(context, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.location
                .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location
                .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

   @Override
   override fun onDestroy() {
       super.onDestroy()
       if (this::searchRequestTask.isInitialized) {
           searchRequestTask.cancel()
       }
       mapView.location
               .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
       mapView.location
               .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
       mapView.gestures.removeOnMoveListener(onMoveListener)
   }


    override fun onResume() {
        weatherTask().execute()
        super.onResume()
    }

    fun Int.dpToPx(resources: Resources): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            resources.displayMetrics
        ).toInt()
    }


}
