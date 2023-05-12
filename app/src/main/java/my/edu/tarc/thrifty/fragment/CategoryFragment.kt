package my.edu.tarc.thrifty.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.adapter.CategoryProductAdapter
import my.edu.tarc.thrifty.databinding.FragmentCategoryBinding
import my.edu.tarc.thrifty.databinding.FragmentProductDetailsBinding
import my.edu.tarc.thrifty.model.AddProductModel


class CategoryFragment : Fragment() {
    private lateinit var binding : FragmentCategoryBinding
    private val args : CategoryFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoryBinding.inflate(layoutInflater)

        getProducts(args.cat)

        return binding.root
    }
    private fun getProducts(category:String?) {
        val list = ArrayList<AddProductModel>()
        Firebase.firestore.collection("products").whereEqualTo("product",category)
            .get().addOnSuccessListener {
                list.clear()
                for(doc in it.documents){
                    val data = doc.toObject(AddProductModel::class.java)
                    list.add(data!!)
                }
                val recyclerView = binding.recyclerView
                recyclerView.adapter = CategoryProductAdapter(requireContext(),list)
            }
    }
}