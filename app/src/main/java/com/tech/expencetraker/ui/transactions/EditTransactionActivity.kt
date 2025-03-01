package com.tech.expencetraker.ui.transactions

import android.annotation.SuppressLint
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

class EditTransactionActivity : AppCompatActivity() {

    private lateinit var etAmount: TextInputEditText
    private lateinit var etCategory: AutoCompleteTextView
    private lateinit var etDate: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var btnSaveChanges: MaterialButton
    private lateinit var btnBack: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private val calendar = Calendar.getInstance()

    private var transactionId: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference

        // Initialize Views
        etAmount = findViewById(R.id.etAmount)
        etCategory = findViewById(R.id.etCategory)
        etDate = findViewById(R.id.etDate)
        etDescription = findViewById(R.id.etDescription)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)

        // Get Transaction Data from Intent
        transactionId = intent.getStringExtra("transactionId")
        etAmount.setText(intent.getDoubleExtra("amount", 0.0).toString()) // Fixed amount retrieval
        etCategory.setText(intent.getStringExtra("category") ?: "")
        etDate.setText(intent.getStringExtra("date") ?: "")
        etDescription.setText(intent.getStringExtra("description") ?: "")

        // Set Date Picker
        etDate.setOnClickListener { showDatePickerDialog() }

        // Set predefined category suggestions
        val categories = arrayOf("Food", "Transport", "Shopping", "Bills", "Salary", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        etCategory.setAdapter(adapter)

        // Save Changes Button
        btnSaveChanges.setOnClickListener { updateTransaction() }

        // Back Button
        btnBack.setOnClickListener { finish() }
    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                etDate.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun updateTransaction() {
        val amountText = etAmount.text.toString().trim()
        val category = etCategory.text.toString().trim()
        val date = etDate.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (amountText.isEmpty() || category.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null || transactionId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: User or transaction not found", Toast.LENGTH_SHORT).show()
            return
        }

        val transactionUpdates = mapOf(
            "amount" to amount,
            "category" to category,
            "date" to date,
            "description" to description,
            "timestamp" to System.currentTimeMillis() // Update timestamp to reflect changes
        )

        progressBar.visibility = View.VISIBLE
        btnSaveChanges.isEnabled = false

        dbRef.child("transactions").child(userId).child(transactionId!!)
            .updateChildren(transactionUpdates)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                btnSaveChanges.isEnabled = true
                Toast.makeText(this, "Transaction updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnSaveChanges.isEnabled = true
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
