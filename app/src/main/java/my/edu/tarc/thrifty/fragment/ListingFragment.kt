package my.edu.tarc.thrifty.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.adapter.AllListingAdapter
import my.edu.tarc.thrifty.databinding.FragmentListingBinding
import my.edu.tarc.thrifty.model.AddProductModel
import my.edu.tarc.thrifty.model.CategoryModel
import my.edu.tarc.thriftyadmin.adapter.CategorySearchAdapter
import java.util.*
import kotlin.collections.ArrayList


class ListingFragment : Fragment() ,CategorySearchAdapter.OnItemClickListener{
    private lateinit var binding : FragmentListingBinding
    private lateinit var list: ArrayList<AddProductModel>
    private lateinit var adapter: AllListingAdapter
    private val args : ListingFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentListingBinding.inflate(layoutInflater)
        list = ArrayList()
        list = getListings()
        adapter = AllListingAdapter(requireContext(), list)
        binding.productRecycler.adapter = adapter

        binding.floatingActionButton.setOnClickListener{
            val action = ListingFragmentDirections.actionListingFragmentToAddListingFragment(args.email)
            findNavController().navigate(action)
        }
        val sortButton = binding.btnSort

        sortButton.setOnClickListener {
            val sortPopup = PopupMenu(requireContext(), sortButton)
            sortPopup.menuInflater.inflate(R.menu.sort_menu, sortPopup.menu)
            sortPopup.setOnMenuItemClickListener { item ->
                Log.d("MyApp","Sort by: ${item.itemId}")
                when (item.itemId) {
                    R.id.prodName -> {
                        // Sort by name
                        list.sortBy { it.productName }
                        adapter = AllListingAdapter(requireContext(), list)
                        binding.productRecycler.adapter = adapter
                    }
                    R.id.prodCarbon -> {
                        // Sort by carbon
                        list.sortBy { it.carbon?.toFloat()  }
                        adapter = AllListingAdapter(requireContext(), list)
                        binding.productRecycler.adapter = adapter
                    }
                    R.id.prodPrice -> {
                        // Sort by price
                        list.sortBy { it.productSp?.toFloat()  }
                        adapter = AllListingAdapter(requireContext(), list)
                        binding.productRecycler.adapter = adapter
                    }
                }
                true
            }
            sortPopup.show()
        }

        //Search
        val searchView = binding.productSearchView
        getCategories()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
    private fun getListings() :ArrayList<AddProductModel>{
        val list = ArrayList<AddProductModel>()
        val queryByName = Firebase.firestore.collection("products").
        whereEqualTo("userEmail",args.email)
        queryByName.get().addOnSuccessListener {
            list.clear()

            for(doc in it.documents){
                val data = doc.toObject(AddProductModel::class.java)
                list.add(data!!)
            }
            //the listing will be sorted by product name by default
            list.sortBy{it.productName}
            adapter = AllListingAdapter(requireContext(),list)
            binding.productRecycler.adapter = adapter
        }
        return list
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
                val all = CategoryModel("All","")

                list.add(0,all)
                list.sortBy { it.cat }
                binding.categoryRecyclerContainer.adapter = CategorySearchAdapter(this,requireContext(),list)
            }
    }

    override fun onItemClick(catSelected : String) {
        Log.d("MyApp","catselected in fragment:" +catSelected)
        if(!catSelected.equals("All"))
            list = getCatProducts(catSelected)

        else if(catSelected.equals("All")||catSelected == ""){
            list = getListings()
        }


    }
    private fun getCatProducts(category:String?)  :ArrayList<AddProductModel>{
        list = ArrayList()
        Firebase.firestore.collection("products")
            .whereEqualTo("productCategory",category)
            .whereEqualTo("userEmail",args.email)
            .get().addOnSuccessListener {
                list.clear()
                for(doc in it.documents){
                    val data = doc.toObject(AddProductModel::class.java)
                    list.add(data!!)
                }
                list.sortBy{it.productName}
                adapter = AllListingAdapter(requireContext(),list)
                binding.productRecycler.adapter = adapter
            }
        return list
    }
}