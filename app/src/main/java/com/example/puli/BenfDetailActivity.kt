package com.example.puli
 
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BenfDetailActivity : AppCompatActivity() {

    private lateinit var edtProjectedDate: EditText
    private lateinit var btnCalculateProjected: Button
    private lateinit var txtProjectedInterest: TextView
    private lateinit var txtTotalProjected: TextView
    private lateinit var txtProjectedDuration: TextView

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

        // Remarks
        findViewById<TextView>(R.id.txtRemarks).text = item.remarks.ifEmpty { "No remarks" }

        // Initialize projected interest views
        edtProjectedDate = findViewById(R.id.edtProjectedDate)
        btnCalculateProjected = findViewById(R.id.btnCalculateProjected)
        txtProjectedInterest = findViewById(R.id.txtProjectedInterest)
        txtTotalProjected = findViewById(R.id.txtTotalProjected)
        txtProjectedDuration = findViewById(R.id.txtProjectedDuration)

        // Date picker for projected date
        edtProjectedDate.setOnClickListener {
            showDatePicker { selectedDate ->
                val formatted = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US).format(selectedDate)
                edtProjectedDate.setText(formatted)
            }
        }

        // Calculate projected interest
        btnCalculateProjected.setOnClickListener {
            val projectedDateStr = edtProjectedDate.text.toString()
            if (projectedDateStr.isEmpty()) {
                Toast.makeText(this, "Select a future date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
                val creditDate = sdf.parse(item.date)!!
                val projectedDate = sdf.parse(projectedDateStr)!!

                if (projectedDate.before(creditDate)) {
                    Toast.makeText(this, "Projected date must be after credit date", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val days = ((projectedDate.time - creditDate.time) / (24 * 60 * 60 * 1000)).toInt() + 1
                val roi = item.iRate.toDoubleOrNull() ?: 0.0
                val interest = (item.amount.toDouble() * roi * days) / (100.0 * 30.0)
                val interestRounded = if (interest.isFinite()) Math.round(interest).toLong() else 0L

                val interestFormatted = formatIndianNumber(interestRounded)
                val totalFormatted = formatIndianNumber(item.amount + interestRounded)

                val years = days / 365
                val rem = days % 365
                val months = rem / 30
                val d = rem % 30
                val durationText = "$years years, $months months, $d days"

                // Update UI
                txtProjectedInterest.text = "₹$interestFormatted"
                txtTotalProjected.text = "₹$totalFormatted"
                txtProjectedDuration.text = durationText

                // ✅ Clarity note
                val note = "Interest calculated from ${item.date} to $projectedDateStr"
                findViewById<TextView>(R.id.txtProjectionNote).text = note

            } catch (e: Exception) {
                Toast.makeText(this, "Invalid date", Toast.LENGTH_SHORT).show()
            }
        }
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
            val totalDays = (diffMillis / (24 * 60 * 60 * 1000)).toInt() + 1

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

    private fun formatIndianNumber(value: Long): String {
        return try {
            val formatter = android.icu.text.DecimalFormat("#,##,##0")
            formatter.format(value)
        } catch (e: Throwable) {
            value.toString()
        }
    }

    private fun showDatePicker(onSelected: (java.util.Date) -> Unit) {
        val cal = java.util.Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selected = java.util.Calendar.getInstance().apply {
                    set(year, month, day)
                }
                onSelected(selected.time)
            },
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }
}
