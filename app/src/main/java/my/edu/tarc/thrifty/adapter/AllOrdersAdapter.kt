package my.edu.tarc.thrifty.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.databinding.AllOrderItemBinding
import my.edu.tarc.thrifty.fragment.AllOrderFragmentDirections
import my.edu.tarc.thrifty.fragment.ListingFragmentDirections
import my.edu.tarc.thrifty.model.AddProductModel
import my.edu.tarc.thrifty.model.AllOrderModel
//For recycler view in all order fragment
class AllOrdersAdapter(var list : ArrayList<AllOrderModel> , val context : Context)
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
        holder.binding.orderId.text = context.getString(R.string.orderID)+list[position].orderId
        holder.binding.productPrice.text = context.getString(R.string.rm) + list[position].price
        holder.binding.productCarbon.text = list[position].carbon +"kg carbon saved"
        holder.binding.orderTime.text = context.getString(R.string.time)+list[position].orderTime
        holder.binding.orderDate.text = context.getString(R.string.date)+list[position].orderDate

        var totalCarbonSaved=0.0
        for(item in list){
            if(item.status != "Canceled"){
                totalCarbonSaved += item.carbon!!.toDouble()
            }
        }
        when(list[position].status){
            context.getString(R.string.ordered) -> {
                holder.binding.productStatus.text = context.getString(R.string.ordered)
            }
            context.getString(R.string.dispatch) -> {
                holder.binding.productStatus.text =  context.getString(R.string.dispatch)
            }
            context.getString(R.string.deliver) -> {
                holder.binding.productStatus.text =  context.getString(R.string.deliver)
                holder.binding.btnCancelOrder.isVisible = false
            }
            context.getString(R.string.cancelStatus) -> {
                holder.binding.productStatus.text = context.getString(R.string.cancelStatus)
                holder.binding.btnCancelOrder.isVisible = false
            }
        }
        holder.binding.btnCancelOrder.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.cancelOrder))
            builder.setMessage(context.getString(R.string.cfmCancelOrder))
            builder.setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                holder.binding.btnCancelOrder.visibility = GONE
                updateStatus(context.getString(R.string.cancelStatus),list[position].orderId!!,list[position].productId!!)
            }
            builder.setNegativeButton(context.getString(R.string.no), null)
            val dialog = builder.create()
            dialog.show()
        }
        holder.itemView.setOnClickListener {
            //go to the product details page
            val action = AllOrderFragmentDirections.actionAllOrderFragmentToProductDetailsFragment("",list[position].productId!!)
            Navigation.findNavController(holder.itemView).navigate(action)

        }
    }
    fun updateStatus(str:String , doc:String,productId:String){
        val data = hashMapOf<String, Any>()
        data["status"] = str
        Firebase.firestore.collection("allOrders")
            .document(doc).update(data).addOnSuccessListener {
                Toast.makeText(context,context.getString(R.string.canceled),Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context,it.message,Toast.LENGTH_SHORT).show()
            }
        //update product availability
        val availability = hashMapOf<String, Any>()
        availability["availability"] = "Available"

        Firebase.firestore.collection("products")
            .document(productId).update(availability).addOnSuccessListener {
                Toast.makeText(context, "Product is available now", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }

    }
    override fun getItemCount(): Int {
        return list.size
    }
    fun searchDataList(searchList: ArrayList<AllOrderModel>) {
        list = searchList
        notifyDataSetChanged()
    }
}