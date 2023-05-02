package my.edu.tarc.thrifty.activity

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import my.edu.tarc.thrifty.MainActivity
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
        binding.btnSignin.setOnClickListener {
            val email = binding.loginEmail.text.toString().lowercase()
            val password = binding.loginPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
//                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "Invalid email or incorrect password", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }


        binding.tvForgot.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_forgot, null)
            val userEmail = view.findViewById<EditText>(R.id.etEmail)

            builder.setView(view)
            val dialog = builder.create()

            view.findViewById<Button>(R.id.btnReset).setOnClickListener {
                if(view.findViewById<EditText>(R.id.etEmail).text.isNullOrEmpty()){
                    Toast.makeText(this,"Please provide your email address",Toast.LENGTH_SHORT).show()
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
            val signupIntent = Intent(this, RegisterActivity::class.java)
            startActivity(signupIntent)
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
                    Toast.makeText(this, "Check your email", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
                }
            }


    }
}