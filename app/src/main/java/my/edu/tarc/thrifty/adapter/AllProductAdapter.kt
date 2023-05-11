package my.edu.tarc.thrifty.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.databinding.LayoutProductItemBinding
import my.edu.tarc.thrifty.fragment.AllProductFragmentDirections

import my.edu.tarc.thrifty.model.AddProductModel
//Used by allProductFragment's product recycler

class AllProductAdapter (val context: Context, var list:ArrayList<AddProductModel>)
    :RecyclerView.Adapter<AllProductAdapter.ProductViewHolder>(){

        inner class ProductViewHolder(val binding:LayoutProductItemBinding)
            :RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = LayoutProductItemBinding.inflate(LayoutInflater.from(context),parent ,false)
        return  ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        var preferences: SharedPreferences
        preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        val currentUser = preferences.getString("email", "")!!
        val data = list[position]

        Glide.with(context).load(data.productCoverImg).into(holder.binding.imageView2)
        holder.binding.tvProName.text = data.productName
        holder.binding.tvCat.text = data.productCategory
        holder.binding.tvCarbonCount.text = data.carbon + context.getString(R.string.kg)
        holder.binding.btnPrice.text = "RM" + data.productSp

        holder.itemView.setOnClickListener {
            if(list[position].userEmail == currentUser){
                Toast.makeText(context,"This is your own listing", Toast.LENGTH_SHORT).show()
                val action = AllProductFragmentDirections.actionAllProductFragmentToEditListingFragment(list[position].productId!!)
                findNavController(holder.itemView).navigate(action)
            }
            else{
                val action =
                    AllProductFragmentDirections.actionAllProductFragmentToProductDetailsFragment(
                        data.productCategory!!,
                        list[position].productId!!
                    )
                findNavController(holder.itemView).navigate(action)
            }

        }

        holder.binding.btnPrice.setOnClickListener {
            if(list[position].userEmail == currentUser){
                Toast.makeText(context,"This is your own listing", Toast.LENGTH_SHORT).show()
                val action = AllProductFragmentDirections.actionAllProductFragmentToEditListingFragment(list[position].productId!!)
                findNavController(holder.itemView).navigate(action)
            }
            else {
                val action =
                    AllProductFragmentDirections.actionAllProductFragmentToProductDetailsFragment(
                        data.productCategory!!,
                        list[position].productId!!
                    )
                findNavController(holder.itemView).navigate(action)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    fun searchDataList(searchList: ArrayList<AddProductModel>) {
        list = searchList
        notifyDataSetChanged()
    }

}