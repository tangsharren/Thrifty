package my.edu.tarc.thrifty.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.android.volley.toolbox.StringRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.rpc.context.AttributeContext.Response
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.stripe.android.PaymentConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.edu.tarc.thrifty.MainActivity
import my.edu.tarc.thrifty.databinding.ActivityCheckoutBinding
import my.edu.tarc.thrifty.roomdb.AppDatabase
import my.edu.tarc.thrifty.roomdb.ProductModel
import org.json.JSONObject
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import java.text.SimpleDateFormat
import java.util.*

//class CheckoutActivity : AppCompatActivity() ,PaymentResultListener{
class CheckoutActivity : AppCompatActivity() {
    private lateinit var binding :ActivityCheckoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Toast.makeText(this,"Order Placed",Toast.LENGTH_SHORT).show()
        uploadData()
    }
    private fun uploadData() {
        val id = intent.getStringArrayListExtra("productIds")
        for(currentId in id!!){
            fetchData(currentId)
        }
    }

    private fun fetchData(productId: String?) {
        val dao = AppDatabase.getInstance(this).productDao()
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
        val preferences = this.getSharedPreferences("user", MODE_PRIVATE)
        val data = hashMapOf<String,Any>()
        data["name"] = name!!
        data["price"] = price!!
        data["productId"] = productId
        data["status"] = "Ordered"
        data["userId"] = preferences.getString("email","")!!
        data["carbon"] = carbon!!
        data["orderDate"] = orderDate!!
        data["orderTime"] = orderTime!!

        val firestore = Firebase.firestore.collection("allOrders")
        val key = firestore.document().id
        data["orderId"] = key

        firestore.document(key).set(data).addOnSuccessListener {
            Toast.makeText(this,"Order Placed",Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }.addOnFailureListener {
            Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show()
        }
    }
}