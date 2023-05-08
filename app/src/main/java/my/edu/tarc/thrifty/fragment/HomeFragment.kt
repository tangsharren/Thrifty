package my.edu.tarc.thrifty.fragment

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentHomeBinding.inflate(layoutInflater)

        binding.categoryRecycler.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment)
        }
        val preference = requireContext().getSharedPreferences("info",AppCompatActivity.MODE_PRIVATE)

        if(preference.getBoolean("isCart",false)){
            findNavController().navigate(R.id.action_homeFragment_to_cartFragment)
        }
        binding.tvAllProduct.setOnClickListener {
            it.findNavController().navigate(R.id.action_homeFragment_to_allProductFragment)
        }
        getCategories()
        getSliderImage()
        getProducts()
        return binding.root
    }

    private fun getSliderImage() {
        val list = ArrayList<String>()
        Firebase.firestore.collection("slider")
            .get().addOnSuccessListener {
                list.clear()
                for(doc in it.documents){
                    val data = doc.data?.get("img")
                    list.add(data.toString())
                }
                Log.d("MyApp","Slider get in home:"+list.toString())
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
            .get().addOnSuccessListener {
                list.clear()
                for(doc in it.documents){
                    val data = doc.toObject(AddProductModel::class.java)
                    list.add(data!!)
                }
                binding.productRecycler.adapter = ProductAdapter(requireContext(),list)
            }
    }


    private fun getCategories(){
        val list = ArrayList<CategoryModel>()
        Firebase.firestore.collection("categories")
            .get().addOnSuccessListener {
                list.clear()
                for(doc in it.documents){
                    val data = doc.toObject(CategoryModel::class.java)
                    list.add(data!!)
                }
                 binding.categoryRecycler.adapter = CategoryAdapter(requireContext(),list)
            }

    }
}