package com.example.smartbizhelper

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartbizhelper.databinding.ActivityAddTransactionBinding
import com.example.smartbizhelper.model.Product
import com.example.smartbizhelper.model.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Set current date as default
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.dateInput.setText(sdf.format(Date()))

        binding.saveTransactionButton.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val productName = binding.itemNameInput.text.toString()
        val dateStr = binding.dateInput.text.toString()
        val category = binding.categoryInput.text.toString()
        val amount = binding.amountInput.text.toString().toDoubleOrNull()
        val transactionType = if (binding.transactionTypeGroup.checkedRadioButtonId == R.id.income_radio) "income" else "expense"

        if (productName.isNotEmpty() && dateStr.isNotEmpty() && category.isNotEmpty() && amount != null) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = try {
                sdf.parse(dateStr)
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid date format. Please use dd/MM/yyyy", Toast.LENGTH_SHORT).show()
                return
            }

            val transaction = Transaction(
                productName = productName,
                date = date,
                category = category,
                amount = amount,
                type = transactionType
            )

            db.collection("transactions") // Corrected collection
                .add(transaction)
                .addOnSuccessListener { 
                    Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show()
                    if (binding.newProductCheckbox.isChecked) {
                        val product = Product(
                            name = productName,
                            category = category,
                            price = amount,
                            stock = 1
                        )
                        db.collection("inventory").add(product) // Corrected collection
                    }
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
