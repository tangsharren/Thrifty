package my.edu.tarc.thrifty.fragment

import android.content.res.Resources.Theme
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.grpc.internal.SharedResourceHolder
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.adapter.AllProductAdapter
import my.edu.tarc.thrifty.adapter.CategoryProductAdapter

import my.edu.tarc.thrifty.adapter.ProductAdapter
import my.edu.tarc.thrifty.databinding.FragmentAllProductBinding
import my.edu.tarc.thrifty.model.AddProductModel
import my.edu.tarc.thrifty.model.CategoryModel
import my.edu.tarc.thriftyadmin.adapter.CategoryAdapter
import my.edu.tarc.thriftyadmin.adapter.CategorySearchAdapter
import java.util.*
import kotlin.collections.ArrayList


class AllProductFragment : Fragment() ,CategorySearchAdapter.OnItemClickListener{
    private lateinit var binding: FragmentAllProductBinding
    private lateinit var list: ArrayList<AddProductModel>
    private lateinit var adapter: AllProductAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllProductBinding.inflate(layoutInflater)

        //Search
        val searchView = binding.productSearchView
        getCategories()

        list = ArrayList()
        Firebase.firestore.collection("products")
            .get().addOnSuccessListener {
                list.clear()
                for (doc in it.documents) {
                    val data = doc.toObject(AddProductModel::class.java)
                    list.add(data!!)
                }
                list.sortByDescending {
                    it.productName
                }
                adapter = AllProductAdapter(requireContext(), list)
                binding.allProductRecycler.adapter = adapter
            }
        binding.productSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {

                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                searchList(newText)
                return true
            }
        })

        return binding.root
    }
    fun searchList(text: String) {
        val searchList = ArrayList<AddProductModel>()
        for (dataClass in list) {
            if (dataClass.productName?.lowercase()
                    ?.contains(text.lowercase(Locale.getDefault())) == true
            ) {
                searchList.add(dataClass)
            }
        }
        adapter.searchDataList(searchList)
    }
    private fun getCategories(){
        val list = ArrayList<CategoryModel>()
        Firebase.firestore.collection("categories")
            .get().addOnSuccessListener {
                list.clear()
                for(doc in it.documents){
                    val data = doc.toObject(CategoryModel::class.java)
                    list.add(data!!)
                }
                binding.categoryRecyclerContainer.adapter = CategorySearchAdapter(this,requireContext(),list)
            }
    }

    override fun onItemClick(catSelected : String) {
        getCatProducts(catSelected)
    }
    private fun getCatProducts(category:String?) {
        val list = ArrayList<AddProductModel>()
        Firebase.firestore.collection("products").whereEqualTo("productCategory",category)
            .get().addOnSuccessListener {
                list.clear()
                for(doc in it.documents){
                    val data = doc.toObject(AddProductModel::class.java)
                    list.add(data!!)
                }
                adapter = AllProductAdapter(requireContext(), list)
                binding.allProductRecycler.adapter = adapter
            }
    }
}
