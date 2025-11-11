package com.example.puli

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BenfAdapter(
    private val context: Context,
    private val items: List<BenfDetails>
) : RecyclerView.Adapter<BenfAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.txtName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_benf, parent, false)
        return ViewHolder(view)
    }

override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val item = items[position]
    
    // Line 1: Name
    holder.txtName.text = item.name
    
    // Line 2: Amount | Date
    val formattedAmount = try {
        val formatter = android.icu.text.DecimalFormat("#,##,##0")
        "₹${formatter.format(item.amount)}"
    } catch (e: Throwable) {
        "₹${item.amount}"
    }
    holder.txtAmountDate.text = "$formattedAmount | ${item.date}"
    
    // Line 3: Calculate interest from record date to TODAY
    val interestText = try {
        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
        val startDate = sdf.parse(item.date) ?: return@try "Interest: N/A"
        val today = java.util.Date()
        
        if (today.before(startDate)) {
            "Interest: ₹0.00 (future date)"
        } else {
            val diffMillis = today.time - startDate.time
            val days = (diffMillis / (24 * 60 * 60 * 1000)).toInt() + 1 // inclusive
            
            // IRate = ₹ per ₹100 per month → monthly rate = IRate / 100
            // Monthly interest = amount * (IRate / 100)
            // Daily interest = monthly / 30
            // Total interest = daily * days
            val roi = item.iRate.toDoubleOrNull() ?: 0.0
            val interest = (item.amount * roi * days) / (100 * 30)
            
            "Interest: ₹${String.format("%.2f", interest)}"
        }
    } catch (e: Exception) {
        "Interest: Error"
    }
    
    holder.txtInterest.text = interestText

    holder.itemView.setOnClickListener {
        val intent = Intent(context, BenfDetailActivity::class.java).apply {
            putExtra("item", item)
        }
        context.startActivity(intent)
    }
}

    override fun getItemCount() = items.size
}
