package com.example.happyplaces

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.happyplaces.data.Place
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private val viewModel: PlaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        setContentView(R.layout.activity_main) // oder Compose-Setup

        mapView = findViewById(R.id.mapView) // falls du XML nutzt
        mapView.setMultiTouchControls(true)

        viewModel.allPlaces.observe(this) { places ->
            mapView.overlays.clear()
            for (place in places) {
                val marker = Marker(mapView)
                marker.position = GeoPoint(place.latitude, place.longitude)
                marker.title = place.title
                mapView.overlays.add(marker)
            }
        }
    }
}
