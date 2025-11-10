package com.example.puli

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.puli.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*
 
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var date1: Date? = null
        var date2: Date? = null

        binding.btnDate1.setOnClickListener {
            showDatePicker { selectedDate ->
                binding.txtDate1.text = dateFormat.format(selectedDate)
                date1 = selectedDate
            }
        }

        binding.btnDate2.setOnClickListener {
            showDatePicker { selectedDate ->
                binding.txtDate2.text = dateFormat.format(selectedDate)
                date2 = selectedDate
            }
        }

        binding.btnCalculate.setOnClickListener {
            val amountStr = binding.edtAmount.text.toString().trim()
            val rateStr = binding.edtRate.text.toString().trim()

            if (amountStr.isEmpty() || rateStr.isEmpty()) {
                binding.txtResult.text = "⚠️ Please enter amount and rate."
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            val roi = rateStr.toDoubleOrNull()

            if (amount == null || amount <= 0) {
                binding.txtResult.text = "⚠️ Amount must be a positive number."
                return@setOnClickListener
            }

            if (roi == null || roi < 0) {
                binding.txtResult.text = "⚠️ Rate cannot be negative."
                return@setOnClickListener
            }

            if (date1 == null || date2 == null) {
                binding.txtResult.text = "⚠️ Please select both dates."
                return@setOnClickListener
            }

            val diffMillis = date2!!.time - date1!!.time
            if (diffMillis < 0) {
                binding.txtResult.text = "⚠️ End date must be on or after start date."
                return@setOnClickListener
            }

            // ✅ INCLUSIVE DURATION: Add 1 day so both start and end are counted
            val daysBetween = (diffMillis / (24 * 60 * 60 * 1000)).toInt() + 1

            // Simple Interest: I = (P × R × T) / (100 × 30)
            // R = ₹ per ₹100 per month → monthly rate = R / 100
            // T in months = days / 30 → so I = P × (R/100) × (days/30) = (P × R × days) / (100 × 30)
            val interest = (amount * roi * daysBetween) / (100 * 30)
            val total = amount + interest

            // Convert days to years, months, days (approximate)
            val years = daysBetween / 365
            val remainingAfterYears = daysBetween % 365
            val months = remainingAfterYears / 30
            val days = remainingAfterYears % 30

            // Update UI
            binding.txtPrincipal.text = "₹${"%.2f".format(amount)}"
            binding.txtInterest.text = "₹${"%.2f".format(interest)}"
            binding.txtTotal.text = "₹${"%.2f".format(total)}"
            binding.txtDuration.text = "${years}y ${months}m ${days}d"
            binding.txtResult.text = "✓ Calculated for $daysBetween day(s)"
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selected = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onDateSelected(selected.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
