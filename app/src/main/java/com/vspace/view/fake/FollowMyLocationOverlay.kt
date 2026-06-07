package com.vspace.view.fake

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.vcore.entity.location.BLocation
import com.vcore.utils.compat.BuildCompat
import com.vspace.databinding.ActivityOsmdroidBinding
import com.vspace.util.ToastEx.toast
import com.vspace.util.ViewBindingEx.inflate
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/**
 * Activity that displays an interactive OpenStreetMap (osmdroid) for selecting
 * a fake GPS coordinate. The user taps the map to place a marker; pressing
 * back returns the selected [GeoPoint] as the activity result.
 *
 * Receives an optional initial [BLocation] via the "location" intent extra
 * and the target package name via the "pkg" extra.
 *
 * On finish, returns RESULT_OK with "latitude" and "longitude" extras.
 */
class FollowMyLocationOverlay : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private val binding: ActivityOsmdroidBinding by inflate()
    /** The currently selected map point; returned as the result on back press. */
    lateinit var startPoint: GeoPoint

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // handle permissions first, before map is created. not depicted here

        // load/initialize the osmdroid configuration, this can be done
        // This won't work unless you have imported this: org.osmdroid.config.Configuration.*
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        // setting this before the layout is inflated is a good idea
        // it 'should' ensure that the map has a writable location for the map cache, even without permissions
        // if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        // see also StorageUtils
        // note, the load method also sets the HTTP User Agent to your application's package name, if you abuse osm's
        // tile servers will get you banned based on this string.

        // inflate and create the map
        setContentView(binding.root)

        // Read the initial location from the intent, handling API-level parcelable differences
        val location: BLocation? = if (BuildCompat.isT()) {
            intent.getParcelableExtra("location", BLocation::class.java)
        } else {
            intent.getParcelableExtra("location")
        }

        startPoint = if (location == null) {
            // Default to Hangzhou, China if no prior location is set
            GeoPoint(30.2736, 120.1563)
        } else {
            GeoPoint(location.latitude, location.longitude)
        }

        val startMarker = Marker(binding.map)
        startMarker.position = startPoint
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        binding.map.overlays.add(startMarker)
        // Handle map tap events: move the marker to the tapped location
        val mReceive: MapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                startPoint = p
                startMarker.position = p
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                binding.map.overlays.add(startMarker)

                toast(p.latitude.toString() + " - " + p.longitude)
                return false
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }
        binding.map.overlays.add(MapEventsOverlay(mReceive))
        val mapController = binding.map.controller
        mapController.setZoom(12.5)
        // val startPoint = GeoPoint(30.2736, 120.1563)
        mapController.setCenter(startPoint)
        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        onBackPressedDispatcher
            .addCallback(onBackPressedCallback)
    }

    private val onBackPressedCallback =
        object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishWithResult(startPoint)
            }
        }

    override fun onResume() {
        super.onResume()
        // this will refresh the osmdroid configuration on resuming.
        // if you make changes to the configuration, use
        // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this)
        // Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        binding.map.onResume() // needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onPause() {
        super.onPause()
        // this will refresh the osmdroid configuration on resuming.
        // if you make changes to the configuration, use
        // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this)
        // Configuration.getInstance().save(this, prefs)
        binding.map.onPause()  // needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionsToRequest = ArrayList<String>()
        var i = 0
        while (i < grantResults.size) {
            permissionsToRequest.add(permissions[i])
            i++
        }

        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    /**
     * Sets the activity result with the selected latitude/longitude and finishes,
     * also hiding the soft keyboard if visible.
     *
     * @param geoPoint the [GeoPoint] chosen by the user on the map.
     */
    private fun finishWithResult(geoPoint: GeoPoint) {
        intent.putExtra("latitude", geoPoint.latitude)
        intent.putExtra("longitude", geoPoint.longitude)
        setResult(Activity.RESULT_OK, intent)

        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        window.peekDecorView()?.run {
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
        finish()
    }
}
