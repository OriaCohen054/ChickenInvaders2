package com.example.chickeninvaders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.chickeninvaders.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * מפה שתגדיר Marker במיקום שתקבל ב־arguments ("latitude", "longitude", "score").
 */
class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {
    private var googleMap: GoogleMap? = null
    private var lat = 0.0
    private var lon = 0.0
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            lat = it.getDouble("latitude", 0.0)
            lon = it.getDouble("longitude", 0.0)
            score = it.getInt("score", 0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFrag = childFragmentManager
            .findFragmentById(R.id.googleMap) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction()
                    .replace(R.id.mapContainer, it)
                    .commit()
            }
        mapFrag.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val pos = LatLng(lat, lon)
        map.addMarker(MarkerOptions().position(pos).title("Score $score"))
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f), 1000, null)
    }
}
