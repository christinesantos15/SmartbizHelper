package com.example.smartbizhelper

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddProductActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val productNameInput = findViewById<EditText>(R.id.product_name_input)
        val stockInput = findViewById<EditText>(R.id.stock_input)
        val priceInput = findViewById<EditText>(R.id.price_input)
        val categoryInput = findViewById<EditText>(R.id.category_input)
        val saveProductButton = findViewById<Button>(R.id.save_product_button)

        saveProductButton.setOnClickListener {
            val productName = productNameInput.text.toString()
            val stock = stockInput.text.toString().toIntOrNull()
            val price = priceInput.text.toString().toDoubleOrNull()
            val category = categoryInput.text.toString()

            if (productName.isNotEmpty() && stock != null && price != null && category.isNotEmpty()) {
                val userId = auth.currentUser?.uid ?: return@setOnClickListener

                val product = hashMapOf(
                    "name" to productName,
                    "stock" to stock,
                    "price" to price,
                    "category" to category
                )

                db.collection("users").document(userId).collection("products")
                    .add(product)
                    .addOnSuccessListener { 
                        Toast.makeText(this, "Product saved", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}