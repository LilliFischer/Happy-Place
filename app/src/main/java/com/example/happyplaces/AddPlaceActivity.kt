package com.example.happyplaces

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.happyplaces.data.Place

class AddPlaceActivity : AppCompatActivity() {

    private val viewModel: PlaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_place)

        // Felder abrufen, Bild auswählen etc.

        // Beim Speichern
        val newPlace = Place(
            title = "Beispiel",
            description = "Beschreibung",
            imagePath = "pfad/zum/bild.jpg",
            latitude = 52.52,
            longitude = 13.405,
            note = "Meine Notiz",
            category = "Essen"
        )
        viewModel.insert(newPlace)
        finish()
    }
}
