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

    // Format amount
    val formattedAmount = try {
        val formatter = android.icu.text.DecimalFormat("#,##,##0")
        "₹${formatter.format(item.amount)}"
    } catch (e: Throwable) {
        "₹${item.amount}"
    }
    findViewById<TextView>(R.id.txtAmount).text = formattedAmount
    findViewById<TextView>(R.id.txtDate).text = item.date
    findViewById<TextView>(R.id.txtIRate).text = "${item.iRate}%"

    // Today's date
    val today = java.util.Date()
    val todayFormatted = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US).format(today)
    findViewById<TextView>(R.id.txtInterestHeader).text = "INTEREST TO DATE (as of $todayFormatted)"

    // Calculate interest & duration
    val (interestValue, durationText) = calculateInterestAndDuration(item, today)

    // Display interest
    val interestFormatted = try {
        val formatter = android.icu.text.DecimalFormat("#,##,##0")
        "₹${formatter.format(interestValue)}"
    } catch (e: Throwable) {
        "₹$interestValue"
    }
    findViewById<TextView>(R.id.txtCalculatedInterest).text = interestFormatted

    // Total = Principal + Interest
    val totalValue = item.amount + interestValue
    val totalFormatted = try {
        val formatter = android.icu.text.DecimalFormat("#,##,##0")
        "₹${formatter.format(totalValue)}"
    } catch (e: Throwable) {
        "₹$totalValue"
    }
    findViewById<TextView>(R.id.txtTotalAmount).text = totalFormatted

    // Duration
    findViewById<TextView>(R.id.txtDuration).text = durationText

    // Remarks — now fetched from XML!
    findViewById<TextView>(R.id.txtRemarks).text = item.remarks.ifEmpty { "No remarks" }
}

private fun calculateInterestAndDuration(
    item: BenfDetails,
    today: java.util.Date
): Pair<Long, String> {
    return try {
        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
        val startDate = sdf.parse(item.date) ?: throw Exception("Invalid date")

        if (today.before(startDate)) {
            return Pair(0L, "0 years, 0 months, 0 days")
        }

        val diffMillis = today.time - startDate.time
        val totalDays = (diffMillis / (24 * 60 * 60 * 1000)).toInt() + 1 // inclusive

        // Convert to years, months, days (approx)
        val years = totalDays / 365
        val remainingAfterYears = totalDays % 365
        val months = remainingAfterYears / 30
        val days = remainingAfterYears % 30

        val durationText = "$years years, $months months, $days days"

        val roi = item.iRate.toDoubleOrNull() ?: 0.0
        val interest = (item.amount.toDouble() * roi * totalDays) / (100.0 * 30.0)
        val interestRounded = if (interest.isFinite()) Math.round(interest).toLong() else 0L

        Pair(interestRounded, durationText)
    } catch (e: Exception) {
        Pair(0L, "Error")
    }
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
