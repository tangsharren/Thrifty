package my.edu.tarc.thrifty.fragment

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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
        getProfilePic()

        binding.tvUpdateAdd.setOnClickListener {
            it.findNavController().navigate(R.id.action_moreFragment_to_addressFragment)

        }
        binding.tvViewOrders.setOnClickListener {
            it.findNavController().navigate(R.id.action_moreFragment_to_allOrderFragment)
        }
        preferences = requireActivity().getSharedPreferences("user", MODE_PRIVATE)
        val email = preferences.getString("email", "")!!
        binding.tvProfileEmail.text = email
        binding.tvProfileName.text = preferences.getString("name", "")

        binding.btnLogout.setOnClickListener {
            Toast.makeText(requireContext(),getString(R.string.loggedOut),Toast.LENGTH_SHORT).show()
            firebaseAuth.signOut()
            Log.d("MyApp",firebaseAuth.getCurrentUser().toString())
            val intent = Intent(requireContext(),LoginActivity::class.java)
            startActivity(intent)
            if(activity != null) {
                activity?.finish()
            }

        }
        //Reauthentication is required before removing account
        binding.btnRemoveAcc.setOnClickListener {
            // Inflate custom layout for the dialog
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
                // Get the password from the EditText in custom layout
                val passwordInput = dialogView.findViewById<TextInputEditText>(R.id.etPassword)
                if (passwordInput.text.isNullOrEmpty()){
                    Toast.makeText(requireContext(),getString(R.string.inputPsw),Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                val password = passwordInput.text.toString()

                // Get the current user from Firebase
                val user = FirebaseAuth.getInstance().currentUser

                // Create a credential with the password
                val credential = EmailAuthProvider.getCredential(user!!.email!!, password)
                Toast.makeText(requireContext(), getString(R.string.plsWait), Toast.LENGTH_SHORT).show()
                // Reauthenticate the user with the credential
                user.reauthenticate(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Reauthentication successful
                            Toast.makeText(requireContext(), getString(R.string.removeUser), Toast.LENGTH_SHORT).show()
                            removeUser(email)
                            deletePerson(email)
                            dialog.dismiss()
                        } else {
                            // Reauthentication failed
                            Toast.makeText(requireContext(), getString(R.string.failedAuthen) +task.exception?.message, Toast.LENGTH_SHORT).show()
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
        //Reset password email will be sent
        binding.tvChangePsw.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val view = layoutInflater.inflate(R.layout.dialog_forgot, null)
            view.findViewById<TextView>(R.id.forgotTitle).setText("Reset Password")
            view.findViewById<Button>(R.id.btnReset).setText("Confirm")
            view.findViewById<EditText>(R.id.etEmail).isVisible = false
            view.findViewById<TextView>(R.id.desc).setText("Are you sure you want to reset password?")
            builder.setView(view)
            val dialog = builder.create()

            view.findViewById<Button>(R.id.btnReset).setOnClickListener {
                sendEmail(email)
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
            val action = MoreFragmentDirections.actionMoreFragmentToListingFragment(email)
            findNavController().navigate(action)
        }
        return binding.root
    }

    private fun validateData() {
        if (imageUrl == null)
            Toast.makeText(requireContext(),getString(R.string.selectImg),Toast.LENGTH_SHORT).show()
        else
            uploadImage()
    }
    private fun uploadImage() {
        val fileName = UUID.randomUUID().toString() + ".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("users/$fileName")
        refStorage.putFile(imageUrl!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener {
                        image -> storeData(image.toString())

                    Toast.makeText(requireContext(),getString(R.string.uploaded), Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener{
                Toast.makeText(requireContext(),getString(R.string.wentWrong), Toast.LENGTH_SHORT).show()
            }
    }
    private fun storeData(url: String){
        preferences = requireActivity().getSharedPreferences("user", MODE_PRIVATE)
        val email = preferences.getString("email", "")!!
        val data = hashMapOf<String, Any>(
            "img" to url
        )

        Firebase.firestore.collection("users")
            .document(email)
            .update(data).addOnSuccessListener {
                Toast.makeText(requireContext(),getString(R.string.updatedPic),Toast.LENGTH_SHORT).show()
                getProfilePic()
            }

            .addOnFailureListener {
                Toast.makeText(requireContext(),getString(R.string.wentWrong),Toast.LENGTH_SHORT).show()
            }
    }
    private fun getProfilePic() :Int{
        var gotProfilePic = 0
        preferences = requireActivity().getSharedPreferences("user", MODE_PRIVATE)
        val email = preferences.getString("email", "")!!
        Firebase.firestore.collection("users").document(email)
            .get().addOnSuccessListener {
                Glide.with(requireContext()).load(it.get("img")).into(binding.profilePic)
                gotProfilePic = 1
            }
            .addOnFailureListener {
                Log.d("MyApp","No profile pic")
            }
        return gotProfilePic
    }
    private fun sendEmail(email: String) {
        if (email.isEmpty()) {
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return
        }
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    Toast.makeText(requireContext(), getString(R.string.checkEmail), Toast.LENGTH_SHORT).show()
                else{
                    Toast.makeText(requireContext(), getString(R.string.noEmail), Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun removeUser(email: String) {
        val user = firebaseAuth.getCurrentUser()
        user?.delete()?.addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(requireContext(),getString(R.string.deletedAcc),Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), getString(R.string.noEmailExist), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}