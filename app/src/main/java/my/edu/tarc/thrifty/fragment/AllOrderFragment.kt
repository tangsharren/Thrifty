package my.edu.tarc.thrifty.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.activity.AddressActivity
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
            }


        return binding.root

    }
}