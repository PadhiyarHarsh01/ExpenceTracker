package com.tech.expencetraker.model

data class TransactionModel(
    var id: String = "",
    var amount: Double = 0.0,
    var category: String = "",
    var date: String = "",
    var description: String = ""
)
