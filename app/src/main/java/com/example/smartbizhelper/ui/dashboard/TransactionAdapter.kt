package com.example.smartbizhelper.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbizhelper.R
import com.example.smartbizhelper.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(private val transactions: List<Transaction>) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int = transactions.size

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productCategory: TextView = itemView.findViewById(R.id.transaction_category)
        private val transactionAmount: TextView = itemView.findViewById(R.id.transaction_amount)
        private val transactionDate: TextView = itemView.findViewById(R.id.transaction_date)
        private val productImage: ImageView = itemView.findViewById(R.id.product_image)

        fun bind(transaction: Transaction) {
            productName.text = transaction.productName
            productCategory.text = transaction.category
            transactionAmount.text = String.format("$%.2f", transaction.amount)
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            transactionDate.text = sdf.format(transaction.date)
            // You can load a real image here using a library like Glide or Picasso
            productImage.setImageResource(R.drawable.ic_product)
        }
    }
}
