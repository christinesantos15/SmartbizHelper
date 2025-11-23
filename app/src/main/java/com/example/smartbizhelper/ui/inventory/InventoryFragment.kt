package com.example.smartbizhelper.ui.inventory

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartbizhelper.AddProductActivity
import com.example.smartbizhelper.databinding.FragmentInventoryBinding
import com.example.smartbizhelper.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var productAdapter: ProductAdapter
    private var firestoreListener: ListenerRegistration? = null
    private val fullProductList = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupSearchView()

        binding.addProductButton.setOnClickListener {
            val intent = Intent(activity, AddProductActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setupFirestoreListener()
    }

    override fun onPause() {
        super.onPause()
        firestoreListener?.remove()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(emptyList())
        binding.inventoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts(newText)
                return true
            }
        })
    }

    private fun setupFirestoreListener() {
        val productsRef = db.collection("inventory")

        firestoreListener = productsRef.addSnapshotListener { snapshots, e ->
            if (e != null || snapshots == null || _binding == null) {
                return@addSnapshotListener
            }

            fullProductList.clear()
            val products = snapshots.toObjects(Product::class.java)
            fullProductList.addAll(products)
            productAdapter.updateProducts(fullProductList)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
