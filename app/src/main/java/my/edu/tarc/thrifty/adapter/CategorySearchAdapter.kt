package my.edu.tarc.thriftyadmin.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.databinding.LayoutCategorySearchBinding
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