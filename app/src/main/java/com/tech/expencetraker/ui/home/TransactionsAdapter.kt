package com.tech.expencetraker.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tech.expencetraker.R
import com.tech.expencetraker.model.TransactionModel

class TransactionsAdapter(
    private var transactions: ArrayList<TransactionModel>,
    private val onItemClick: (TransactionModel) -> Unit
) : RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(transaction: TransactionModel) {
            tvAmount.text = String.format("$%.2f", transaction.amount) // Proper currency format
            tvCategory.text = transaction.category
            tvDate.text = transaction.date

            itemView.setOnClickListener { onItemClick(transaction) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
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
