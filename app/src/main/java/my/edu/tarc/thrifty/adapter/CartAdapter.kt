package my.edu.tarc.thrifty.adapter

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.databinding.LayoutCartItemBinding
import my.edu.tarc.thrifty.fragment.CartFragmentDirections
import my.edu.tarc.thrifty.roomdb.AppDatabase
import my.edu.tarc.thrifty.roomdb.ProductModel

class CartAdapter(val context: Context, val list: List<ProductModel>) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
    inner class CartViewHolder(val binding: LayoutCartItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = LayoutCartItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        Glide.with(context).load(list[position].productImage).into(holder.binding.imageView4)

        holder.binding.tvProNames.text = list[position].productName
        holder.binding.tvProPrice.text = context.getString(R.string.rm) + list[position].productSp
        holder.binding.tvProCarbons.text = list[position].carbon + context.getString(R.string.carbonKg)

        holder.itemView.setOnClickListener{
            val action = CartFragmentDirections.actionCartFragmentToProductDetailsFragment("",list[position].productId)
            Navigation.findNavController(holder.itemView).navigate(action)
        }

        var loadingDialog: Dialog
        loadingDialog = Dialog(context)
        loadingDialog.setContentView(R.layout.progress_layout)
        loadingDialog.setCancelable(false)

        val dao = AppDatabase.getInstance(context).productDao()
        holder.binding.imageView5.setOnClickListener {
            loadingDialog.show()
            GlobalScope.launch(Dispatchers.IO) {
                dao.deleteProduct(
                    ProductModel(
                        list[position].productId,
                        list[position].productName,
                        list[position].productImage,
                        list[position].productSp,
                        list[position].carbon
                    )
                )
                loadingDialog.dismiss()
            }
        }
    }
    override fun getItemCount(): Int {
        return list.size
    }
}