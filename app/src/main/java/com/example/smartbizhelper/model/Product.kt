package com.example.smartbizhelper.model

import com.google.firebase.firestore.DocumentId

data class Product(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val stock: Int = 0
)
