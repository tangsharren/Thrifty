package my.edu.tarc.thrifty.fragment

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.adapter.ProductAdapter
import my.edu.tarc.thrifty.databinding.FragmentHomeBinding
import my.edu.tarc.thrifty.model.AddProductModel
import my.edu.tarc.thrifty.model.CategoryModel
import my.edu.tarc.thriftyadmin.adapter.CategoryAdapter

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var preferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentHomeBinding.inflate(layoutInflater)
        preferences = requireActivity().getSharedPreferences("user", MODE_PRIVATE)
        val currentUser = preferences.getString("email", "")!!
        binding.categoryRecycler.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment)
        }
        val preference = requireContext().getSharedPreferences("info",AppCompatActivity.MODE_PRIVATE)

        if(preference.getBoolean("isCart",false)){
            findNavController().navigate(R.id.action_homeFragment_to_cartFragment)
        }
        binding.tvAllProduct.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToAllProductFragment(currentUser)
            findNavController().navigate(action)
//            it.findNavController().navigate(R.id.action_homeFragment_to_allProductFragment)
        }
        getCategories()
        getSliderImage()
        getProducts()
        binding.buttonTrans.setOnClickListener{
            it.findNavController().navigate(R.id.action_homeFragment_to_allProductFragment)
        }
        return binding.root
    }

    private fun getSliderImage() {
        val list = ArrayList<String>()
        Firebase.firestore.collection("slider")
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.d("MyApp", "Listen failed.", e)
                    return@addSnapshotListener
                }
                list.clear()
                for(doc in querySnapshot!!){
                    val data = doc.data?.get("img")
                    list.add(data.toString())
                }
                val slideList = ArrayList<SlideModel>()
                for(data in list){
                    slideList.add(SlideModel(data, ScaleTypes.CENTER_INSIDE))
                }
                binding.slider.setImageList(slideList)

            }
    }
    private fun getProducts() {
        val list = ArrayList<AddProductModel>()
        Firebase.firestore.collection("products")
            .whereEqualTo("availability","Available")
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.d("MyApp", "Listen failed.", e)
                    return@addSnapshotListener
                }
                list.clear()
                for(doc in querySnapshot!!){
                    val data = doc.toObject(AddProductModel::class.java)
                    list.add(data!!)
                    list.sortBy { it.productName }
                }
                checkIfFragmentAttached {
                    binding.productRecycler.adapter = ProductAdapter(requireContext(), list)
                }
            }
    }


    private fun getCategories(){
        val list = ArrayList<CategoryModel>()
        Firebase.firestore.collection("categories")
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.d("MyApp", "Listen failed.", e)
                    return@addSnapshotListener
                }
                list.clear()
                for(doc in querySnapshot!!){
                    val data = doc.toObject(CategoryModel::class.java)
                    list.add(data!!)
                    list.sortBy { it.cat }
                }
                 binding.categoryRecycler.adapter = CategoryAdapter(requireContext(),list)
            }

    }
    fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
    }
}