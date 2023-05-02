package my.edu.tarc.thrifty.activity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.databinding.ActivityAddressBinding
import my.edu.tarc.thrifty.fragment.AddressFragmentArgs

class AddressActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAddressBinding
    private lateinit var preferences : SharedPreferences
    private val args : AddressFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = this.getSharedPreferences("user", MODE_PRIVATE)

        loadUserInfo()

        binding.saveAdd.setOnClickListener {
            validateData(
                binding.userEmail.text.toString(),
                binding.userName.text.toString(),
                binding.userPostcode.text.toString(),
                binding.userStreet.text.toString(),
                binding.userState.text.toString(),
                binding.userUnitNo.text.toString(),
            )
        }
    }

    private fun validateData(email: String, name: String, postcode: String,
                             street: String, state: String, unitNo: String) {
        if(email.isEmpty() || name.isEmpty() || postcode.isEmpty()|| street.isEmpty()|| state.isEmpty()|| unitNo.isEmpty())
            Toast.makeText(this,"Please fill all fields",Toast.LENGTH_SHORT).show()
        else{
            storeData(postcode,street,state,unitNo)
            finish()
        }
    }

    private fun storeData(postcode: String, street: String, state: String, unitNo: String) {
        val email : String
        val user = FirebaseAuth.getInstance().getCurrentUser()
        user.let {
            email = it!!.email!!
        }
        val map = hashMapOf<String,Any>()
        map["unitNo"] = unitNo
        map["state"] = state
        map["street"] = street
        map["postcode"] = postcode

        Firebase.firestore.collection("users")
            .document(email)
            .update(map).addOnSuccessListener {
//                val totalCost = args.totalCost
//                val productIds = args.totalCost
                val b = Bundle()
                b.putStringArrayList("productIds",intent.getStringArrayListExtra("productIds"))
                if(intent.getStringExtra("totalCost")!=null){
                    val totalCost =intent.getStringExtra("totalCost")
                    b.putString("totalCost",totalCost)
                    val intent = Intent(this, CheckoutActivity::class.java)
                    intent.putExtras(b)
                    startActivity(intent)
                }
                Toast.makeText(this,"Address Updated",Toast.LENGTH_SHORT).show()

            }

            .addOnFailureListener {
                Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserInfo() {
        val email : String
        val user = FirebaseAuth.getInstance().getCurrentUser()
        user.let {
             email = it!!.email!!
        }

        Firebase.firestore.collection("users")
            .document(email)
            .get().addOnSuccessListener {
                binding.userName.setText(it.getString("userName"))
                binding.userEmail.setText(it.getString("userEmail"))
                binding.userStreet.setText(it.getString("street"))
                binding.userUnitNo.setText(it.getString("unitNo"))
                binding.userState.setText(it.getString("state"))
                binding.userPostcode.setText(it.getString("postcode"))
            }
            .addOnFailureListener {
                //Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show()
            }
    }
}