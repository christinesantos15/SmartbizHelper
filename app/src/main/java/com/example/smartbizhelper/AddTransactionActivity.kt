package com.example.smartbizhelper

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val itemNameInput = findViewById<EditText>(R.id.item_name_input)
        val dateInput = findViewById<EditText>(R.id.date_input)
        val categoryInput = findViewById<EditText>(R.id.category_input)
        val amountInput = findViewById<EditText>(R.id.amount_input)
        val transactionTypeGroup = findViewById<RadioGroup>(R.id.transaction_type_group)
        val newProductCheckbox = findViewById<CheckBox>(R.id.new_product_checkbox)
        val saveTransactionButton = findViewById<Button>(R.id.save_transaction_button)

        // Set current date as default
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateInput.setText(sdf.format(Date()))

        saveTransactionButton.setOnClickListener {
            val itemName = itemNameInput.text.toString()
            val dateStr = dateInput.text.toString()
            val category = categoryInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull()
            val transactionType = if (transactionTypeGroup.checkedRadioButtonId == R.id.income_radio) "income" else "expense"

            if (itemName.isNotEmpty() && dateStr.isNotEmpty() && category.isNotEmpty() && amount != null) {
                val userId = auth.currentUser?.uid ?: return@setOnClickListener

                val date = try {
                    sdf.parse(dateStr)
                } catch (e: Exception) {
                    Toast.makeText(this, "Invalid date format. Please use dd/MM/yyyy", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val transaction = hashMapOf(
                    "productName" to itemName, // Corrected field name
                    "date" to date, // Corrected data type
                    "category" to category,
                    "amount" to amount,
                    "type" to transactionType
                )

                db.collection("users").document(userId).collection("transactions")
                    .add(transaction)
                    .addOnSuccessListener { 
                        Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show()
                        if (newProductCheckbox.isChecked) {
                            val product = hashMapOf(
                                "name" to itemName,
                                "category" to category,
                                "price" to amount,
                                "stock" to 1 // Added default stock
                            )
                            db.collection("users").document(userId).collection("products").add(product)
                        }
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
