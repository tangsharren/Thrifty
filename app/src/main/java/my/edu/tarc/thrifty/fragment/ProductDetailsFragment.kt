package my.edu.tarc.thrifty.fragment

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
        binding = FragmentProductDetailsBinding.inflate(layoutInflater)
        //args.id is the product id we get from the previous fragment
        checkIfFragmentAttached{
            getProductDetails(args.id)
        }


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
                val seller = it.getString("userEmail")
                binding.tvName.text = name
                binding.tvPrice.text = "RM"+ productSp
                binding.desc.text = productDesc
                binding.tvCarbons.text = getString(R.string.total_carbon_footprint_saved) + productCarbon + getString(R.string.kg)
                binding.seller.text = "Sold by :"+seller
                Log.d("MyApp",list.toString())
                val slideList = ArrayList<SlideModel>()
                for(data in list){
                    slideList.add(SlideModel(data, ScaleTypes.CENTER_CROP))
                }
                //if the product is ordered by someone then cannot add to cart
                val availability = it.getString("availability")
                if(availability == "Unavailable"){
                    binding.tvAddToCart.isVisible = false
                    Toast.makeText(requireContext(),"Sorry, this product is unavailable for purchase",Toast.LENGTH_LONG).show()
                }
                cartAction(proId,name,productSp,it.getString("productCoverImg"),productCarbon)
                binding.imageSlider.setImageList(slideList)

            }.addOnFailureListener {
                Toast.makeText(requireContext(),getString(R.string.wentWrong), Toast.LENGTH_SHORT).show()
            }
    }
    private lateinit var loadingDialog: Dialog
    private fun cartAction(proId: String, name: String?, productSp: String?, coverImg: String?,carbon: String?) {

        loadingDialog = Dialog(requireContext())
        loadingDialog.setContentView(R.layout.progress_layout)
        loadingDialog.setCancelable(false)

        val productDao = AppDatabase.getInstance(requireContext()).productDao()
        if(productDao.isExit(proId)!=null)
            binding.tvAddToCart.text = getString(R.string.goCart)
        else
            binding.tvAddToCart.text = getString(R.string.addCart)

        binding.tvAddToCart.setOnClickListener{
            loadingDialog.show()
            checkIfFragmentAttached {
                Thread.sleep(2000)
                if(productDao.isExit(proId)!=null){
                    loadingDialog.show()
                    openCart()
                }else{
                    addToCart(productDao, proId, name, productSp,coverImg,carbon)
                }
            }


        }
    }

    private fun addToCart(productDao: ProductDao, proId: String, name: String?, productSp: String?, coverImg: String?, carbon: String?) {
        val data = ProductModel(proId,name,coverImg,productSp,carbon)
        lifecycleScope.launch(Dispatchers.IO){
            productDao.insertProduct(data)
            Thread.sleep(2000)
            binding.tvAddToCart.text = getString(R.string.goCart)
            loadingDialog.dismiss()
        }

    }

    private fun openCart() {
        val preference = requireContext().getSharedPreferences("info", AppCompatActivity.MODE_PRIVATE)
        val editor = preference.edit()
        editor.putBoolean("isCart",true)
        editor.apply()
        loadingDialog.dismiss()
        val intent = Intent(requireContext(),MainActivity::class.java)
        startActivity(intent)
    }
    fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
        else{
            Thread.sleep(2000)
        }
    }
}