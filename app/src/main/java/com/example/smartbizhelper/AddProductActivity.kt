package com.example.smartbizhelper

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartbizhelper.databinding.ActivityAddProductBinding
import com.example.smartbizhelper.model.Product
import com.google.firebase.firestore.FirebaseFirestore

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        binding.saveProductButton.setOnClickListener {
            saveProduct()
        }
    }

    private fun saveProduct() {
        val productName = binding.productNameInput.text.toString()
        val stock = binding.stockInput.text.toString().toIntOrNull()
        val price = binding.priceInput.text.toString().toDoubleOrNull()
        val category = binding.categoryInput.text.toString()

        if (productName.isNotEmpty() && stock != null && price != null && category.isNotEmpty()) {
            val product = Product(
                name = productName,
                stock = stock,
                price = price,
                category = category
            )

            db.collection("inventory")
                .add(product)
                .addOnSuccessListener { 
                    Toast.makeText(this, "Product saved", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
        }
    }
}
