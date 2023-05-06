package my.edu.tarc.thrifty.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.adapter.AllProductAdapter

import my.edu.tarc.thrifty.databinding.FragmentAllProductBinding
import my.edu.tarc.thrifty.model.AddProductModel
import my.edu.tarc.thrifty.model.CategoryModel
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

        list = ArrayList()
//        Firebase.firestore.collection("products")
//            .get().addOnSuccessListener {
//                list.clear()
//                for (doc in it.documents) {
//                    val data = doc.toObject(AddProductModel::class.java)
//                    list.add(data!!)
//                }
//            }
        list = getProducts()
        adapter = AllProductAdapter(requireContext(), list)
        binding.allProductRecycler.adapter = adapter

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
                        adapter = AllProductAdapter(requireContext(), list)
                        binding.allProductRecycler.adapter = adapter
                    }
                    R.id.prodCarbon -> {
                        // Sort by carbon
                        list.sortBy { it.carbon }
                        adapter = AllProductAdapter(requireContext(), list)
                        binding.allProductRecycler.adapter = adapter
                    }
                    R.id.prodPrice -> {
                        // Sort by price
                        list.sortBy { it.productSp }
                        adapter = AllProductAdapter(requireContext(), list)
                        binding.allProductRecycler.adapter = adapter
                    }
                }
                true
            }
            sortPopup.show()
        }

        //Search
        val searchView = binding.productSearchView
        getCategories()

        //For auto scrolling text view
        val textView = binding.encourageMsg

        textView.requestFocus()
        textView.setHorizontallyScrolling(true)
        //To show encouraging message
        // Create an array list of strings
        val lists= arrayListOf("Do you know? Furniture production has a huge global carbon footprint. Each piece of furniture generates an average of 47kg of carbon dioxide equivalents."
            , "Hey! The fashion industry accounts for about 8-10% of global carbon emissions, and nearly 20% of wastewater! Let's start thrifting now",
            "Fashion industry creates more emission than flying, forget about fast fashion now!",
            "Do you enjoy exploring different styles and trends without breaking the bank? Starts thrifting now!",
        "Thrifting saves tons of preloved from ending up in landfills",
        "Let's discover your treasures here, from vintage gems to designer labels",
        "Reusing 1 kg of clothing saves 25 kg of CO2 according to a study.")

        var index = 0
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    textView.text = lists[index]
                    index++
                    if (index == lists.size) {
                        index = 0
                    }
                }
            }
        }, 200, 20000)

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
        Log.d("MyApp","catselected in fragment:" +catSelected.toString())
        if(!catSelected.equals("All"))
            list = getCatProducts(catSelected)
        else if(catSelected.equals("All")||catSelected == "")
            list = getProducts()
    }
    private fun getCatProducts(category:String?) :ArrayList<AddProductModel>{
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
        return list
    }
    private fun getProducts():ArrayList<AddProductModel> {
        val list = ArrayList<AddProductModel>()
        Firebase.firestore.collection("products")
            .get().addOnSuccessListener {
                list.clear()
                for (doc in it.documents) {
                    val data = doc.toObject(AddProductModel::class.java)
                    list.add(data!!)
                }
                adapter = AllProductAdapter(requireContext(), list)
                binding.allProductRecycler.adapter = adapter
            }
        return list
    }
}
