package com.tech.expencetraker.ui.transactions

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference
        userId = auth.currentUser?.uid

        // Initialize Views
        tvAmount = findViewById(R.id.tvAmount)
        tvCategory = findViewById(R.id.tvCategory)
        tvDate = findViewById(R.id.tvDate)
        tvDescription = findViewById(R.id.tvDescription)
        btnEditTransaction = findViewById(R.id.btnEditTransaction)
        btnDeleteTransaction = findViewById(R.id.btnDeleteTransaction)
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)

        // Get Transaction ID from Intent
        transactionId = intent.getStringExtra("transactionId")

        if (transactionId.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Transaction ID or User ID is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Fetch Transaction Details from Firebase
        fetchTransactionDetails()

        // Edit Button Click
        btnEditTransaction.setOnClickListener {
            val intent = Intent(this, EditTransactionActivity::class.java).apply {
                putExtra("transactionId", transactionId)
            }
            startActivity(intent)
        }

        // Delete Button Click
        btnDeleteTransaction.setOnClickListener {
            deleteTransaction()
        }

        // Back Button Click
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun fetchTransactionDetails() {
        progressBar.visibility = View.VISIBLE

        val transactionRef = dbRef.child("users").child(auth.currentUser!!.uid).child("transactions").child(transactionId!!)

        transactionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressBar.visibility = View.GONE
                if (snapshot.exists()) {
                    // Fetch values safely and convert them correctly
                    val amount = snapshot.child("amount").getValue(Double::class.java)?.toString() ?: "0.00"
                    val category = snapshot.child("category").getValue(String::class.java) ?: "N/A"
                    val timestamp = snapshot.child("timestamp").getValue(Any::class.java)?.toString() ?: "N/A"
                    val description = snapshot.child("description").getValue(String::class.java) ?: "No description"

                    // Set values to TextViews
                    tvAmount.text = "Amount: ₹$amount"
                    tvCategory.text = "Category: $category"
                    tvDate.text = "Date: $timestamp"
                    tvDescription.text = "Description: $description"
                } else {
                    Toast.makeText(this@TransactionDetailsActivity, "Transaction not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@TransactionDetailsActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun deleteTransaction() {
        if (userId.isNullOrEmpty() || transactionId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: User ID or Transaction ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnDeleteTransaction.isEnabled = false

        val transactionRef = dbRef.child("users").child(userId!!).child("transactions").child(transactionId!!)

        transactionRef.removeValue()
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Transaction deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnDeleteTransaction.isEnabled = true
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
