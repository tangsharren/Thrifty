package my.edu.tarc.thrifty.fragment

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.edu.tarc.thrifty.MainActivity
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.databinding.FragmentAddressBinding
import my.edu.tarc.thrifty.databinding.FragmentCartBinding
import my.edu.tarc.thrifty.databinding.FragmentCheckoutBinding
import my.edu.tarc.thrifty.roomdb.AppDatabase
import my.edu.tarc.thrifty.roomdb.ProductModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CheckoutFragment : Fragment() {
    private val args : AddressFragmentArgs by navArgs()
    private lateinit var binding : FragmentCheckoutBinding
    private lateinit var preferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCheckoutBinding.inflate(layoutInflater)
        Toast.makeText(requireContext(),getString(R.string.placedOrder), Toast.LENGTH_SHORT).show()
        uploadData()
        return binding.root
    }
    private fun uploadData() {
        val id = args.productIds.toList()
        for(currentId in id!!){
            fetchData(currentId)
        }
    }

    private fun fetchData(productId: String?) {
        val dao = AppDatabase.getInstance(requireContext()).productDao()
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = dateFormatter.format(Date())

        val timeFormatter = SimpleDateFormat("HH:mm:ss")
        val currentTime = timeFormatter.format(Date())
        Log.d("MyApp",currentDate.toString())
        Log.d("MyApp",currentTime.toString())
        Firebase.firestore.collection("products")
            .document(productId!!).get().addOnSuccessListener {
                lifecycleScope.launch(Dispatchers.IO){
                    dao.deleteProduct(ProductModel(productId))
                }
                saveData(it.getString("productName"),
                    it.getString("productSp"),
                    productId,
                    it.getString("carbon"),
                    currentDate.toString(),
                    currentTime.toString())
            }
    }

    private fun saveData(name: String?, price: String?, productId: String,carbon: String?,orderDate: String?,orderTime: String?) {
        preferences = requireActivity().getSharedPreferences("user", MODE_PRIVATE)
        val email = preferences.getString("email", "")!!


        val data = hashMapOf<String,Any>()
        data["name"] = name!!
        data["price"] = price!!
        data["productId"] = productId
        data["status"] = getString(R.string.ordered)
        data["userId"] = email
        data["carbon"] = carbon!!
        data["orderDate"] = orderDate!!
        data["orderTime"] = orderTime!!

        val firestore = Firebase.firestore.collection("allOrders")
        val key = firestore.document().id
        data["orderId"] = key

        firestore.document(key).set(data).addOnSuccessListener {
            Toast.makeText(requireContext(),getString(R.string.placedOrder),Toast.LENGTH_SHORT).show()

            //Delete the product after purchasing
            deleteProduct(productId)
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            if(activity != null) {
                activity?.finish()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(),getString(R.string.wentWrong),Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteProduct(productId: String) {
        val db = Firebase.firestore
        val storageRef = db.collection("products").document(productId!!)

        storageRef.delete()
            .addOnSuccessListener {
               Log.d("MyApp","Purchased product deleted")
            }
            .addOnFailureListener { e ->
                Log.d("MyApp", e.toString())
            }
    }
}
