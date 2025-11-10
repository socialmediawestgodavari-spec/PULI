package com.example.puli

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PinActivity : AppCompatActivity() {

    private lateinit var edtPin: EditText

    companion object {
        private const val APP_PIN = "1964"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        edtPin = findViewById(R.id.edtPin)

        // Auto-validate when 4 digits are entered
        edtPin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val pin = s?.toString() ?: ""
                if (pin.length == 4) {
                    if (pin == APP_PIN) {
                        startActivity(Intent(this@PinActivity, HomeActivity::class.java))
                        
                        finish()
                    } else {
                        Toast.makeText(this@PinActivity, "Invalid PIN", Toast.LENGTH_SHORT).show()
                        edtPin.setText("")
                    }
                }
            }
        })
    }
}
