package my.edu.tarc.thrifty.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.adapter.AllListingAdapter
import my.edu.tarc.thrifty.databinding.FragmentListingBinding
import my.edu.tarc.thrifty.model.AddProductModel


class ListingFragment : Fragment() {
    private lateinit var binding : FragmentListingBinding
    private val args : ListingFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentListingBinding.inflate(layoutInflater)
        getListings(args.email)
//        getProducts()
        binding.floatingActionButton.setOnClickListener{
            val action = ListingFragmentDirections.actionListingFragmentToAddListingFragment(args.email)
            findNavController().navigate(action)
        }

        return binding.root
    }
    private fun getListings(email:String) {
        val list = ArrayList<AddProductModel>()
        val queryByName = Firebase.firestore.collection("products").whereEqualTo("userEmail",email)
        queryByName.get().addOnSuccessListener {
            list.clear()

            for(doc in it.documents){
                val data = doc.toObject(AddProductModel::class.java)
                list.add(data!!)
            }
            Log.d("MyApp","getListings:"+list.toString())
            binding.productRecycler.adapter = AllListingAdapter(requireContext(),list)
        }
    }
}