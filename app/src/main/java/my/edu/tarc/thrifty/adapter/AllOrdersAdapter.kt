package my.edu.tarc.thrifty.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.databinding.AllOrderItemBinding
import my.edu.tarc.thrifty.model.AllOrderModel

class AllOrdersAdapter(val list : ArrayList<AllOrderModel> , val context : Context)
    : RecyclerView.Adapter<AllOrdersAdapter.AllOrderViewHolder>(){

    inner class AllOrderViewHolder(val binding:AllOrderItemBinding)
        :RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllOrderViewHolder {
        return AllOrderViewHolder(
            AllOrderItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }

    override fun onBindViewHolder(holder: AllOrderViewHolder, position: Int) {
        holder.binding.productTitle.text = list[position].name
        holder.binding.productPrice.text = list[position].price

        when(list[position].status){
            "Ordered" -> {
                holder.binding.productStatus.text = "Ordered"
            }

            "Dispatched" -> {
                holder.binding.productStatus.text = "Dispatched"
            }
            "Delivered" -> {
                holder.binding.productStatus.text = "Delivered"
            }
            "Canceled" -> {
                holder.binding.productStatus.text = "Canceled"
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}