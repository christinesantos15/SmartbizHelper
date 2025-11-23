package com.example.smartbizhelper.ui.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbizhelper.R
import com.example.smartbizhelper.model.Product
import java.util.Locale

class ProductAdapter(private var products: List<Product>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inventory_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productPrice: TextView = itemView.findViewById(R.id.product_price)
        private val productStock: TextView = itemView.findViewById(R.id.product_stock)
        private val productImage: ImageView = itemView.findViewById(R.id.product_image)

        fun bind(product: Product) {
            productName.text = product.name
            productPrice.text = String.format(Locale.US, "₱%.2f", product.price)
            productStock.text = String.format(Locale.US, "Stock: %d", product.stock)
            // You can load a real image here using a library like Glide or Picasso
            // For now, we\'ll just use the placeholder
            productImage.setImageResource(R.drawable.ic_product)
        }
    }
}
