package com.tech.expencetraker.model

data class TransactionModel(
    var transactionId: String? = null,
    var amount: Double = 0.0,
    var category: String? = null,
    var date: String? = null,
    var description: String? = null,
    var timestamp: Long = System.currentTimeMillis()
)
