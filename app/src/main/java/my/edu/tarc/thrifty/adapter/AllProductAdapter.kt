package my.edu.tarc.thrifty.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.Filter
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.databinding.LayoutProductItemBinding
import my.edu.tarc.thrifty.fragment.AllProductFragmentDirections
import my.edu.tarc.thrifty.fragment.HomeFragmentDirections
import my.edu.tarc.thrifty.model.AddProductModel
//Used by allProductFragment's product recycler

class AllProductAdapter (val context: Context, val list:ArrayList<AddProductModel>)
    :RecyclerView.Adapter<AllProductAdapter.ProductViewHolder>(){
        inner class ProductViewHolder(val binding:LayoutProductItemBinding)
            :RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = LayoutProductItemBinding.inflate(LayoutInflater.from(context),parent ,false)
        return  ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val data = list[position]

        Glide.with(context).load(data.productCoverImg).into(holder.binding.imageView2)
        holder.binding.tvProName.text = data.productName
        holder.binding.tvCat.text = data.productCategory
        holder.binding.tvCarbonCount.text = data.carbon + "kg"
        holder.binding.btnPrice.text = "RM" + data.productSp

        holder.itemView.setOnClickListener {
            val action =
                AllProductFragmentDirections.actionAllProductFragmentToProductDetailsFragment(
                    data.productCategory!!,
                    list[position].productId!!
                )
            findNavController(holder.itemView).navigate(action)
        }

        holder.binding.btnPrice.setOnClickListener {
            val action =
                AllProductFragmentDirections.actionAllProductFragmentToProductDetailsFragment(
                    data.productCategory!!,
                    list[position].productId!!
                )
            findNavController(holder.itemView).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    // A method that returns a filter object for the adapter

}