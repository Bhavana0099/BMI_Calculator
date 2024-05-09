package com.zybooks.bmicalculator

import android.annotation.SuppressLint
import android.view.animation.AnimationUtils
import android.media.MediaPlayer
import android.app.AlertDialog
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.Menu
import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import android.content.pm.PackageManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.view.animation.Animation
import android.Manifest
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat



class MainActivity : AppCompatActivity() {
    private lateinit var weight: EditText
    private lateinit var height: EditText
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var scaleUpAnimation: Animation
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        val imageBoy = findViewById<ImageView>(R.id.image_boy)
        val imageGirl = findViewById<ImageView>(R.id.image_girl)
        weight = findViewById<EditText>(R.id.weight_value)
        height = findViewById<EditText>(R.id.height_value)
        val calculateButton = findViewById<Button>(R.id.calculate_button)
        val bmi = findViewById<TextView>(R.id.bmi)
        val bmiStatus = findViewById<TextView>(R.id.bmi_status)
        val bmiView = findViewById<LinearLayout>(R.id.bmiView)
        val calculateAgainButton = findViewById<TextView>(R.id.calculate_again)
        val infoFragment = InfoFragment()
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, infoFragment)
            addToBackStack(null) // optional: allows back navigation
            commit()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted, proceed to get user's location
            getUserLocation()
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.button_sound1) //load media player
        scaleUpAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_up) //load scale_up animation
        scaleUpAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                // Animation end
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })

        imageBoy.setOnClickListener {
            imageBoy.setImageResource(R.drawable.ic_boy)
            imageGirl.setImageResource(R.drawable.ic_girl_blur)
        }

        imageGirl.setOnClickListener {
            imageBoy.setImageResource(R.drawable.ic_boy_blur)
            imageGirl.setImageResource(R.drawable.ic_girl)
        }

        calculateButton.setOnClickListener {
            mediaPlayer.start() //Play sound when button is clicked
            var weightValue = 0.0
            var heightValue = 0.0
            if (weight.text.toString().isNotEmpty()) {
                weightValue = weight.text.toString().toDouble()
            }
            if (height.text.toString().isNotEmpty()) {
                heightValue = (height.text.toString().toDouble() / 100)
            }
            if (weightValue > 0.0 && heightValue > 0.0) {
                val bmiValue = String.format("%.2f", weightValue / heightValue.pow(2))
                bmi.text = bmiValue
                bmiStatus.text = bmiStatusValue(weightValue / heightValue.pow(2))
                bmiView.visibility = VISIBLE
                bmiView.startAnimation(scaleUpAnimation) // Apply fade-in animation to BMI result view
                calculateButton.visibility = GONE
            } else
                Toast.makeText(this, "Please Input Weight and Height Values greater than 0", Toast.LENGTH_LONG).show()
        }

        calculateAgainButton.setOnClickListener {
            bmiView.visibility = GONE
            calculateButton.visibility = VISIBLE
            showClearConfirmationDialog()
        }

        registerForContextMenu(weight)
        registerForContextMenu(height)

    }

    fun openMaps(view: View) {
        getUserLocation()
    }
    private fun getUserLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Location services disabled, prompt user to enable
            Toast.makeText(
                this,
                "Please enable location services",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission granted, get last known location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        // Location found, construct Google Maps URL
                        val googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=${it.latitude},${it.longitude}"
                        // Open Google Maps with the specified location
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsUrl))
                        startActivity(intent)
                    } ?: run {
                        Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Permission not granted, request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }


    private fun bmiStatusValue(bmi: Double): String {
        lateinit var bmiStatus: String
        if (bmi < 18.5)
            bmiStatus = "Underweight"
        else if (bmi >= 18.5 && bmi < 25)
            bmiStatus = "Normal"
        else if (bmi >= 25 && bmi < 30)
            bmiStatus = "Overweight"
        else if (bmi > 30)
            bmiStatus = "Obese"
        return bmiStatus
    }

    private fun showClearConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear Confirmation")
            .setMessage("Are you sure you want to clear input fields?")
            .setPositiveButton("Yes") { _, _ ->
                clearInputFields()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun clearInputFields() {
        weight.text.clear()
        height.text.clear()
        weight.requestFocus()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v != null) {
            when (v.id) {
                weight.id -> {
                    menu?.apply {
                        add(Menu.NONE, R.id.menu_clear_weight, Menu.NONE, "Clear Weight")
                        add(Menu.NONE, R.id.menu_copy_weight, Menu.NONE, "Copy Weight")
                    }
                }

                height.id -> {
                    menu?.apply {
                        add(Menu.NONE, R.id.menu_clear_height, Menu.NONE, "Clear Height")
                        add(Menu.NONE, R.id.menu_copy_height, Menu.NONE, "Copy Height")
                    }
                }
            }
        }
    }
    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_clear_weight -> weight.text.clear()
            R.id.menu_clear_height -> height.text.clear()
            R.id.menu_copy_weight -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val copiedText = weight.text.toString()
                clipboard.setPrimaryClip(ClipData.newPlainText("Copied Text", copiedText))
                Toast.makeText(this, "Weight copied to clipboard", Toast.LENGTH_SHORT).show()
            }
            R.id.menu_copy_height -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val copiedText = height.text.toString()
                clipboard.setPrimaryClip(ClipData.newPlainText("Copied Text", copiedText))
                Toast.makeText(this, "Height copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }
}