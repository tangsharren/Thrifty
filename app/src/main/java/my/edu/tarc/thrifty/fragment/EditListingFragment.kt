package my.edu.tarc.thrifty.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.databinding.FragmentEditListingBinding
import my.edu.tarc.thrifty.model.CategoryModel
import my.edu.tarc.thriftyadmin.adapter.EditProductImageAdapter
import java.util.*
import kotlin.collections.ArrayList

class EditListingFragment : Fragment(){

    private lateinit var binding : FragmentEditListingBinding
    private lateinit var list: ArrayList<Uri>
    private lateinit var addedImagelist: ArrayList<Uri>
    private lateinit var listImages: ArrayList<String>
    private lateinit var adapter: EditProductImageAdapter
    private lateinit var preferences: SharedPreferences
    private val args : EditListingFragmentArgs by navArgs()
    private var newCoverImage: Uri? = null
    private var coverImage: Uri? = null
    private var newCoverImgUrl: String? = ""

    private lateinit var dialog: Dialog
    private lateinit var categoryList: ArrayList<String>
    //launch gallery to add cover image
    private var launchGalleryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            newCoverImage = it.data!!.data
            binding.productCoverImg.setImageURI(newCoverImage)
            binding.productCoverImg.isVisible = true
            binding.prodImgRecycler.isVisible = true
        }
    }
    //launch gallery to add product images
    private var launchProductActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val imageUrl = it.data!!.data
            list.add(imageUrl!!)
            addedImagelist.add(imageUrl)
            adapter.notifyDataSetChanged()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditListingBinding.inflate(layoutInflater)
        list = ArrayList()
        listImages = ArrayList()
        addedImagelist = ArrayList()

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)

        getListingDetails(args.productId)

        binding.btnCoverImg.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchGalleryActivity.launch(intent)
        }
        binding.btnProdImg.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchProductActivity.launch(intent)
        }
        //Store the user's updated details only if all fields are correct
        binding.btnUpdate.setOnClickListener {
            if(validateData())
                storeData()

            else(!validateData())
                Toast.makeText(requireContext(),R.string.wentWrong,Toast.LENGTH_SHORT).show()
        }


        return binding.root
    }
    private fun setProductCategory(previousCat:String) {
        categoryList = ArrayList()
        Firebase.firestore.collection("categories").get().addOnSuccessListener {
            categoryList.clear()
            for (doc in it.documents) {
                val data = doc.toObject(CategoryModel::class.java)
                categoryList.add(data!!.cat!!)
            }
            categoryList.add(0, "Select Category")

            val arrayAdapter =
                ArrayAdapter(
                    requireContext(),
                    com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                    categoryList
                )
            binding.catSpinner.adapter = arrayAdapter
            //set the selection of the category spinner based on the category previously saved
            when(previousCat){
                "Video Gaming "->binding.catSpinner.setSelection(1)
                "Photography"->binding.catSpinner.setSelection(2)
                "Tops"->binding.catSpinner.setSelection(3)
                "Footwear "->binding.catSpinner.setSelection(4)
                "Sports equipment "->binding.catSpinner.setSelection(5)
                "Babies & Kids"->binding.catSpinner.setSelection(6)
                "Bottom"->binding.catSpinner.setSelection(7)
                "Home Appliances "->binding.catSpinner.setSelection(8)
                "Furniture "->binding.catSpinner.setSelection(9)
            }
        }
    }
    private fun getListingDetails(proId:String?) {
        Firebase.firestore.collection("products")
            .document(proId!!).get().addOnSuccessListener {
                val oldProdImages = it.get("productImages") as ArrayList<String>
                val name = it.getString("productName")
                val productSp = it.getString("productSp")
                val productDesc = it.getString("productDescription")
                val productCarbon = it.getString("carbon")
                val productCat = it.getString("productCategory")
                coverImage = Uri.parse(it.getString("productCoverImg"))

                Glide.with(requireContext()).load(it.getString("productCoverImg")).into(binding.productCoverImg)
                Log.d("MyApp","oldProdImgs in getListingDetails:"+ oldProdImages.toString())

                adapter = EditProductImageAdapter(requireContext(),list,proId)
                binding.prodImgRecycler.adapter= adapter

                for (str in oldProdImages) {
                    list.add(Uri.parse(str))
                }

                binding.etName.setText(name)
                binding.etPrice.setText(productSp)
                binding.etDesc.setText(productDesc)
                binding.etCarbon.setText(productCarbon)


                setProductCategory(productCat!!)

            }.addOnFailureListener {
                Toast.makeText(requireContext(),R.string.wentWrong, Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadCoverImage() {
        dialog.show()
        val fileName = UUID.randomUUID().toString() + ".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("products/$fileName")
        refStorage.putFile(newCoverImage!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    newCoverImgUrl = image.toString()
//                    val newCoverImg = image
                    val db = Firebase.firestore.collection("products")
                    val key = args.productId
                    db.document(key).update("productCoverImg" ,newCoverImgUrl)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), getString(R.string.coverUpdate), Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .addOnFailureListener {
                            dialog.dismiss()
                            Toast.makeText(requireContext(), getString(R.string.coverError), Toast.LENGTH_SHORT).show()
                        }

                }
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), getString(R.string.coverStoreError), Toast.LENGTH_SHORT).show()
            }
    }

    private var i = 0
    private fun uploadProductImage() {
        dialog.show()
        val fileName = UUID.randomUUID().toString() + ".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("products/$fileName")
        refStorage.putFile(addedImagelist[i])
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    listImages.add(image!!.toString())
                    if (addedImagelist.size == listImages.size){
//                        val newProductImg = image
                        val db = Firebase.firestore.collection("products")
                        val key = args.productId
                        val docRef = db.document(key)

                        docRef.update("productImages", FieldValue.arrayUnion(*listImages.toTypedArray()))
                            .addOnSuccessListener {
                                Log.d("MyApp", "DocumentSnapshot successfully updated!")

                                dialog.dismiss()
                            }
                            .addOnFailureListener { e -> Log.w("MyApp", "Error updating document", e) }
                    }

                    else {
                        i += 1
                        uploadProductImage()
                    }
                }
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(
                    requireContext(), getString(R.string.prodImgError), Toast.LENGTH_SHORT).show()
            }
    }
    private fun validateData() :Boolean{
        if (binding.etName.text.toString().isEmpty()) {
            binding.etName.requestFocus()
            binding.etName.error = getString(R.string.empty)
            return false
        } else if (binding.etPrice.text.toString().isEmpty()) {
            binding.etPrice.requestFocus()
            binding.etPrice.error = getString(R.string.empty)
            return false
        } else if (coverImage == null){
            Toast.makeText(requireContext(), getString(R.string.selectCover), Toast.LENGTH_SHORT)
                .show()
            return false
        }

        else if (list.size < 1){
            Toast.makeText(requireContext(), getString(R.string.selectProd), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        else if (binding.catSpinner.selectedItemPosition == 0) {
            Toast.makeText(requireContext(), getString(R.string.plsSelectCat), Toast.LENGTH_SHORT)
                .show()
            return false

        } else if(newCoverImage !=null){
            uploadCoverImage()
        }

        else if(!addedImagelist.isEmpty()){
            Log.d("MyApp","addedImageList: "+addedImagelist.toString())
            uploadProductImage()

        }
        return true
    }
    private fun storeData() {
        val db = Firebase.firestore.collection("products")
        val key = args.productId
        categoryList = ArrayList()
        Firebase.firestore.collection("categories").get()

        val newName = binding.etName.text.toString()
        val newDesc = binding.etDesc.text.toString()
        val newCat = binding.catSpinner.selectedItem.toString()
        val newCarbon = binding.etCarbon.text.toString()
        val newPrice = binding.etPrice.text.toString()

        db.document(key).update(
            mapOf("productName" to newName,
                "productSp" to newPrice,
                "productDescription" to newDesc,
                "productCategory" to newCat,
                "carbon" to newCarbon))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), getString(R.string.updateListing), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), getString(R.string.wentWrong), Toast.LENGTH_SHORT).show()
            }
    }
}