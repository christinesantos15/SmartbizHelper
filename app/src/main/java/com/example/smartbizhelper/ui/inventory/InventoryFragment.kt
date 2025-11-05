package com.example.smartbizhelper.ui.inventory

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbizhelper.AddProductActivity
import com.example.smartbizhelper.R
import com.example.smartbizhelper.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InventoryFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<Product>()
    private val fullProductList = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inventory, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val recyclerView: RecyclerView = view.findViewById(R.id.inventory_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        productAdapter = ProductAdapter(productList)
        recyclerView.adapter = productAdapter

        val addProductButton: Button = view.findViewById(R.id.add_product_button)
        addProductButton.setOnClickListener {
            val intent = Intent(activity, AddProductActivity::class.java)
            startActivity(intent)
        }

        val searchView: SearchView = view.findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts(newText)
                return true
            }
        })

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchProducts()
    }

    private fun fetchProducts() {
        val userId = auth.currentUser?.uid ?: return
        val productsRef = db.collection("users").document(userId).collection("products")

        productsRef.get().addOnSuccessListener { result ->
            if (result.isEmpty) {
                // Add sample data
                val sampleProducts = listOf(
                    Product("Handmade Necklaces", 25.00, 10, ""),
                    Product("Vintage T-shirt", 45.50, 5, ""),
                    Product("Organic Coffee Beans", 15.00, 20, ""),
                    Product("Scented Candles", 12.00, 15, "")
                )
                for (product in sampleProducts) {
                    productsRef.add(product)
                }
            }
        }

        productsRef.get()
            .addOnSuccessListener { documents ->
                fullProductList.clear()
                for (document in documents) {
                    val product = document.toObject(Product::class.java)
                    fullProductList.add(product)
                }
                productList.clear()
                productList.addAll(fullProductList)
                productAdapter.notifyDataSetChanged()
            }
    }

    private fun filterProducts(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            fullProductList
        } else {
            fullProductList.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        productAdapter.updateProducts(filteredList)
    }
}
