package my.edu.tarc.thrifty.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
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
        list = getProducts()
        adapter = AllProductAdapter(requireContext(), list)
        binding.allProductRecycler.adapter = adapter

        val sortButton = binding.btnSort
        sortButton.setOnClickListener {
            val sortPopup = PopupMenu(requireContext(), sortButton)
            sortPopup.menuInflater.inflate(R.menu.sort_menu, sortPopup.menu)
            sortPopup.setOnMenuItemClickListener { item ->

                when (item.itemId) {
                    R.id.prodName -> {
                        // Sort by name
                        list.sortBy { it.productName }
                        adapter = AllProductAdapter(requireContext(), list)
                        binding.allProductRecycler.adapter = adapter
                        Toast.makeText(requireContext(), getString(R.string.sortName),Toast.LENGTH_SHORT).show()
                    }
                    R.id.prodCarbon -> {
                        // Sort by carbon
                        list.sortBy { it.carbon?.toFloat() }
                        adapter = AllProductAdapter(requireContext(), list)
                        binding.allProductRecycler.adapter = adapter
                        Toast.makeText(requireContext(),getString(R.string.sortCarbon),Toast.LENGTH_SHORT).show()
                    }
                    R.id.prodPrice -> {
                        // Sort by price
                        list.sortBy { it.productSp?.toFloat() }
                        adapter = AllProductAdapter(requireContext(), list)
                        binding.allProductRecycler.adapter = adapter
                        Toast.makeText(requireContext(),getString(R.string.sortPrice),Toast.LENGTH_SHORT).show()
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
        val lists= arrayListOf(
            getString(R.string.list1),
            getString(R.string.list2),
            getString(R.string.list3),
            getString(R.string.list4),
            getString(R.string.list5),
            getString(R.string.list6),
            getString(R.string.list7))

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
        Toast.makeText(requireContext(),getString(R.string.currentCat) + catSelected,Toast.LENGTH_SHORT ).show()
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
                list.sortBy{it.productName}
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
                list.sortBy{it.productName}
                adapter = AllProductAdapter(requireContext(), list)
                binding.allProductRecycler.adapter = adapter
            }
        return list
    }
}
