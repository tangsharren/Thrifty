package my.edu.tarc.thrifty.fragment

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.adapter.OrderStatusAdapter
import my.edu.tarc.thrifty.databinding.FragmentOrderStatusBinding
import my.edu.tarc.thrifty.model.AllOrderModel

class OrderStatusFragment : Fragment() {
    private lateinit var binding : FragmentOrderStatusBinding
    private lateinit var preferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderStatusBinding.inflate(layoutInflater)
        preferences = requireActivity().getSharedPreferences("user", MODE_PRIVATE)
        val currentUser = preferences.getString("email", "")!!

        //get listing sold
        val list = ArrayList<AllOrderModel>()
        Firebase.firestore.collection("allOrders").whereEqualTo("userSeller",currentUser)
            . addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.d("MyApp", "Listen failed.", e)
                    return@addSnapshotListener
                }

                list.clear()
                for (doc in querySnapshot!!) {
                    val data = doc.toObject(AllOrderModel::class.java)
                    list.add(data!!)
                }
                checkIfFragmentAttached {
                    binding.recyclerView.adapter = OrderStatusAdapter(list,requireContext())
                }

            }
        Firebase.firestore.collection("allOrders").get().addOnFailureListener {
            Log.d("MyApp", "no!!")
        }

        return binding.root
    }
    fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
        else{
            Thread.sleep(2000)
        }
    }
}