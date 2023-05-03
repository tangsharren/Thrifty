package my.edu.tarc.thrifty.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.MainActivity
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.activity.LoginActivity
import my.edu.tarc.thrifty.databinding.FragmentLoginBinding
import my.edu.tarc.thrifty.databinding.FragmentRegisterBinding
import my.edu.tarc.thrifty.model.UserModel


class RegisterFragment : Fragment() {
    private lateinit var binding : FragmentRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentRegisterBinding.inflate(layoutInflater)


        firebaseAuth = FirebaseAuth.getInstance()

        binding.login.setOnClickListener {
            openLogin()
        }
        binding.userName.setOnFocusChangeListener{ _,focused ->
            if(!focused) {
                binding.nameContainer.helperText = ""
            }
        }
        binding.regPassword.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                binding.pswContainer.helperText = validPassword()
            }
        }
        binding.regEmail.setOnFocusChangeListener { _, focused ->
            val email = binding.regEmail.text.toString()
            if(!focused) {
                binding.emailContainer.helperText = validEmail()
            }

        }
        binding.confirmContainer.setOnFocusChangeListener{ _,focused ->
            if(!focused) {
                binding.confirmContainer.helperText = ""
            }
        }


        binding.signup.setOnClickListener {
            val email = binding.regEmail.text.toString().lowercase()
            val password = binding.regPassword.text.toString()
            val confirmPassword = binding.regCfmPassword.text.toString()

            binding.emailContainer.helperText = validEmail()
            binding.pswContainer.helperText = validPassword()


            val validEmail = binding.emailContainer.helperText == null
            val validPassword = binding.pswContainer.helperText == null

            if (validEmail && validPassword &&email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {

                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                storeData()

                            } else {
                                Toast.makeText(requireContext(), "This email address is registered. Please login ", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "Password does not matched", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }
    private fun validEmail(): String? {
        val email = binding.regEmail.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid Email Address"
        }
        return null
    }
    private fun validPassword(): String? {
        val passwordText = binding.regPassword.text.toString()
        if(passwordText.length < 8)
        {
            return "Minimum 8 Character Password"
        }
        if(!passwordText.matches(".*[A-Z].*".toRegex()))
        {
            return "Must Contain 1 Upper-case Character"
        }
        if(!passwordText.matches(".*[a-z].*".toRegex()))
        {
            return "Must Contain 1 Lower-case Character"
        }
        if(!passwordText.matches(".*[@#\$%^&+=].*".toRegex()))
        {
            return "Must Contain 1 Special Character (@#\$%^&+=)"
        }

        return null
    }
    private fun storeData() {
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Loading")
            .setMessage("Please Wait")
            .setCancelable(false)
            .create()
        builder.show()

        val data = UserModel(
            userName = binding.userName.text.toString(),
            userEmail = binding.regEmail.text.toString().lowercase(),
            userPassword = binding.regCfmPassword.text.toString()
        )

        Firebase.firestore.collection("users").document(binding.regEmail.text.toString().lowercase())
            .set(data).addOnSuccessListener {
//                Toast.makeText(this, "User registered", Toast.LENGTH_SHORT).show()
                val user = FirebaseAuth.getInstance().getCurrentUser()
                var email:String? = ""
                user?.let {
                    email = it.email
                    Log.d("MyApp",email!!)
                }
                builder.dismiss()
                Toast.makeText(requireContext(), "Registered & Signed In As $email", Toast.LENGTH_SHORT).show()

//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)

//                val action = RegisterFragmentDirections.actionRegisterFragmentToHomeFragment()
//                findNavController().navigate(action)

                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                if(activity != null) {
                    activity?.finish()
                }

            }
            .addOnFailureListener {
                builder.dismiss()
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openLogin() {
//        startActivity(Intent(this, LoginActivity::class.java))
//        finish()
        val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
        findNavController().navigate(action)
    }
}