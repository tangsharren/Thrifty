package my.edu.tarc.thrifty.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.databinding.AllOrderItemBinding
import my.edu.tarc.thrifty.model.AllOrderModel
//For recycler view in all order fragment
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
        holder.binding.orderId.text = "Order ID: "+list[position].orderId
        holder.binding.productPrice.text = "RM" + list[position].price
        holder.binding.productCarbon.text = list[position].carbon +"kg carbon saved"
        holder.binding.orderTime.text = "Time: "+list[position].orderTime
        holder.binding.orderDate.text = "Date: "+list[position].orderDate

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
                holder.binding.btnCancelOrder.isVisible = false
            }
        }
        holder.binding.btnCancelOrder.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Cancel Order")
            builder.setMessage("Are you sure you want to cancel this order?")
            builder.setPositiveButton("Yes") { _, _ ->
                holder.binding.btnCancelOrder.visibility = GONE
                updateStatus("Canceled",list[position].orderId!!)
            }
            builder.setNegativeButton("No", null)
            val dialog = builder.create()
            dialog.show()
        }
    }
    fun updateStatus(str:String , doc:String){
        val data = hashMapOf<String, Any>()
        data["status"] = str
        Firebase.firestore.collection("allOrders")
            .document(doc).update(data).addOnSuccessListener {
                Toast.makeText(context,"Order canceled",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context,it.message,Toast.LENGTH_SHORT).show()
            }
    }
    override fun getItemCount(): Int {
        return list.size
    }
}