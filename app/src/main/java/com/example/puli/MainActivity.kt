package com.example.puli

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

        setupInputFilters()
        setupDatePickers()

        binding.btnCalculate.setOnClickListener {
            calculateInterest()
        }
    }

    private fun setupInputFilters() {
        // Principal: max 7 digits, integers only
        binding.edtAmount.filters = arrayOf(android.text.InputFilter.LengthFilter(7))

        // ROI: custom decimal filter (max 2 before, 2 after)
        binding.edtRate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.isEmpty() || input == ".") return

                val parts = input.split(".")
                val before = parts[0]
                val after = if (parts.size > 1) parts[1] else ""

                var corrected = input

                // Max 2 digits before decimal
                if (before.length > 2) {
                    corrected = before.take(2)
                    if (after.isNotEmpty()) corrected += "." + after.take(2)
                }
                // Max 2 digits after decimal
                else if (after.length > 2) {
                    corrected = before + "." + after.take(2)
                }

                // Prevent leading zeros (e.g., "00.5" → "0.5")
                if (before.length > 1 && before.startsWith("0")) {
                    corrected = before.drop(1)
                    if (after.isNotEmpty()) corrected += "." + after.take(2)
                }

                if (corrected != input) {
                    binding.edtRate.removeTextChangedListener(this)
                    binding.edtRate.setText(corrected)
                    binding.edtRate.setSelection(corrected.length)
                    binding.edtRate.addTextChangedListener(this)
                }
            }
        })
    }

    private fun setupDatePickers() {
        binding.edtDate1.setOnClickListener {
            showDatePicker { date -> 
                binding.edtDate1.setText(dateFormat.format(date)) 
            }
        }
        binding.edtDate2.setOnClickListener {
            showDatePicker { date -> 
                binding.edtDate2.setText(dateFormat.format(date)) 
            }
        }
    }

    private fun calculateInterest() {
        val amountStr = binding.edtAmount.text.toString().trim()
        val rateStr = binding.edtRate.text.toString().trim()

        if (amountStr.isEmpty() || rateStr.isEmpty()) {
            binding.txtResult.text = "⚠️ Enter amount and rate"
            return
        }

        val amount = amountStr.toDoubleOrNull()
        val roi = rateStr.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            binding.txtResult.text = "⚠️ Amount must be > 0"
            return
        }
        if (roi == null || roi < 0) {
            binding.txtResult.text = "⚠️ Rate cannot be negative"
            return
        }

        val date1Str = binding.edtDate1.text.toString()
        val date2Str = binding.edtDate2.text.toString()

        if (date1Str.isEmpty() || date2Str.isEmpty()) {
            binding.txtResult.text = "⚠️ Select both dates"
            return
        }

        val date1 = try { dateFormat.parse(date1Str) } catch (e: Exception) { null }
        val date2 = try { dateFormat.parse(date2Str) } catch (e: Exception) { null }

        if (date1 == null || date2 == null) {
            binding.txtResult.text = "⚠️ Invalid date"
            return
        }

        if (date2.before(date1)) {
            binding.txtResult.text = "⚠️ End date before start"
            return
        }

        // ✅ INCLUSIVE: +1 day
        val daysBetween = (date2.time - date1.time) / (24 * 60 * 60 * 1000) + 1

        val interest = (amount * roi * daysBetween) / (100 * 30)
        val total = amount + interest

        // Format with Indian comma style
        binding.txtPrincipal.text = formatIndianNumber(amount)
        binding.txtInterest.text = formatIndianNumber(interest)
        binding.txtTotal.text = formatIndianNumber(total)

        // Duration: approximate
        val years = daysBetween / 365
        val rem = (daysBetween % 365).toInt()
        val months = rem / 30
        val days = rem % 30
        binding.txtDuration.text = "${years}y ${months}m ${days}d"

        binding.txtResult.text = "✓ Calculated for $daysBetween day(s)"
    }

    private fun showDatePicker(onSelected: (Date) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, y, m, d ->
                val selected = Calendar.getInstance().apply {
                    set(y, m, d)
                    set(Calendar.HOUR, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onSelected(selected.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun formatIndianNumber(value: Double): String {
        // Use Indian locale formatting (works on API 24+)
        return try {
            val formatter = android.icu.text.DecimalFormat("#,##,##0.00")
            formatter.format(value)
        } catch (e: Throwable) {
            // Fallback for older Android versions
            String.format("%.2f", value)
        }
    }
}
