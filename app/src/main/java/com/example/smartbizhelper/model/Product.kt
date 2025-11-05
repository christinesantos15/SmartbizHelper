package com.example.smartbizhelper.model

data class Product(
    val name: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val category: String = "",
    val imageUrl: String = ""
)
