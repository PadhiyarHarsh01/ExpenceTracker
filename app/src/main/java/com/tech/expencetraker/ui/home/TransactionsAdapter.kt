package com.tech.expencetraker.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tech.expencetraker.R
import com.tech.expencetraker.model.TransactionModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionsAdapter(
    private var transactions: ArrayList<TransactionModel>,
    private val onItemClick: (TransactionModel) -> Unit
) : RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)

        fun bind(transaction: TransactionModel) {
            // Format amount as currency (ensure it's never null)
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")) // Indian format
            tvAmount.text = currencyFormat.format(transaction.amount)

            // Set category and description
            tvCategory.text = transaction.category ?: "Unknown"
            tvDescription.text = transaction.description ?: "No description"

            // Convert timestamp to readable date
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(transaction.timestamp))
            tvDate.text = formattedDate

            // Handle item click event
            itemView.setOnClickListener { onItemClick(transaction) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    // Function to update the list dynamically
    fun updateList(newTransactions: List<TransactionModel>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }
}
