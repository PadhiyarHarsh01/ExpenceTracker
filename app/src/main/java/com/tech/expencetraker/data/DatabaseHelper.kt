package com.tech.expencetraker.data

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tech.expencetraker.model.Transaction

class DatabaseHelper {
    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("transactions")

    // Add transaction to Firebase
    fun addTransaction(transaction: Transaction, onComplete: (Boolean, String?) -> Unit) {
        val key = database.push().key ?: return
        transaction.id = key
        database.child(key).setValue(transaction)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.message) }
    }

    // Fetch transactions from Firebase
    fun getTransactions(onDataReceived: (List<Transaction>) -> Unit) {
        database.get().addOnSuccessListener { snapshot ->
            val transactions = snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
            onDataReceived(transactions)
        }.addOnFailureListener {
            onDataReceived(emptyList())
        }
    }
}
