package my.edu.tarc.thrifty.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.adapter.AllProductAdapter

import my.edu.tarc.thrifty.adapter.ProductAdapter
import my.edu.tarc.thrifty.databinding.FragmentAllProductBinding
import my.edu.tarc.thrifty.model.AddProductModel


class AllProductFragment : Fragment() {
    private lateinit var binding: FragmentAllProductBinding
    private lateinit var list: ArrayList<AddProductModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllProductBinding.inflate(layoutInflater)

        //Search
        val searchView = binding.productSearchView

        list = ArrayList()
        Firebase.firestore.collection("products")
            .get().addOnSuccessListener {
                list.clear()
                for(doc in it.documents){
                    val data = doc.toObject(AddProductModel::class.java)
                    list.add(data!!)
                }
                binding.allProductRecycler.adapter = AllProductAdapter(requireContext(),list)
            }
        return binding.root
    }
}