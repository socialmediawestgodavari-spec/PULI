package com.example.puli

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.HttpURLConnection
import java.net.URL

class PinActivity : AppCompatActivity() {

    private lateinit var edtPin: EditText
    private var dynamicPin: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        edtPin = findViewById(R.id.edtPin)

       
        fetchAndParsePinXml("https://raw.githubusercontent.com/socialmediawestgodavari-spec/puli-data/refs/heads/main/akey") { pin ->
            dynamicPin = pin
        }

        edtPin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val pin = s?.toString() ?: ""
                if (pin.length == 4) {
                    if (pin == dynamicPin) {
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

    private fun fetchAndParsePinXml(urlString: String, onSuccess: (String) -> Unit) {
    Thread {
        try {
            val url = URL(urlString)
            (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000

                if (responseCode == 200) {
                    val factory = XmlPullParserFactory.newInstance()
                    factory.isNamespaceAware = true
                    val parser = factory.newPullParser()
                    parser.setInput(inputStream, null)

                    var p = ""; var u = ""; var l = ""; var i_val = ""
                    var currentTag = ""
                    var insideKeys = false

                    var eventType = parser.eventType
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        when (eventType) {
                            XmlPullParser.START_TAG -> {
                                val tagName = parser.name
                                if (tagName == "Keys") {
                                    insideKeys = true
                                } else if (insideKeys && listOf("p", "u", "l", "i").contains(tagName)) {
                                    currentTag = tagName
                                }
                            }
                            XmlPullParser.TEXT -> {
                                if (insideKeys && currentTag.isNotEmpty()) {
                                    val text = parser.text.trim()
                                    when (currentTag) {
                                        "p" -> p = text
                                        "u" -> u = text
                                        "l" -> l = text
                                        "i" -> i_val = text
                                    }
                                    currentTag = ""
                                }
                            }
                            XmlPullParser.END_TAG -> {
                                if (parser.name == "Keys") {
                                    insideKeys = false
                                }
                            }
                        }
                        eventType = parser.next()
                    }

                   
                    val pin = buildString {
                        append(if (p.length >= 1) p[0] else '0')
                        append(if (u.length >= 2) u[1] else '0')
                        append(if (l.length >= 3) l[2] else '0')
                        append(if (i_val.length >= 4) i_val[3] else '0')
                    }

                    runOnUiThread { onSuccess(pin) }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@PinActivity, "HTTP ${responseCode}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this@PinActivity, "Parse error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }.start()
}
}                  
