package com.example.smartbizhelper.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbizhelper.AddTransactionActivity
import com.example.smartbizhelper.R
import com.example.smartbizhelper.model.Transaction
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

class DashboardFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recentTransactionsRecyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()
    private lateinit var fragmentView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.fragment_dashboard, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recentTransactionsRecyclerView = fragmentView.findViewById(R.id.recent_transactions_recycler_view)
        recentTransactionsRecyclerView.layoutManager = LinearLayoutManager(context)
        transactionAdapter = TransactionAdapter(transactions)
        recentTransactionsRecyclerView.adapter = transactionAdapter

        val addTransactionFab: FloatingActionButton = fragmentView.findViewById(R.id.add_transaction_fab)
        addTransactionFab.setOnClickListener {
            val intent = Intent(activity, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        return fragmentView
    }

    override fun onResume() {
        super.onResume()
        fetchDashboardData(fragmentView)
    }

    private fun fetchDashboardData(view: View) {
        val userId = auth.currentUser?.uid ?: return

        val incomeValue = view.findViewById<TextView>(R.id.income_value)
        val expenseValue = view.findViewById<TextView>(R.id.expense_value)
        val netProfitValue = view.findViewById<TextView>(R.id.net_profit_value)
        val transactionsRef = db.collection("users").document(userId).collection("transactions")

        transactionsRef.orderBy("date", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { documents ->
                var totalIncome = 0.0
                var totalExpense = 0.0
                transactions.clear()
                val allTransactions = documents.toObjects(Transaction::class.java)

                for (transaction in allTransactions) {
                    if (transaction.type == "income") {
                        totalIncome += transaction.amount
                    } else if (transaction.type == "expense") {
                        totalExpense += transaction.amount
                    }
                }

                // Limit the list to the 5 most recent transactions for display
                transactions.addAll(allTransactions.take(5))
                transactionAdapter.notifyDataSetChanged()

                incomeValue.text = String.format("$%.2f", totalIncome)
                expenseValue.text = String.format("$%.2f", totalExpense)
                netProfitValue.text = String.format("$%.2f", totalIncome - totalExpense)
            }
    }
}
