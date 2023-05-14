package my.edu.tarc.thrifty.fragment

import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.edu.tarc.thrifty.MainActivity
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.databinding.FragmentCheckoutBinding
import my.edu.tarc.thrifty.roomdb.AppDatabase
import my.edu.tarc.thrifty.roomdb.ProductModel
import java.text.SimpleDateFormat
import java.util.*


class CheckoutFragment : Fragment() {
    private val args : AddressFragmentArgs by navArgs()
    private lateinit var binding : FragmentCheckoutBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var loadingDialog: Dialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCheckoutBinding.inflate(layoutInflater)

        loadingDialog = Dialog(requireActivity())
        loadingDialog.setContentView(R.layout.progress_layout)
        loadingDialog.setCancelable(false)
        loadingDialog.show()

        uploadData()
        return binding.root
    }
    private fun uploadData() {
        val id = args.productIds.toList()
        for(currentId in id!!){
            fetchData(currentId)
            Toast.makeText(requireContext(),getString(R.string.placedOrder),Toast.LENGTH_SHORT).show()
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
                    currentTime.toString(),
                    it.getString("userEmail"))
            }
    }

    private fun saveData(name: String?, price: String?, productId: String,carbon: String?,orderDate: String?,orderTime: String?, userListed:String?) {
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
        data["userSeller"] = userListed!!

        val firestore = Firebase.firestore.collection("allOrders")
        val key = firestore.document().id
        data["orderId"] = key

        firestore.document(key).set(data).addOnSuccessListener {
            //Update the product availability after purchasing
            Thread.sleep(1000)
            updateAvailability(productId)

            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            if(activity != null) {
                activity?.finish()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(),getString(R.string.wentWrong),Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAvailability(productId: String) {
        //update product availability
        val availability = hashMapOf<String, Any>()
        availability["availability"] = "Unavailable"

        Firebase.firestore.collection("products")
            .document(productId).update(availability).addOnSuccessListener {
//                Toast.makeText(context, "Product is unavailable now", Toast.LENGTH_SHORT).show()
                Log.d("MyApp",productId + " is unavaible now")
            }.addOnFailureListener {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }

    }
}
