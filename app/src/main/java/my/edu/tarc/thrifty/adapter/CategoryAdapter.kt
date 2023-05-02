package my.edu.tarc.thriftyadmin.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.activity.CategoryActivity
import my.edu.tarc.thrifty.databinding.LayoutCategoryItemBinding
import my.edu.tarc.thrifty.fragment.HomeFragmentDirections
import my.edu.tarc.thrifty.model.CategoryModel

class CategoryAdapter(var context: Context, val list : ArrayList<CategoryModel>)
    : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>(){
    inner class CategoryViewHolder(view: View): RecyclerView.ViewHolder(view){
        var binding = LayoutCategoryItemBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
       return CategoryViewHolder(
           LayoutInflater.from(context).inflate(R.layout.layout_category_item,parent,false))
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.binding.textView.text = list[position].cat
        Glide.with(context).load(list[position].img).into(holder.binding.imageView)

        holder.itemView.setOnClickListener{
//            val intent = Intent(context,CategoryActivity::class.java)
//            intent.putExtra("cat",list[position].cat)
//            context.startActivity(intent)
            val action = HomeFragmentDirections.actionHomeFragmentToCategoryFragment(list[position].cat!!)
            findNavController(holder.itemView).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}