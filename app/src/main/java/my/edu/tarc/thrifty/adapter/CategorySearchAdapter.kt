package my.edu.tarc.thriftyadmin.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.InspectableProperty
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.databinding.LayoutCategoryItemBinding
import my.edu.tarc.thrifty.databinding.LayoutCategorySearchBinding
import my.edu.tarc.thrifty.fragment.HomeFragmentDirections
import my.edu.tarc.thrifty.model.AddProductModel
import my.edu.tarc.thrifty.model.CategoryModel

class CategorySearchAdapter(val listener:OnItemClickListener,var context: Context, val list : ArrayList<CategoryModel>)
    : RecyclerView.Adapter<CategorySearchAdapter.CategoryViewHolder>(){
    inner class CategoryViewHolder(view: View): RecyclerView.ViewHolder(view){
        var binding = LayoutCategorySearchBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
       return CategoryViewHolder(
           LayoutInflater.from(context).inflate(R.layout.layout_category_search,parent,false))
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {

        val button = holder.binding.btnCategory
        button.text = list[position].cat

        val currentItem = list[position].cat.toString()
        Log.d("MyApp", currentItem )
       button.setOnClickListener {
            listener.onItemClick(currentItem)

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    interface OnItemClickListener{
        fun onItemClick(catSelected : String)
    }
}