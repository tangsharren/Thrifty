package my.edu.tarc.thrifty.fragment

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.databinding.FragmentAddressBinding

class AddressFragment : Fragment() {

    private lateinit var binding :FragmentAddressBinding
    private lateinit var list : ArrayList<String>
    private lateinit var preferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list = ArrayList()

//        preferences = activity.getSharedPreferences("user", Context.MODE_PRIVATE)
        //Get preference
//        val userName = userPrefs.userName
//        val userEmail = userPrefs.userEmail
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
            Toast.makeText(requireContext(),"Please fill all fields", Toast.LENGTH_SHORT).show()
        else{
            storeData(postcode,street,state,unitNo)
//            finish()
        }
    }

    private fun storeData(postcode: String, street: String, state: String, unitNo: String) {
        preferences = requireActivity().getSharedPreferences("user", MODE_PRIVATE)
        val email = preferences.getString("email", "")!!

        val map = hashMapOf<String,Any>()
        map["unitNo"] = unitNo
        map["state"] = state
        map["street"] = street
        map["postcode"] = postcode

        Firebase.firestore.collection("users")
            .document(email)
            .update(map).addOnSuccessListener {
                //to get args from cart fragment
                val previousFragment = findNavController().previousBackStackEntry?.destination?.id
                if(previousFragment == R.id.cartFragment){
                    val args : AddressFragmentArgs by navArgs()
//                if(args.totalCost!=""){
                    var total = args.totalCost
                    val list = args.productIds!!.toList()
                    val b = Bundle()
                    b.putStringArrayList("productIds",ArrayList(list))
                    val action = AddressFragmentDirections.actionAddressFragmentToCheckoutFragment(list.toTypedArray(), total.toString())
                    findNavController().navigate(action)
                }
                Toast.makeText(requireContext(),"Address Updated", Toast.LENGTH_SHORT).show()
            }

            .addOnFailureListener {
                Toast.makeText(requireContext(),"Something went wrong", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserInfo() {
        val email : String
        val user = FirebaseAuth.getInstance().getCurrentUser()
        user.let {
            email = it!!.email!!
        }
//        val userEmail = userPrefs.userEmail


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