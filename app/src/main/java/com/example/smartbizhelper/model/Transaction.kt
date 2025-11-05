package com.example.smartbizhelper.model

import java.util.Date

data class Transaction(
    val productName: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val date: Date = Date(),
    val type: String = ""
)
