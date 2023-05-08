package my.edu.tarc.thrifty.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.edu.tarc.thrifty.MainActivity
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.databinding.FragmentProductDetailsBinding
import my.edu.tarc.thrifty.roomdb.AppDatabase
import my.edu.tarc.thrifty.roomdb.ProductDao
import my.edu.tarc.thrifty.roomdb.ProductModel

class ProductDetailsFragment : Fragment() {
    private lateinit var binding : FragmentProductDetailsBinding
    private val args : ProductDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProductDetailsBinding.inflate(layoutInflater)

//        getProductDetails(intent.getStringExtra("id"))
        getProductDetails(args.id)

        return binding.root
    }
    private fun getProductDetails(proId:String?) {
        Firebase.firestore.collection("products")
            .document(proId!!).get().addOnSuccessListener {
                val list = it.get("productImages") as ArrayList<String>
                val name = it.getString("productName")
                val productSp = it.getString("productSp")
                val productDesc = it.getString("productDescription")
                val productCarbon = it.getString("carbon")
                binding.tvName.text = name
                binding.tvPrice.text = getString(R.string.rm) + productSp
                binding.tvDesc.text = productDesc
                binding.tvCarbons.text = getString(R.string.total_carbon_footprint_saved) + productCarbon + getString(R.string.kg)
                Log.d("MyApp",list.toString())
                val slideList = ArrayList<SlideModel>()
                for(data in list){
                    slideList.add(SlideModel(data, ScaleTypes.CENTER_CROP))
                }

                cartAction(proId,name,productSp,it.getString("productCoverImg"),productCarbon)
                binding.imageSlider.setImageList(slideList)
            }.addOnFailureListener {
                Toast.makeText(requireContext(),"Something went wrong", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cartAction(proId: String, name: String?, productSp: String?, coverImg: String?,carbon: String?) {
        val productDao = AppDatabase.getInstance(requireContext()).productDao()
        if(productDao.isExit(proId)!=null)
            binding.tvAddToCart.text = getString(R.string.goCart)
        else
            binding.tvAddToCart.text = getString(R.string.addCart)
        binding.tvAddToCart.setOnClickListener{
            if(productDao.isExit(proId)!=null){
                openCart()
            }else{
                addToCart(productDao, proId, name, productSp,coverImg,carbon)
            }
        }
    }

    private fun addToCart(productDao: ProductDao, proId: String, name: String?, productSp: String?, coverImg: String?, carbon: String?) {
        val data = ProductModel(proId,name,coverImg,productSp,carbon)
        Log.d("MyApp",carbon!!)
        lifecycleScope.launch(Dispatchers.IO){
            productDao.insertProduct(data)
            binding.tvAddToCart.text = getString(R.string.goCart)
        }
    }

    private fun openCart() {
        val preference = requireContext().getSharedPreferences("info", AppCompatActivity.MODE_PRIVATE)
        val editor = preference.edit()
        editor.putBoolean("isCart",true)
        editor.apply()

        val intent = Intent(requireContext(),MainActivity::class.java)
        startActivity(intent)
    }
}