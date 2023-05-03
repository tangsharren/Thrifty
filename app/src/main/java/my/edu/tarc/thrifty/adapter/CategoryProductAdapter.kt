package my.edu.tarc.thrifty.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import my.edu.tarc.thrifty.databinding.ItemCategoryProductLayoutBinding
import my.edu.tarc.thrifty.fragment.CategoryFragmentDirections
import my.edu.tarc.thrifty.model.AddProductModel
//For the product recycler view in category fragment
class CategoryProductAdapter (val context: Context, val list:ArrayList<AddProductModel>)
    :RecyclerView.Adapter<CategoryProductAdapter.CategoryProductViewHolder>(){

    inner class CategoryProductViewHolder(val binding: ItemCategoryProductLayoutBinding)
        :RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryProductAdapter.CategoryProductViewHolder {
        val binding = ItemCategoryProductLayoutBinding.inflate(LayoutInflater.from(context),parent,false)
        return CategoryProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryProductAdapter.CategoryProductViewHolder, position: Int) {
        Glide.with(context).load(list[position].productCoverImg).into(holder.binding.imageView3)
        holder.binding.tvProductNama.text = list[position].productName
        holder.binding.tvProductPrice.text = "RM"+list[position].productSp
        holder.binding.tvCarbonFoot.text = list[position].carbon+"kg"

        holder.itemView.setOnClickListener{
//            val intent = Intent(context,ProductDetailsActivity::class.java)
//            intent.putExtra("id",list[position].productId)
//            context.startActivity(intent)
            val action = CategoryFragmentDirections.actionCategoryFragmentToProductDetailsFragment("",list[position].productId!!)
            Navigation.findNavController(holder.itemView).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}