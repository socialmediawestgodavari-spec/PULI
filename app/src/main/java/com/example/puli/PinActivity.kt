package com.example.puli

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PinActivity : AppCompatActivity() {

    private lateinit var edtPin: EditText
    private lateinit var btnSubmit: Button

    // 🔒 Hardcoded PIN - cannot be changed by user
    private const val APP_PIN = "1964"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        edtPin = findViewById(R.id.edtPin)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val enteredPin = edtPin.text.toString()
            if (enteredPin == APP_PIN) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid PIN", Toast.LENGTH_SHORT).show()
                edtPin.setText("")
            }
        }
    }
}
