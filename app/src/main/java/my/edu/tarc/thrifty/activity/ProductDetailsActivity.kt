package my.edu.tarc.thrifty.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.edu.tarc.thrifty.MainActivity
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.databinding.ActivityProductDetailsBinding
import my.edu.tarc.thrifty.roomdb.AppDatabase
import my.edu.tarc.thrifty.roomdb.ProductDao
import my.edu.tarc.thrifty.roomdb.ProductModel

class ProductDetailsActivity : AppCompatActivity() {
    private lateinit var binding:ActivityProductDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)

        getProductDetails(intent.getStringExtra("id"))
        setContentView(binding.root)
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
                binding.tvPrice.text = productSp
                binding.tvDesc.text = productDesc
//                binding.tvCarbon.text = productCarbon + "kg of carbon footprint will be saved!"
                binding.tvCarbon.text = productCarbon
                Log.d("MyApp",list.toString())
                val slideList = ArrayList<SlideModel>()
                for(data in list){
                    slideList.add(SlideModel(data,ScaleTypes.CENTER_CROP))
                }

                cartAction(proId,name,productSp,it.getString("productCoverImg"),productCarbon)
                binding.imageSlider.setImageList(slideList)
            }.addOnFailureListener {
                Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show()
            }
    }

    private fun cartAction(proId: String, name: String?, productSp: String?, coverImg: String?,carbon: String?) {
        val productDao = AppDatabase.getInstance(this).productDao()
        if(productDao.isExit(proId)!=null)
            binding.tvAddToCart.text = "Go to Cart"
        else
            binding.tvAddToCart.text = "Add to Cart"
        binding.tvAddToCart.setOnClickListener{
            if(productDao.isExit(proId)!=null){
                openCart()
            }else{
                addToCart(productDao, proId, name, productSp,coverImg,carbon)
            }
        }
    }

    private fun addToCart(productDao: ProductDao, proId: String, name: String?, productSp: String?, coverImg: String?,carbon: String?) {
        val data = ProductModel(proId,name,coverImg,productSp,carbon)
        Log.d("MyApp",carbon!!)
        lifecycleScope.launch(Dispatchers.IO){
            productDao.insertProduct(data)
            binding.tvAddToCart.text = "Go to Cart"
        }
    }

    private fun openCart() {
       val preference = this.getSharedPreferences("info", MODE_PRIVATE)
        val editor = preference.edit()
        editor.putBoolean("isCart",true)
        editor.apply()

        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }
}