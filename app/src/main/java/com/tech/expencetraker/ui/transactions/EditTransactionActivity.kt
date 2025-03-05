package com.tech.expencetraker.ui.transactions

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tech.expencetraker.R
import com.tech.expencetraker.utils.SimpleTextWatcher
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
    private lateinit var amountInputLayout: TextInputLayout
    private lateinit var categoryInputLayout: TextInputLayout
    private lateinit var dateInputLayout: TextInputLayout
    private lateinit var descriptionInputLayout: TextInputLayout

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

        amountInputLayout = findViewById(R.id.amountInputLayout)
        categoryInputLayout = findViewById(R.id.categoryInputLayout)
        dateInputLayout = findViewById(R.id.dateInputLayout)
        descriptionInputLayout = findViewById(R.id.descriptionInputLayout)

        // Get Transaction Data from Intent
        transactionId = intent.getStringExtra("transactionId")

        if (intent != null && transactionId != null) {
            etAmount.setText(intent.getDoubleExtra("amount", 0.0).toString())
            etCategory.setText(intent.getStringExtra("category") ?: "")
            etDate.setText(intent.getStringExtra("date") ?: "")
            etDescription.setText(intent.getStringExtra("description") ?: "")
        } else {
            Toast.makeText(this, "Error: Transaction data not found", Toast.LENGTH_SHORT).show()
            finish()
        }

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

        // Live validation to remove errors as user types
        setupValidationListeners()
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

        if (!validateInputs(amountText, category, date)) return

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            amountInputLayout.error = "Enter a valid amount"
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
            "timestamp" to System.currentTimeMillis()
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

    private fun validateInputs(amount: String, category: String, date: String): Boolean {
        var isValid = true

        if (amount.isEmpty()) {
            amountInputLayout.error = "Amount is required"
            isValid = false
        } else {
            amountInputLayout.error = null
        }

        if (category.isEmpty()) {
            categoryInputLayout.error = "Category is required"
            isValid = false
        } else {
            categoryInputLayout.error = null
        }

        if (date.isEmpty()) {
            dateInputLayout.error = "Date is required"
            isValid = false
        } else {
            dateInputLayout.error = null
        }

        return isValid
    }

    private fun setupValidationListeners() {
        etAmount.addTextChangedListener(SimpleTextWatcher { amountInputLayout.error = null })
        etCategory.addTextChangedListener(SimpleTextWatcher { categoryInputLayout.error = null })
        etDate.addTextChangedListener(SimpleTextWatcher { dateInputLayout.error = null })
    }
}
