package com.tech.expencetraker.ui.transactions

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tech.expencetraker.R

class TransactionDetailsActivity : AppCompatActivity() {

    private lateinit var tvAmount: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnEditTransaction: MaterialButton
    private lateinit var btnDeleteTransaction: MaterialButton
    private lateinit var btnBack: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    private var transactionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference

        // Initialize Views
        tvAmount = findViewById(R.id.tvAmount)
        tvCategory = findViewById(R.id.tvCategory)
        tvDate = findViewById(R.id.tvDate)
        tvDescription = findViewById(R.id.tvDescription)
        btnEditTransaction = findViewById(R.id.btnEditTransaction)
        btnDeleteTransaction = findViewById(R.id.btnDeleteTransaction)
        btnBack = findViewById(R.id.btnBack)
//        progressBar = findViewById(R.id.progressBar) // 🛠️ FIXED: Now initialized properly

        // Get Transaction Data from Intent
        transactionId = intent.getStringExtra("transactionId")
        val amount = intent.getStringExtra("amount") ?: "0.00"
        val category = intent.getStringExtra("category") ?: "N/A"
        val timestamp = intent.getStringExtra("timestamp") ?: "N/A" // 🛠️ FIXED: Renamed from "date"
        val description = intent.getStringExtra("description") ?: "No description"

        // Set Data to Views
        tvAmount.text = "Amount: ₹$amount"
        tvCategory.text = "Category: $category"
        tvDate.text = "Date: $timestamp" // 🛠️ FIXED: Using correct timestamp
        tvDescription.text = "Description: $description"

        // Edit Button Click
        btnEditTransaction.setOnClickListener {
            val intent = Intent(this, EditTransactionActivity::class.java).apply {
                putExtra("transactionId", transactionId)
                putExtra("amount", amount)
                putExtra("category", category)
                putExtra("timestamp", timestamp) // 🛠️ FIXED: Corrected key
                putExtra("description", description)
            }
            startActivity(intent)
        }

        // Delete Button Click
        btnDeleteTransaction.setOnClickListener {
            transactionId?.let { id -> deleteTransaction(id) }
        }

        // Back Button Click
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun deleteTransaction(transactionId: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnDeleteTransaction.isEnabled = false

        // 🛠️ FIXED: Corrected Firebase path
        dbRef.child("users").child(userId).child("transactions").child(transactionId)
            .removeValue()
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Transaction deleted successfully", Toast.LENGTH_SHORT).show()
                finish() // Close this activity after deletion
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnDeleteTransaction.isEnabled = true
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
