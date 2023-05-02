package my.edu.tarc.thrifty.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.activity.AddressActivity
import my.edu.tarc.thrifty.activity.CheckoutActivity
import my.edu.tarc.thrifty.activity.LoginActivity
import my.edu.tarc.thrifty.databinding.FragmentMoreBinding
import java.util.*

class MoreFragment : Fragment() {

    private lateinit var binding: FragmentMoreBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var firebaseAuth: FirebaseAuth
    private var imageUrl: Uri? = null

    private var launchGalleryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            imageUrl = it.data!!.data
            binding.profilePic.setImageURI(imageUrl)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMoreBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.tvUpdateAdd.setOnClickListener {
            val addressIntent = Intent(context, AddressActivity::class.java)
            startActivity(addressIntent)
        }
        binding.tvViewOrders.setOnClickListener {
            it.findNavController().navigate(R.id.action_moreFragment_to_allOrderFragment)
        }
        preferences = requireActivity().getSharedPreferences("user", MODE_PRIVATE)
        val email = preferences.getString("email", "")!!
        binding.tvProfileEmail.text = email
        binding.tvProfileName.text = preferences.getString("name", "")

        binding.btnLogout.setOnClickListener {
            Toast.makeText(requireContext(),"You are logged out now...",Toast.LENGTH_SHORT).show()
            firebaseAuth.signOut()
            Log.d("MyApp",firebaseAuth.getCurrentUser().toString())
            val intent = Intent(requireContext(),LoginActivity::class.java)
            startActivity(intent)
            if(activity != null) {
                activity?.finish()
            }

        }
        binding.btnRemoveAcc.setOnClickListener {
            // Inflate your custom layout for the dialog
            val dialogView = layoutInflater.inflate(R.layout.dialog_reauthentic, null)

            // Create an AlertDialog builder and set the title and message
            val dialogBuilder = AlertDialog.Builder(requireContext())

            // Set your custom layout as the view of the dialog
            dialogBuilder.setView(dialogView)

            // Create and show the dialog
            val dialog = dialogBuilder.create()
            // Get the buttons from your custom layout
            val reauthenticateButton = dialogView.findViewById<Button>(R.id.btnReset)
            val cancelButton = dialogView.findViewById<Button>(R.id.btnCancel)

            // Set the onClickListener for the reauthenticate button
            reauthenticateButton.setOnClickListener {
                // Get the password from the EditText in your custom layout
                val passwordInput = dialogView.findViewById<TextInputEditText>(R.id.etPassword)
                if (passwordInput.text.isNullOrEmpty()){
                    Toast.makeText(requireContext(),"Please input password",Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                val password = passwordInput.text.toString()

                // Get the current user from Firebase
                val user = FirebaseAuth.getInstance().currentUser

                // Create a credential with the password
                val credential = EmailAuthProvider.getCredential(user!!.email!!, password)
                Toast.makeText(requireContext(), "Please wait...", Toast.LENGTH_SHORT).show()
                // Reauthenticate the user with the credential
                user!!.reauthenticate(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Reauthentication successful
                            Toast.makeText(requireContext(), "Removed user successfully", Toast.LENGTH_SHORT).show()
                            //https://www.youtube.com/watch?v=BfiYnovc6jU&t=9s
                            removeUser(email)
                            //https://www.youtube.com/watch?v=8XujhEbaHNQ
                            deletePerson(email)
                            dialog.dismiss()
                        } else {
                            // Reauthentication failed
                            Toast.makeText(requireContext(), "Reauthentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            passwordInput.text?.clear()
                        }
                    }
            }
            if (dialog.window != null) {
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            dialog.show()

            // Set the onClickListener for the cancel button
            cancelButton.setOnClickListener {
                // Dismiss the dialog
                dialog.dismiss()
            }
        }

        binding.tvChangePsw.setOnClickListener {
            //send reset psw email
            //https://www.youtube.com/watch?v=wbuCil83wC8&list=PLQ_Ai1O7sMV2_qzi0ra-eL4EX-vN3W2QW&index=10
            val builder = AlertDialog.Builder(requireContext())
            val view = layoutInflater.inflate(R.layout.dialog_forgot, null)
            val userEmail = view.findViewById<EditText>(R.id.etEmail)
            view.findViewById<TextView>(R.id.forgotTitle).setText("Update Password")

            builder.setView(view)
            val dialog = builder.create()

            view.findViewById<Button>(R.id.btnReset).setOnClickListener {
                if(view.findViewById<EditText>(R.id.etEmail).text.isNullOrEmpty()){
                    Toast.makeText(context,"Please provide your email address",Toast.LENGTH_SHORT).show()
                }
                else{
                    compareEmail(userEmail)
                    dialog.dismiss()
                }
            }
            view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }
            if (dialog.window != null) {
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            dialog.show()

        }
        binding.profilePic.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchGalleryActivity.launch(intent)
        }
        binding.tvUpload.setOnClickListener {
            validateData()
        }
        binding.tvViewListing.setOnClickListener {

        }
        return binding.root
    }
    private fun validateData() {
        if (imageUrl == null)
            Toast.makeText(requireContext(),"Please select image",Toast.LENGTH_SHORT).show()
        else
            uploadImage()
    }
    private fun uploadImage() {
        val fileName = UUID.randomUUID().toString() + ".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("products/$fileName")
        refStorage.putFile(imageUrl!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener {
                        image -> storeData(image.toString())

                    Toast.makeText(requireContext(),"Uploaded", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener{
                Toast.makeText(requireContext(),"Something went wrong with storage", Toast.LENGTH_SHORT).show()
            }
    }
    private fun storeData(url: String){
        val email : String
        val user = firebaseAuth.getCurrentUser()
        user.let {
            email = it!!.email!!
        }
        val data = hashMapOf<String, Any>(
            "img" to url
        )

        Firebase.firestore.collection("users")
            .document(email)
            .update(data).addOnSuccessListener {
//                binding.profilePic.setImageResource(R.drawable.profile)
                Toast.makeText(requireContext(),"Profile Pic Updated",Toast.LENGTH_SHORT).show()
            }

            .addOnFailureListener {
                Toast.makeText(requireContext(),"Something went wrong",Toast.LENGTH_SHORT).show()
            }
    }
    private fun compareEmail(email: EditText) {
        if (email.text.toString().isEmpty()) {
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
            return
        }
        firebaseAuth.sendPasswordResetEmail(email.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Check your email", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(requireContext(), "Email not found", Toast.LENGTH_SHORT).show()
                }
            }


    }
    private fun removeUser(email: String) {
        val user = firebaseAuth.getCurrentUser()
        user?.delete()?.addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(requireContext(),"Account deleted successfully! You will be logged out now...",Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(),LoginActivity::class.java)
                startActivity(intent)
                if(activity != null) {
                    activity?.finish()
                }
            }
            else{
                Toast.makeText(requireContext(),it.exception.toString(),Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deletePerson(email: String) = CoroutineScope(Dispatchers.IO).launch {
        val personCollectionRef = Firebase.firestore.collection("users")
        val personQuery = personCollectionRef
            .whereEqualTo("userEmail", email)
            .get()
            .await()
        if (personQuery.documents.isNotEmpty()) {
            for (document in personQuery) {
                try {
                    personCollectionRef.document(document.id).delete().await()

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "No users matched the email.", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}