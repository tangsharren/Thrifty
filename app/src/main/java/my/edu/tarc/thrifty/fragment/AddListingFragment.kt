package my.edu.tarc.thrifty.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.adapter.AddProductImageAdapter
import my.edu.tarc.thrifty.model.AddProductModel
import my.edu.tarc.thrifty.model.CategoryModel
import java.util.*
import kotlin.collections.ArrayList
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import my.edu.tarc.thrifty.databinding.FragmentAddListingBinding

class AddListingFragment : Fragment() {

    private lateinit var binding: FragmentAddListingBinding
    private lateinit var list: ArrayList<Uri>
    private lateinit var listImages: ArrayList<String>
    private lateinit var adapter: AddProductImageAdapter
    private var coverImage: Uri? = null
    private lateinit var dialog: Dialog
    private var coverImgUrl: String? = ""
    private lateinit var categoryList: ArrayList<String>
    private val args : AddListingFragmentArgs by navArgs()

    private var launchGalleryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            coverImage = it.data!!.data
            binding.productCoverImg.setImageURI(coverImage)
            binding.productCoverImg.isVisible = true
            binding.productImgRecyclerView.isVisible = true
        }
    }
    private var launchProductActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val imageUrl = it.data!!.data
            list.add(imageUrl!!)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddListingBinding.inflate(layoutInflater)

        list = ArrayList()
        listImages = ArrayList()

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)

        binding.selectCoverImg.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchGalleryActivity.launch(intent)
        }
        binding.productImgBtn.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchProductActivity.launch(intent)
        }

        setProductCategory()

        adapter = AddProductImageAdapter(list)
        binding.productImgRecyclerView.adapter = adapter

        binding.submitProductBtn.setOnClickListener {
            validateData()
        }
        return binding.root
    }

    private fun setProductCategory() {
        categoryList = ArrayList()
        Firebase.firestore.collection("categories").get().addOnSuccessListener {
            categoryList.clear()
            for (doc in it.documents) {
                val data = doc.toObject(CategoryModel::class.java)
                categoryList.add(data!!.cat!!)
            }
            Log.d("MyApp", "categoryList: $categoryList")
            categoryList.add(0, getString(R.string.selectCat))

            val arrayAdapter =
                ArrayAdapter(
                    requireContext(),
                    com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                    categoryList
                )
            arrayAdapter.setDropDownViewResource(com.google.android.material.R.layout.support_simple_spinner_dropdown_item)
            binding.productCategoryDropdown.adapter = arrayAdapter
            binding.productCategoryDropdown.setSelection(0)
            binding.productCategoryDropdown.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedCategory = parent?.getItemAtPosition(position).toString()
                        Log.d("MyApp", "selectedCategory: $selectedCategory")
                        // Do something with the selected category
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Do something when nothing is selected
                    }

                }
        }
    }

    private fun validateData() {
        if (binding.productNameEdt.text.toString().isEmpty()) {
            binding.productNameEdt.requestFocus()
            binding.productNameEdt.error = "Empty"
        } else if (binding.productSpEdt.text.toString().isEmpty()) {
            binding.productSpEdt.requestFocus()
            binding.productSpEdt.error = "Empty"
        } else if (coverImage == null)
            Toast.makeText(requireContext(), getString(R.string.selectCover), Toast.LENGTH_SHORT)
                .show()
        else if (list.size < 1)
            Toast.makeText(requireContext(), getString(R.string.selectProd), Toast.LENGTH_SHORT)
                .show()
        else if (binding.productCategoryDropdown.selectedItemPosition == 0) {
            Toast.makeText(requireContext(), getString(R.string.selectWarn), Toast.LENGTH_SHORT)
                .show()

        } else
            uploadImage()
    }

    private fun uploadImage() {
        dialog.show()
        val fileName = UUID.randomUUID().toString() + ".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("products/$fileName")
        refStorage.putFile(coverImage!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    coverImgUrl = image.toString()
                    uploadProductImage()
                }
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.storageError),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun uploadProductImage() {
        dialog.show()
        val fileName = UUID.randomUUID().toString() + ".jpg"
        var i = 0
        val refStorage = FirebaseStorage.getInstance().reference.child("products/$fileName")
        refStorage.putFile(list[i])
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    listImages.add(image!!.toString())
                    if (list.size == listImages.size)
                        storeData()
                    else {
                        i += 1
                        uploadProductImage()
                    }
                }
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(
                    requireContext(),getString(R.string.storageError),Toast.LENGTH_SHORT).show()
            }
    }

    private fun storeData() {
        val db = Firebase.firestore.collection("products")
        val key = db.document().id
        categoryList = ArrayList()
        Firebase.firestore.collection("categories").get()
        val data = AddProductModel(
            binding.productNameEdt.text.toString(),
            binding.productDescriptionEdt.text.toString(),
            coverImgUrl.toString(),
            binding.productCategoryDropdown.selectedItem.toString(),
            key,
            binding.etCarbon.text.toString(),
            binding.productSpEdt.text.toString(),
            listImages,
            args.email,
            )
        db.document(key).set(data)
            .addOnSuccessListener {
                binding.productCoverImg.isVisible = false
                binding.productImgRecyclerView.isVisible = false
                //To delete last added product's img
                list.clear()
                binding.productSpEdt.text!!.clear()
                binding.productNameEdt.text!!.clear()
                binding.productDescriptionEdt.text!!.clear()
                binding.etCarbon.text!!.clear()
                binding.productCategoryDropdown.setSelection(0)
                dialog.dismiss()
                Toast.makeText(requireContext(), getString(R.string.addedProd), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), getString(R.string.wentWrong), Toast.LENGTH_SHORT)
                    .show()
            }
    }
}


