package my.edu.tarc.thrifty.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.adapter.AllOrdersAdapter
import my.edu.tarc.thrifty.databinding.FragmentAllOrderBinding
import my.edu.tarc.thrifty.model.AllOrderModel
import my.edu.tarc.thrifty.roomdb.ProductModel


class AllOrderFragment : Fragment() {
    private lateinit var binding: FragmentAllOrderBinding
    private lateinit var list: ArrayList<AllOrderModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAllOrderBinding.inflate(layoutInflater)

        list = ArrayList()
        val preferences = requireContext().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        Firebase.firestore.collection("allOrders")
            .whereEqualTo("userId", preferences.getString("email","")!!)
            . addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.d("MyApp", "Listen failed.", e)
                    return@addSnapshotListener
                }

                list.clear()
                for (doc in querySnapshot!!) {
                    val data = doc.toObject(AllOrderModel::class.java)
                    list.add(data)
                }
                binding.recyclerView.adapter = AllOrdersAdapter(list, requireContext())

                var totalCarbonSaved=0.0
                for(item in list){
                    if(item.status != "Canceled"){
                        totalCarbonSaved += item.carbon!!.toFloat()
                    }
                }
//                String.format("Total Carbon Footprint Saved : %.2f kg", carbon)
                binding.btnTotalCarbon.text = String.format("Total of  %.2f kg carbon footprint is saved from your past purchase!",totalCarbonSaved)
                Log.d("MyApp",totalCarbonSaved.toString())
//                findNavController().previousBackStackEntry?.savedStateHandle?.set("carbonSaved", totalCarbonSaved.toString())
//                findNavController().popBackStack()
            }
        return binding.root
    }
}