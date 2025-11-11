package com.example.puli

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BenfDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_benf_detail)

        val item = intent.getParcelableExtra<BenfDetails>("item")!!

        // Header
        findViewById<TextView>(R.id.txtName).text = item.name

        // Amount (with comma)
        val formattedAmount = try {
            val formatter = android.icu.text.DecimalFormat("#,##,##0")
            "₹${formatter.format(item.amount)}"
        } catch (e: Throwable) {
            "₹${item.amount}"
        }
        findViewById<TextView>(R.id.txtAmount).text = formattedAmount

        // Date
        findViewById<TextView>(R.id.txtDate).text = item.date

        // Rate
        findViewById<TextView>(R.id.txtIRate).text = "${item.iRate}%"

        // Calculated Interest (to today)
        val calculatedInterest = calculateInterestToToday(item)
        findViewById<TextView>(R.id.txtCalculatedInterest).text = "₹$calculatedInterest"

        // Remarks (if any — currently empty, but leave for future)
        findViewById<TextView>(R.id.txtRemarks).text = item.remarks.ifEmpty { "No remarks" }
    }

    private fun calculateInterestToToday(item: BenfDetails): String {
        return try {
            val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
            val startDate = sdf.parse(item.date) ?: return "0"
            val today = java.util.Date()

            if (today.before(startDate)) {
                "0 (future date)"
            } else {
                val diffMillis = today.time - startDate.time
                val days = (diffMillis / (24 * 60 * 60 * 1000)).toInt() + 1
                val roi = item.iRate.toDoubleOrNull() ?: 0.0
                val interest = (item.amount.toDouble() * roi * days) / (100.0 * 30.0)
                if (interest.isFinite()) {
                    val rounded = Math.round(interest).toLong()
                    val formatter = android.icu.text.DecimalFormat("#,##,##0")
                    formatter.format(rounded)
                } else {
                    "0"
                }
            }
        } catch (e: Exception) {
            "Error"
        }
    }
}
