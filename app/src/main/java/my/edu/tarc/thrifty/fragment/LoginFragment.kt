package my.edu.tarc.thrifty.fragment

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import my.edu.tarc.thrifty.MainActivity
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.activity.RegisterActivity
import my.edu.tarc.thrifty.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {
    private lateinit var binding : FragmentLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentLoginBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginEmail.setOnFocusChangeListener{ _,focused ->
            if(!focused) {
                binding.emailAddContainer.helperText = ""
            }
        }
        binding.loginPassword.setOnFocusChangeListener{ _,focused ->
            if(!focused) {
                binding.passwordContainer.helperText = ""
            }
        }
        binding.btnSignup.setOnClickListener {
//            startActivity(Intent(this, RegisterActivity::class.java))
//            finish()
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            findNavController().navigate(action)
        }
        binding.btnSignin.setOnClickListener {
            val email = binding.loginEmail.text.toString().lowercase()
            val password = binding.loginPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(requireContext(), "Logged in successfully", Toast.LENGTH_SHORT).show()

                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                        if(activity != null) {
                            activity?.finish()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Invalid email or incorrect password", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }


        binding.tvForgot.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val view = layoutInflater.inflate(R.layout.dialog_forgot, null)
            val userEmail = view.findViewById<EditText>(R.id.etEmail)

            builder.setView(view)
            val dialog = builder.create()

            view.findViewById<Button>(R.id.btnReset).setOnClickListener {
                if(view.findViewById<EditText>(R.id.etEmail).text.isNullOrEmpty()){
                    Toast.makeText(requireContext(),"Please provide your email address", Toast.LENGTH_SHORT).show()
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

        binding.btnSignup.setOnClickListener {
//            val signupIntent = Intent(this, RegisterActivity::class.java)
//            startActivity(signupIntent)
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            findNavController().navigate(action)
        }
        return binding.root
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
}