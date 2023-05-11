package my.edu.tarc.thrifty.adapter

import android.content.Context
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.databinding.OrderStatusItemBinding
import my.edu.tarc.thrifty.fragment.AllOrderFragmentDirections
import my.edu.tarc.thrifty.fragment.OrderStatusFragmentDirections
import my.edu.tarc.thrifty.model.AllOrderModel


class OrderStatusAdapter(val list : ArrayList<AllOrderModel>, val context : Context)
    : RecyclerView.Adapter< OrderStatusAdapter.OrderStatusViewHolder>() {

    inner class OrderStatusViewHolder(val binding: OrderStatusItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderStatusViewHolder {
        val binding = OrderStatusItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return OrderStatusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderStatusViewHolder, position: Int) {
        val data = list[position]
        holder.binding.productTitle.text = data.name
        holder.binding.orderId.text = context.getString(R.string.orderID)+data.orderId
        holder.binding.productStatus.text = data.status
        holder.binding.productPrice.text = context.getString(R.string.rm) + data.price
        holder.binding.tvUserOrder.text = context.getString(R.string.buyer)+data.userId
        holder.binding.orderDate.text = context.getString(R.string.date)+data.orderDate
        holder.binding.orderTime.text = context.getString(R.string.time)+data.orderTime

        holder.binding.cancelButton.setOnClickListener {
            holder.binding.proceedButton.visibility = View.GONE
            updateStatus("Canceled", list[position].orderId!!,list[position].productId!!)
        }
        when (list[position].status) {
            "Ordered" -> {
                holder.binding.proceedButton.text = "Dispatched"
            }

            "Dispatched" -> {
                if (holder.binding.proceedButton.text != "Already Delivered")
                    holder.binding.proceedButton.text = "Delivered"
            }
            "Delivered" -> {
                holder.binding.cancelButton.visibility = View.GONE
                holder.binding.proceedButton.isEnabled = false
                holder.binding.proceedButton.text = "Already Delivered"

            }
            "Canceled" -> {
                holder.binding.proceedButton.visibility = View.GONE
                holder.binding.cancelButton.isEnabled = false
            }

        }
        holder.binding.proceedButton.setOnClickListener {
            when (holder.binding.proceedButton.text) {
                "Dispatched" -> {
                    updateStatus("Dispatched", list[position].orderId!!,list[position].productId!!)
                }
                "Delivered" -> {
                    updateStatus("Delivered", list[position].orderId!!,list[position].productId!!)
                }
            }
        }
        holder.itemView.setOnClickListener {
            //go to edit listing page
            val action =OrderStatusFragmentDirections.actionOrderStatusFragmentToEditListingFragment(list[position].productId!!)
            Navigation.findNavController(holder.itemView).navigate(action)
        }
    }

    fun updateStatus(str: String, doc: String,productId:String) {
        val data = hashMapOf<String, Any>()
        data["status"] = str
        Firebase.firestore.collection("allOrders")
            .document(doc).update(data).addOnSuccessListener {
                Toast.makeText(context, "Status Updated", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        //update product availability
        val availability = hashMapOf<String, Any>()
        availability["availability"] = "Available"
        if(str == "Canceled"){
            Firebase.firestore.collection("products")
                .document(productId).update(availability).addOnSuccessListener {
                    Toast.makeText(context, "Product is available now", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}