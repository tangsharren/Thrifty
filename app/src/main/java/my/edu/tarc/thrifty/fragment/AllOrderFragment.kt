package my.edu.tarc.thrifty.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.adapter.AllOrdersAdapter
import my.edu.tarc.thrifty.databinding.FragmentAllOrderBinding
import my.edu.tarc.thrifty.model.AllOrderModel
import my.edu.tarc.thrifty.model.CategoryModel
import my.edu.tarc.thriftyadmin.adapter.CategorySearchAdapter
import java.util.*
import kotlin.collections.ArrayList


class AllOrderFragment : Fragment(){
    private lateinit var binding: FragmentAllOrderBinding
    private lateinit var list: ArrayList<AllOrderModel>
    private lateinit var adapter: AllOrdersAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAllOrderBinding.inflate(layoutInflater)
        list= ArrayList()

        list = getOrders()

        val sortButton = binding.btnSort
        sortButton.setOnClickListener {
            // Kotlin code for the popup
            val sortPopup = PopupMenu(requireContext(), sortButton)
            sortPopup.menuInflater.inflate(R.menu.sort_order, sortPopup.menu)
            sortPopup.setOnMenuItemClickListener { item ->
                Log.d("MyApp", "Sort by: ${item.itemId}")
                when (item.itemId) {
                    R.id.orderProdName -> {
                        // Sort by name
                        list.sortBy { it.name }
                        adapter = AllOrdersAdapter(list, requireContext())
                        binding.recyclerView.adapter = adapter
                    }
                    R.id.orderProdCarbon -> {
                        // Sort by carbon
                        list.sortBy { it.carbon }
                        adapter = AllOrdersAdapter(list, requireContext())
                        binding.recyclerView.adapter = adapter
                    }
                    R.id.orderPrice -> {
                        // Sort by price
                        list.sortBy { it.price }
                        adapter = AllOrdersAdapter(list, requireContext())
                        binding.recyclerView.adapter = adapter
                    }
                    R.id.orderDate -> {
                        // Sort by price
                        list.sortBy { it.orderDate }
                        adapter = AllOrdersAdapter(list, requireContext())
                        binding.recyclerView.adapter = adapter
                    }
                }
                true
            }
            sortPopup.show()
        }

        //Search
        val searchView = binding.productSearchView

        val textView = binding.carbonMsg

        textView.requestFocus()
        textView.setHorizontallyScrolling(true)

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
        val searchList = ArrayList<AllOrderModel>()
        for (dataClass in list) {
            if (dataClass.name?.lowercase()
                    ?.contains(text.lowercase(Locale.getDefault())) == true
            ) {
                searchList.add(dataClass)
            }
        }
        adapter.searchDataList(searchList)
    }

    private fun getOrders() :ArrayList<AllOrderModel>{
        val list = ArrayList<AllOrderModel>()
        val preferences =
            requireContext().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        Firebase.firestore.collection("allOrders")
            .whereEqualTo("userId", preferences.getString("email", "")!!)
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.d("MyApp", "Listen failed.", e)
                    return@addSnapshotListener
                }

                list.clear()
                for (doc in querySnapshot!!) {
                    val data = doc.toObject(AllOrderModel::class.java)
                    list.add(data)
                }
                var totalCarbonSaved = 0.0
                for (item in list) {
                    if (item.status != "Canceled") {
                        totalCarbonSaved += item.carbon!!.toFloat()
                    }
                }
                binding.carbonMsg.text = String.format(
                    "Total of  %.2f kg carbon footprint is saved from your past purchase!",
                    totalCarbonSaved
                )
                Log.d("MyApp","OrderList:$list")
                adapter = AllOrdersAdapter(list, requireContext())
                binding.recyclerView.adapter = adapter
            }
        return list
    }
}