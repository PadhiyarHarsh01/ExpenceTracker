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
import java.util.*

class HomeActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var etIncomeInput: EditText
    private lateinit var btnSetIncome: Button
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var tvBalance: TextView
    private lateinit var rvTransactions: RecyclerView
    private lateinit var fabAddTransaction: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var bottomNavigationView: BottomNavigationView

    // Firebase
    private lateinit var transactionsAdapter: TransactionsAdapter
    private var transactionsList: ArrayList<TransactionModel> = arrayListOf()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize UI components
        etIncomeInput = findViewById(R.id.etIncomeInput)
        btnSetIncome = findViewById(R.id.btnSetIncome)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpense = findViewById(R.id.tvExpense)
        tvBalance = findViewById(R.id.tvBalance)
        rvTransactions = findViewById(R.id.rvTransactions)
        fabAddTransaction = findViewById(R.id.fabAddTransaction)
        progressBar = findViewById(R.id.progressBar)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // RecyclerView Setup
        rvTransactions.layoutManager = LinearLayoutManager(this)
        transactionsAdapter = TransactionsAdapter(transactionsList) { transaction ->
            val intent = Intent(this, TransactionDetailsActivity::class.java).apply {
                putExtra("transactionId", transaction.transactionId)
                putExtra("amount", transaction.amount)
                putExtra("category", transaction.category)
                putExtra("description", transaction.description)
                putExtra("timestamp", transaction.timestamp)
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

        // Load Transactions and Income
        loadTransactions()
        loadIncome()

        // Set Income Button Click
        btnSetIncome.setOnClickListener {
            val incomeText = etIncomeInput.text.toString()
            if (incomeText.isNotEmpty()) {
                val income = incomeText.toDoubleOrNull()
                if (income != null) {
                    setIncome(income)
                } else {
                    Toast.makeText(this, "Invalid income value!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Enter income amount!", Toast.LENGTH_SHORT).show()
            }
        }

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

    private fun loadTransactions() {
        progressBar.visibility = View.VISIBLE
        val userId = auth.currentUser?.uid ?: return
        val transactionRef = database.child("transactions")

        transactionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newTransactions = arrayListOf<TransactionModel>()
                var totalExpense = 0.0

                for (data in snapshot.children) {
                    val transaction = data.getValue(TransactionModel::class.java)
                    transaction?.let {
                        newTransactions.add(it)

                        // FIX: Count all positive transactions as expenses
                        if (it.amount != null && it.amount!! > 0) {
                            totalExpense += it.amount!!
                        }
                    }
                }

                // Sort transactions by timestamp (latest first)
                newTransactions.sortByDescending { it.timestamp }

                // Update UI
                transactionsAdapter.updateList(newTransactions)

                // Update expense and balance
                tvExpense.text = "₹%.2f".format(totalExpense)
                updateBalance()

                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Failed to load transactions!", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
        })
    }

    private fun loadIncome() {
        val userId = auth.currentUser?.uid ?: return
        database.child("income").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val income = snapshot.getValue(Double::class.java) ?: 0.0
                tvIncome.text = "₹%.2f".format(income)

                // Update balance after loading income
                updateBalance()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Failed to load income!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateBalance() {
        val incomeText = tvIncome.text.toString().replace("₹", "").trim()
        val expenseText = tvExpense.text.toString().replace("₹", "").trim()

        val income = incomeText.toDoubleOrNull() ?: 0.0
        val totalExpense = expenseText.toDoubleOrNull() ?: 0.0

        val balance = income - totalExpense
        tvBalance.text = "₹%.2f".format(balance)
    }

    private fun setIncome(income: Double) {
        val userId = auth.currentUser?.uid ?: return
        database.child("income").setValue(income).addOnSuccessListener {
            Toast.makeText(this, "Income set successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to set income!", Toast.LENGTH_SHORT).show()
        }
    }
}
