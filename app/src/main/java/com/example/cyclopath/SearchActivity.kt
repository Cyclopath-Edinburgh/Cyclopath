package com.example.cyclopath

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.search.autofill.*
import com.mapbox.search.ui.adapter.autofill.AddressAutofillUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView

class SearchActivity : AppCompatActivity() {
    private lateinit var addressAutofill: AddressAutofill

    private lateinit var searchResultsView: SearchResultsView
    private lateinit var searchEngineUiAdapter: AddressAutofillUiAdapter

    private lateinit var queryEditText: EditText

    private lateinit var apartmentEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var stateEditText: EditText
    private lateinit var zipEditText: EditText
    private lateinit var fullAddress: TextView
    private lateinit var pinCorrectionNote: TextView
    private lateinit var mapView: MapView
    private lateinit var mapPin: View
    private lateinit var mapboxMap: MapboxMap

    private var ignoreNextMapIdleEvent: Boolean = false
    private var ignoreNextQueryTextUpdate: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this@SearchActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(this@SearchActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            askForLocationPermissions()
        }
        setContentView(R.layout.activity_search)

        addressAutofill = AddressAutofill.create(getString(R.string.mapbox_access_token))

        queryEditText = findViewById(R.id.query_text)
        apartmentEditText = findViewById(R.id.address_apartment)
        cityEditText = findViewById(R.id.address_city)
        stateEditText = findViewById(R.id.address_state)
        zipEditText = findViewById(R.id.address_zip)
        fullAddress = findViewById(R.id.full_address)
        pinCorrectionNote = findViewById(R.id.pin_correction_note)

        mapPin = findViewById(R.id.map_pin)
        mapView = findViewById(R.id.map)
        mapboxMap = mapView.getMapboxMap()
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS)
        mapboxMap.addOnMapIdleListener {
            if (ignoreNextMapIdleEvent) {
                ignoreNextMapIdleEvent = false
                return@addOnMapIdleListener
            }

            val mapCenter = mapboxMap.cameraState.center
            findAddress(mapCenter)
        }

        searchResultsView = findViewById(R.id.search_results_view)

        searchResultsView.initialize(
                SearchResultsView.Configuration(
                        commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL)
                )
        )

        searchEngineUiAdapter = AddressAutofillUiAdapter(
                view = searchResultsView,
                addressAutofill = addressAutofill
        )

        LocationEngineProvider.getBestLocationEngine(applicationContext).lastKnownLocationOrNull(this) { point ->
            point?.let {
                mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                                .center(point)
                                .zoom(9.0)
                                .build()
                )
                ignoreNextMapIdleEvent = true
            }
        }

        searchEngineUiAdapter.addSearchListener(object : AddressAutofillUiAdapter.SearchListener {

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

        queryEditText.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                if (ignoreNextQueryTextUpdate) {
                    ignoreNextQueryTextUpdate = false
                    return
                }

                val query = Query.create(text.toString())
                if (query != null) {
                    lifecycleScope.launchWhenStarted {
                        searchEngineUiAdapter.search(query)
                        println("HEERE")
                        println(text)
                    }
                }
                searchResultsView.isVisible = query != null
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
                    this,
                    arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PERMISSIONS_REQUEST_LOCATION
            )
            println("PASS")
        }
    }

    private fun findAddress(point: Point) {
        println("FIND"+point)
        println(point.latitude())
        lifecycleScope.launchWhenStarted {
            when (val response = addressAutofill.suggestions(point, AddressAutofillOptions()))  {
                is AddressAutofillResponse.Suggestions -> {
                    if (response.suggestions.isEmpty()) {
                        showToast(R.string.address_autofill_error_pin_correction)
                    } else {
                        showAddressAutofillSuggestion(
                                response.suggestions.first(),
                                fromReverseGeocoding = true
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

    private fun showAddressAutofillSuggestion(suggestion: AddressAutofillSuggestion, fromReverseGeocoding: Boolean) {
        val address = suggestion.result().address
        cityEditText.setText(address.place)
        stateEditText.setText(address.region)
        zipEditText.setText(address.postcode)

        fullAddress.isVisible = true
        fullAddress.text = suggestion.formattedAddress

        pinCorrectionNote.isVisible = true

        if (!fromReverseGeocoding) {
            mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                            .center(suggestion.coordinate)
                            .zoom(16.0)
                            .build()
            )
            ignoreNextMapIdleEvent = true
            mapPin.isVisible = true
        }

        ignoreNextQueryTextUpdate = true
        queryEditText.setText(
                listOfNotNull(
                        address.houseNumber,
                        address.street
                ).joinToString()
        )
        queryEditText.clearFocus()

        searchResultsView.isVisible = false
        searchResultsView.hideKeyboard()
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
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                        .setTitle("Location permessions needed")
                        .setMessage("you need to allow this permission!")
                        .setPositiveButton("Sure") { dialog, which ->
                            ActivityCompat.requestPermissions(this@SearchActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    Cyclopath.LOCATION_PERMISSION_REQUEST_CODE)
                        }
                        .setNegativeButton("Not now") { dialog, which ->
                            //                                        //Do nothing
                        }
                        .show()

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        Cyclopath.LOCATION_PERMISSION_REQUEST_CODE)

                // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
}
