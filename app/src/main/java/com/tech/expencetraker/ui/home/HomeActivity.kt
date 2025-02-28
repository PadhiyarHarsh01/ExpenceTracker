package com.tech.expencetraker.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tech.expencetraker.R
import com.tech.expencetraker.model.TransactionModel
import com.tech.expencetraker.ui.transactions.AddTransactionActivity
import com.tech.expencetraker.ui.transactions.TransactionDetailsActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var tvBalance: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var rvTransactions: RecyclerView
    private lateinit var fabAddTransaction: FloatingActionButton
    private lateinit var btnLogout: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var transactionsAdapter: TransactionsAdapter
    private lateinit var transactionsList: ArrayList<TransactionModel>

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize views
        tvBalance = findViewById(R.id.tvBalance)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpense = findViewById(R.id.tvExpense)
        rvTransactions = findViewById(R.id.rvTransactions)
        fabAddTransaction = findViewById(R.id.fabAddTransaction)
        btnLogout = findViewById(R.id.btnLogout)
//        progressBar = findViewById(R.id.progressBar)

        // Set up RecyclerView
        rvTransactions.layoutManager = LinearLayoutManager(this)
        transactionsList = arrayListOf()
        transactionsAdapter = TransactionsAdapter(transactionsList) { transaction ->
            val intent = Intent(this, TransactionDetailsActivity::class.java)
            intent.putExtra("transactionId", transaction.id)
            intent.putExtra("amount", transaction.amount)
            intent.putExtra("category", transaction.category)
            intent.putExtra("date", transaction.date)
            intent.putExtra("description", transaction.description)
            startActivity(intent)
        }
        rvTransactions.adapter = transactionsAdapter

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database = FirebaseDatabase.getInstance().getReference("users").child(userId)

        // Load Data
        loadUserData()
        loadTransactions()

        // Add Transaction Button
        fabAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        // Logout Button
        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadUserData() {
        progressBar.visibility = View.VISIBLE
        database.child("balance").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val balance = snapshot.getValue(Double::class.java) ?: 0.0
                tvBalance.text = "₹%.2f".format(balance)
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Failed to load balance!", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
        })

        database.child("income").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val income = snapshot.getValue(Double::class.java) ?: 0.0
                tvIncome.text = "₹%.2f".format(income)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        database.child("expense").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val expense = snapshot.getValue(Double::class.java) ?: 0.0
                tvExpense.text = "₹%.2f".format(expense)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("users").child(userId).child("transactions")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newTransactions = ArrayList<TransactionModel>()
                for (data in snapshot.children) {
                    val transaction = data.getValue(TransactionModel::class.java)
                    transaction?.let { newTransactions.add(it) }
                }
                transactionsAdapter.updateList(newTransactions) // Use the new update method
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Failed to load transactions!", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
