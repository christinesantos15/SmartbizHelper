package com.example.smartbizhelper.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartbizhelper.AddTransactionActivity
import com.example.smartbizhelper.databinding.FragmentDashboardBinding
import com.example.smartbizhelper.model.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var productAdapter: DashboardProductAdapter // Corrected adapter
    private var firestoreListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        setupRecyclerViews()

        binding.addTransactionButton.setOnClickListener {
            val intent = Intent(activity, AddTransactionActivity::class.java)
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

    private fun setupRecyclerViews() {
        transactionAdapter = TransactionAdapter(emptyList())
        binding.recentTransactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
            isNestedScrollingEnabled = false
        }

        productAdapter = DashboardProductAdapter(emptyList()) // Corrected adapter
        binding.recentProductsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = productAdapter
        }
    }

    private fun setupFirestoreListener() {
        val transactionsRef = db.collection("transactions").orderBy("date", Query.Direction.DESCENDING)

        firestoreListener = transactionsRef.addSnapshotListener { snapshots, e ->
            if (e != null || snapshots == null || _binding == null) {
                return@addSnapshotListener
            }

            var totalIncome = 0.0
            var totalExpense = 0.0

            val allTransactions = snapshots.toObjects(Transaction::class.java)

            for (transaction in allTransactions) {
                if (transaction.type == "income") {
                    totalIncome += transaction.amount
                } else if (transaction.type == "expense") {
                    totalExpense += transaction.amount
                }
            }

            val recentTransactions = allTransactions.take(5)
            transactionAdapter.updateTransactions(recentTransactions)

            val recentProducts = allTransactions.distinctBy { it.productName }.take(5)
            productAdapter.updateProducts(recentProducts) // Corrected adapter

            binding.incomeValue.text = String.format(Locale.US, "₱%.2f", totalIncome)
            binding.expenseValue.text = String.format(Locale.US, "₱%.2f", totalExpense)
            binding.netProfitValue.text = String.format(Locale.US, "₱%.2f", totalIncome - totalExpense)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
