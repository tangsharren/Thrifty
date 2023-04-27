package my.edu.tarc.thrifty.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.UserManager
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.MainActivity
import my.edu.tarc.thrifty.databinding.ActivityRegisterBinding
import my.edu.tarc.thrifty.model.UserModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)


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
//                                val intent = Intent(this, LoginActivity::class.java)
//                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "This email address is registered. Please login ", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Password does not matched", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
//            storeData()
        }
    }

//    private fun validateUser() {
//        if (binding.userName.text!!.isEmpty() || binding.userNumber.text!!.isEmpty())
//            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
//        else
//            storeData()
//    }
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
        val builder = AlertDialog.Builder(this)
            .setTitle("Loading")
            .setMessage("Please Wait")
            .setCancelable(false)
            .create()
        builder.show()

        val preferences = this.getSharedPreferences("user", MODE_PRIVATE)
        val editor = preferences.edit()

        editor.putString("email", binding.regEmail.text.toString().lowercase())
        editor.putString("name", binding.userName.text.toString())
        editor.apply()
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
                Toast.makeText(this, "Registered & Signed In As $email", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                //openLogin()

            }
            .addOnFailureListener {
                builder.dismiss()
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openLogin() {
//        val loginIntent = Intent(this, LoginActivity::class.java)
//        startActivity(loginIntent)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}