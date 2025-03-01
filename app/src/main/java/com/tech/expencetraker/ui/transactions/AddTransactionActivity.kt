package com.tech.expencetraker.ui.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tech.expencetraker.R
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var etAmount: TextInputEditText
    private lateinit var etCategory: AutoCompleteTextView
    private lateinit var etDate: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var btnSaveTransaction: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference

        // Initialize UI elements
        etAmount = findViewById(R.id.etAmount)
        etCategory = findViewById(R.id.etCategory)
        etDate = findViewById(R.id.etDate)
        etDescription = findViewById(R.id.etDescription)
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction)
        progressBar = findViewById(R.id.progressBar)

        // Set Date Picker
        etDate.setOnClickListener { showDatePickerDialog() }

        // Set predefined category suggestions
        val categories = arrayOf("Food", "Transport", "Shopping", "Bills", "Salary", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        etCategory.setAdapter(adapter)

        // Save transaction on button click
        btnSaveTransaction.setOnClickListener { saveTransaction() }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                etDate.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun saveTransaction() {
        val amountText = etAmount.text.toString().trim()
        val category = etCategory.text.toString().trim()
        val date = etDate.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (amountText.isEmpty() || category.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val transactionId = UUID.randomUUID().toString()  // Unique ID for transaction
        val timestamp = System.currentTimeMillis()  // Store timestamp for sorting

        val transaction = mapOf(
            "transactionId" to transactionId,
            "amount" to amount,  // Stored as Double
            "category" to category,
            "date" to date,
            "description" to description,
            "timestamp" to timestamp  // Ensuring transactions can be sorted by time
        )

        // Disable button and show progress while saving
        progressBar.visibility = View.VISIBLE
        btnSaveTransaction.isEnabled = false

        dbRef.child("users").child(userId).child("transactions").child(transactionId).setValue(transaction)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                btnSaveTransaction.isEnabled = true
                Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnSaveTransaction.isEnabled = true
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
