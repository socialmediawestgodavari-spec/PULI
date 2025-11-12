
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

class DebitListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debit_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

      
        val debitXmlUrl = "https://raw.githubusercontent.com/socialmediawestgodavari-spec/puli-data/refs/heads/main/DebitsDetails.xml"
        fetchAndParseXml(debitXmlUrl) { list ->
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
                                    tagName = parser.name
                                    if (tagName == "BenfDetails") {
                                        current = BenfDetails(0, "", 0, "", "", "")
                                    }
                                }
                                XmlPullParser.TEXT -> {
                                    val text = parser.text.trim()
                                    if (text.isNotEmpty() && current != null) {
                                        when (tagName) {
                                            "RID" -> current.rid = text.toInt()
                                            "name" -> current.name = text
                                            "amount" -> current.amount = text.toLong()
                                            "date" -> current.date = text
                                            "IRate" -> current.iRate = text
                                            "Remarks" -> current.remarks = text // now included
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
                            if (list.isEmpty()) {
                                Toast.makeText(this@DebitListActivity, "No debit data found", Toast.LENGTH_LONG).show()
                            }
                            onSuccess(list)
                        }
                    } else {
                        showError("HTTP ${responseCode}")
                    }
                }
            } catch (e: Exception) {
                showError("Load failed: ${e.message}")
            }
        }.start()
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
        }
    }
}
