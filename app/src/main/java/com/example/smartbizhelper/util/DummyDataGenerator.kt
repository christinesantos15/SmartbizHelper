package com.example.smartbizhelper.util

import com.example.smartbizhelper.model.Product
import com.example.smartbizhelper.model.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.concurrent.TimeUnit

object DummyDataGenerator {

    fun generateDummyData() {
        val firestore = FirebaseFirestore.getInstance()

        // Dummy Products
        val products = listOf(
            Product(name = "Laptop", price = 1200.0, stock = 50),
            Product(name = "Smartphone", price = 800.0, stock = 150),
            Product(name = "Keyboard", price = 100.0, stock = 200),
            Product(name = "Mouse", price = 50.0, stock = 300),
            Product(name = "Monitor", price = 300.0, stock = 100)
        )

        products.forEach { product ->
            firestore.collection("inventory").add(product)
        }

        // Dummy Transactions
        val transactions = mutableListOf<Transaction>()
        val now = Date()

        // Daily Sales and Expenses
        for (i in 0..5) {
            val date = Date(now.time - TimeUnit.DAYS.toMillis(i.toLong()))
            transactions.add(Transaction(productName = "Laptop", category = "Electronics", amount = 1200.0, date = date, type = "sale"))
            transactions.add(Transaction(productName = "Office Supplies", category = "Supplies", amount = 150.0, date = date, type = "expense"))
        }

        // Weekly Sales and Expenses
        for (i in 0..3) {
            val date = Date(now.time - TimeUnit.DAYS.toMillis(i.toLong() * 7))
            transactions.add(Transaction(productName = "Smartphone", category = "Electronics", amount = 800.0, date = date, type = "sale"))
            transactions.add(Transaction(productName = "Software License", category = "Software", amount = 200.0, date = date, type = "expense"))
        }

        // Monthly Sales and Expenses
        for (i in 0..2) {
            val date = Date(now.time - TimeUnit.DAYS.toMillis((i * 30).toLong()))
            transactions.add(Transaction(productName = "Monitor", category = "Electronics", amount = 300.0, date = date, type = "sale"))
            transactions.add(Transaction(productName = "Cloud Services", category = "Services", amount = 500.0, date = date, type = "expense"))
        }

        transactions.forEach { transaction ->
            firestore.collection("transactions").add(transaction)
        }
    }
}
