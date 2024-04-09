package com.zybooks.bmicalculator

import android.annotation.SuppressLint
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

class MainActivity : AppCompatActivity() {
    private lateinit var weight: EditText
    private lateinit var height: EditText
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

        imageBoy.setOnClickListener {
            imageBoy.setImageResource(R.drawable.ic_boy)
            imageGirl.setImageResource(R.drawable.ic_girl_blur)
        }

        imageGirl.setOnClickListener {
            imageBoy.setImageResource(R.drawable.ic_boy_blur)
            imageGirl.setImageResource(R.drawable.ic_girl)
        }

        calculateButton.setOnClickListener {
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