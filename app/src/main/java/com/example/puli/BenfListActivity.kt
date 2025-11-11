package com.example.puli

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.HttpURLConnection
import java.net.URL

class BenfListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_benf_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 🔁 REPLACE WITH YOUR ACTUAL XML URL
        fetchAndParseXml("https://drive.google.com/file/d/1zfNXAWzqTUFUSDTeEDiJic7bkh6CnOG7/view?usp=sharing") { list ->
            recyclerView.adapter = BenfAdapter(this, list)
        }
    }

    private fun fetchAndParseXml(urlString: String, onSuccess: (List<BenfDetails>) -> Unit) {
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

                        val list = mutableListOf<BenfDetails>()
                        var current: BenfDetails? = null
                        var tagName = ""

                        var eventType = parser.eventType
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            when (eventType) {
                               
                                XmlPullParser.START_TAG -> {
                                    val localName = parser.name // This gives "BenfDetails", even with namespace
                                    tagName = localName
                                    if (localName == "BenfDetails") {
                                        current = BenfDetails(0, "", 0, "", "", "")
                                    }
                                }
                                XmlPullParser.TEXT -> {
                                    val text = parser.text.trim()
                                    if (text.isNotEmpty()) {
                                        when (tagName) {
                                            "RID" -> current?.rid = text.toInt()
                                            "name" -> current?.name = text
                                            "amount" -> current?.amount = text.toLong()
                                            "date" -> current?.date = text
                                            "IRate" -> current?.iRate = text
                                            "Remarks" -> current?.remarks = text
                                        }
                                    }
                                }
                                XmlPullParser.END_TAG -> {
                                    if (parser.name == "BenfDetails" && current != null) {
                                        list.add(current!!)
                                        current = null
                                    }
                                }
                            }
                            eventType = parser.next()
                        }

                        runOnUiThread {
                            onSuccess(list)
                        }
                    } else {
                        showError("HTTP ${responseCode}")
                    }
                }
            } catch (e: Exception) {
                showError(e.message ?: "Failed to load data")
            }
        }.start()
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
        }
    }
}
