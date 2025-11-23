package com.example.smartbizhelper.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartbizhelper.databinding.FragmentReportsBinding
import com.example.smartbizhelper.model.Transaction
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private var firestoreListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()

        binding.generateReportButton.setOnClickListener {
            Toast.makeText(context, "Generating report...", Toast.LENGTH_SHORT).show()
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

    private fun setupFirestoreListener() {
        val transactionsRef = firestore.collection("transactions")

        firestoreListener = transactionsRef.addSnapshotListener { snapshots, e ->
            if (e != null || snapshots == null || _binding == null) {
                return@addSnapshotListener
            }

            val transactions = snapshots.toObjects(Transaction::class.java)
            setupCharts(transactions)
        }
    }

    private fun setupCharts(transactions: List<Transaction>) {
        val salesTransactions = transactions.filter { it.type == "income" } // Corrected to 'income'
        val expenseTransactions = transactions.filter { it.type == "expense" }

        if (salesTransactions.isNotEmpty()) {
            setupWeeklySalesBarChart(salesTransactions)
            setupTopProductsPieChart(salesTransactions)
            displayInsights(salesTransactions, expenseTransactions)
        } else {
            binding.salesBarChart.clear()
            binding.productsPieChart.clear()
            binding.keyInsightsText.text = "No sales data available to generate insights."
        }
    }

    private fun setupWeeklySalesBarChart(sales: List<Transaction>) {
        val calendar = Calendar.getInstance()
        val weeklySales = mutableMapOf<String, Float>()
        val days = (0..6).map { 
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -it)
            SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
        }.reversed()

        days.forEach { weeklySales[it] = 0f }

        for (sale in sales) {
            val day = SimpleDateFormat("EEE", Locale.getDefault()).format(sale.date)
            if (weeklySales.containsKey(day)) {
                weeklySales[day] = weeklySales.getValue(day) + sale.amount.toFloat()
            }
        }

        val entries = days.mapIndexed { index, day -> BarEntry(index.toFloat(), weeklySales[day] ?: 0f) }
        val dataSet = BarDataSet(entries, "Weekly Sales Volume")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        binding.salesBarChart.data = BarData(dataSet)
        binding.salesBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(days)
        binding.salesBarChart.description.isEnabled = false
        binding.salesBarChart.invalidate()
    }

    private fun setupTopProductsPieChart(sales: List<Transaction>) {
        val productSales = sales.groupBy { it.productName }
            .mapValues { it.value.sumOf { sale -> sale.amount } }

        val entries = productSales.map { PieEntry(it.value.toFloat(), it.key) }
        val dataSet = PieDataSet(entries, "Top Products")
        dataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
        dataSet.sliceSpace = 3f
        binding.productsPieChart.data = PieData(dataSet)
        binding.productsPieChart.description.isEnabled = false
        binding.productsPieChart.centerText = "Top Products"
        binding.productsPieChart.invalidate()
    }

    private fun displayInsights(sales: List<Transaction>, expenses: List<Transaction>) {
        val totalSales = sales.sumOf { it.amount }
        val totalExpenses = expenses.sumOf { it.amount }
        val netProfit = totalSales - totalExpenses

        val topProduct = sales.groupBy { it.productName }
            .mapValues { it.value.sumOf { sale -> sale.amount } }
            .maxByOrNull { it.value }

        var insights = "Total Sales: ₱${String.format("%.2f", totalSales)}\n"
        insights += "Total Expenses: ₱${String.format("%.2f", totalExpenses)}\n"
        insights += "Net Profit: ₱${String.format("%.2f", netProfit)}\n"

        if (topProduct != null && totalSales > 0) { // Added totalSales > 0 check
            val percentage = (topProduct.value / totalSales) * 100
            insights += "Your top-selling product is ${topProduct.key}, contributing to ${String.format("%.1f", percentage)}%% of your total sales."
        }

        binding.keyInsightsText.text = insights
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
