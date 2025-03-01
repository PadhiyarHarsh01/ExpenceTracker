package com.tech.expencetraker.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.tech.expencetraker.R
import com.tech.expencetraker.model.TransactionModel
import com.tech.expencetraker.ui.profile.ProfileActivity
import com.tech.expencetraker.ui.settings.SettingsActivity
import com.tech.expencetraker.ui.transactions.AddTransactionActivity
import com.tech.expencetraker.ui.transactions.TransactionDetailsActivity

class HomeActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var tvBalance: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var rvTransactions: RecyclerView
    private lateinit var fabAddTransaction: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var bottomNavigationView: BottomNavigationView

    // Firebase
    private lateinit var transactionsAdapter: TransactionsAdapter
    private lateinit var transactionsList: ArrayList<TransactionModel>
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize UI components
        tvBalance = findViewById(R.id.tvBalance)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpense = findViewById(R.id.tvExpense)
        rvTransactions = findViewById(R.id.rvTransactions)
        fabAddTransaction = findViewById(R.id.fabAddTransaction)
        progressBar = findViewById(R.id.progressBar)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // RecyclerView Setup
        rvTransactions.layoutManager = LinearLayoutManager(this)
        transactionsList = arrayListOf()
        transactionsAdapter = TransactionsAdapter(transactionsList) { transaction ->
            val intent = Intent(this, TransactionDetailsActivity::class.java).apply {
                putExtra("transactionId", transaction.id)
                putExtra("amount", transaction.amount)
                putExtra("category", transaction.category)
                putExtra("date", transaction.date)
                putExtra("description", transaction.description)
            }
            startActivity(intent)
        }
        rvTransactions.adapter = transactionsAdapter

        // Firebase Authentication
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Firebase Database Reference
        database = FirebaseDatabase.getInstance().getReference("users").child(userId)

        // Load Data
        loadUserData()
        loadTransactions()

        // Add Transaction Button
        fabAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        // Bottom Navigation
        bottomNavigationView.selectedItemId = R.id.nav_home
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> return@setOnItemSelectedListener true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    return@setOnItemSelectedListener true
                }
            }
            false
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
                transactionsList.clear()
                for (data in snapshot.children) {
                    val transaction = data.getValue(TransactionModel::class.java)
                    transaction?.let { transactionsList.add(it) }
                }
                transactionsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Failed to load transactions!", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
