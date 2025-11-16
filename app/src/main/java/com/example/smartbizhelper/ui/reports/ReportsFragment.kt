package com.example.smartbizhelper.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartbizhelper.R
import com.example.smartbizhelper.model.Transaction
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReportsFragment : Fragment() {

    private lateinit var salesBarChart: BarChart
    private lateinit var productsPieChart: PieChart
    private lateinit var keyInsightsText: TextView
    private lateinit var generateReportButton: Button
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reports, container, false)

        firestore = FirebaseFirestore.getInstance()

        salesBarChart = view.findViewById(R.id.sales_bar_chart)
        productsPieChart = view.findViewById(R.id.products_pie_chart)
        keyInsightsText = view.findViewById(R.id.key_insights_text)
        generateReportButton = view.findViewById(R.id.generate_report_button)

        generateReportButton.setOnClickListener {
            Toast.makeText(context, "Generating report...", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchTransactions()
    }

    private fun fetchTransactions() {
        val transactionsRef = firestore.collection("transactions")

        transactionsRef.get().addOnSuccessListener { result ->
            if (result.isEmpty) {
                // Add sample data
                val sampleTransactions = listOf(
                    Transaction("Handmade Necklaces", "Jewelry", 25.00, Date(), "sale"),
                    Transaction("Vintage T-shirt", "Apparel", 45.50, Date(), "sale"),
                    Transaction("Organic Coffee Beans", "Food & Beverage", 15.00, Date(), "sale"),
                    Transaction("Scented Candles", "Home Goods", 12.00, Date(), "sale"),
                    Transaction("Custom Keychains", "Accessories", 8.50, Date(), "sale"),
                    Transaction("Leather Wallets", "Accessories", 60.00, Date(), "sale"),
                    Transaction("Artisan Soaps", "Beauty", 7.00, Date(), "sale"),
                    Transaction("Hand-poured Soy Wax Melts", "Home Goods", 5.00, Date(), "sale"),
                    Transaction("Beaded Bracelets", "Jewelry", 18.00, Date(), "sale"),
                    Transaction("Raw Materials", "Materials", 50.00, Date(), "expense"),
                    Transaction("Shipping Supplies", "Logistics", 20.00, Date(), "expense"),
                    Transaction("Marketing", "Business", 30.00, Date(), "expense")
                )
                val batch = firestore.batch()
                for (transaction in sampleTransactions) {
                    val docRef = transactionsRef.document()
                    batch.set(docRef, transaction)
                }
                batch.commit().addOnSuccessListener {
                    fetchAllTransactions(transactionsRef)
                }
            } else {
                fetchAllTransactions(transactionsRef)
            }
        }
    }

    private fun fetchAllTransactions(transactionsRef: com.google.firebase.firestore.CollectionReference) {
        transactionsRef.get().addOnSuccessListener { documents ->
            val transactions = documents.toObjects(Transaction::class.java)
            setupCharts(transactions)
        }
    }

    private fun setupCharts(transactions: List<Transaction>) {
        val salesTransactions = transactions.filter { it.type == "sale" }
        val expenseTransactions = transactions.filter { it.type == "expense" }

        if (salesTransactions.isNotEmpty()) {
            setupWeeklySalesBarChart(salesTransactions)
            setupTopProductsPieChart(salesTransactions)
            displayInsights(salesTransactions, expenseTransactions)
        } else {
            // Clear or hide charts if there are no sales
            salesBarChart.clear()
            productsPieChart.clear()
            keyInsightsText.text = "No sales data available to generate insights."
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
        salesBarChart.data = BarData(dataSet)
        salesBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(days)
        salesBarChart.description.isEnabled = false
        salesBarChart.invalidate()
    }

    private fun setupTopProductsPieChart(sales: List<Transaction>) {
        val productSales = sales.groupBy { it.productName }
            .mapValues { it.value.sumOf { sale -> sale.amount } }

        val entries = productSales.map { PieEntry(it.value.toFloat(), it.key) }
        val dataSet = PieDataSet(entries, "Top Products")
        dataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
        dataSet.sliceSpace = 3f
        productsPieChart.data = PieData(dataSet)
        productsPieChart.description.isEnabled = false
        productsPieChart.centerText = "Top Products"
        productsPieChart.invalidate()
    }

    private fun displayInsights(sales: List<Transaction>, expenses: List<Transaction>) {
        val totalSales = sales.sumOf { it.amount }
        val totalExpenses = expenses.sumOf { it.amount }
        val netProfit = totalSales - totalExpenses

        val topProduct = sales.groupBy { it.productName }
            .mapValues { it.value.sumOf { sale -> sale.amount } }
            .maxByOrNull { it.value }

        var insights = "Total Sales: $${String.format("%.2f", totalSales)}\n"
        insights += "Total Expenses: $${String.format("%.2f", totalExpenses)}\n"
        insights += "Net Profit: $${String.format("%.2f", netProfit)}\n"

        if (topProduct != null) {
            val percentage = (topProduct.value / totalSales) * 100
            insights += "Your top-selling product is ${topProduct.key}, contributing to ${String.format("%.1f", percentage)}%% of your total sales."
        }

        keyInsightsText.text = insights
    }
}
