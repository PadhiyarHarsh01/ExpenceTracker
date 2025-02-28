package com.tech.expencetraker.model

data class Transaction(
    var id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
