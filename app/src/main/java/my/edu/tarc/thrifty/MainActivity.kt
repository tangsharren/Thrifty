package my.edu.tarc.thrifty

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.thrifty.activity.LoginActivity
import my.edu.tarc.thrifty.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    var i = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        navController = navHostFragment!!.findNavController()
//        navController = this.findNavController(R.id.fragmentContainer)
        if(FirebaseAuth.getInstance().currentUser == null){
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
//            navController.navigate(R.id.loginFragment)
        }
        //Get current user email & name
        val email : String
        var name : String?
        var password : String?
        val user = FirebaseAuth.getInstance().getCurrentUser()
        user.let {
            email = it!!.email!!
            Log.d("MyApp",email)

        }

        Firebase.firestore.collection("users")
            .document(email)
            .get().addOnSuccessListener {
                name = it.getString("userName")
                password = it.getString("userPassword")
                //Set shared preferences
                val preferences = this.getSharedPreferences("user", MODE_PRIVATE)
                val editor = preferences.edit()
                editor.putString("email", email)
                editor.putString("name", name)
                editor.putString("password",password)
                editor.apply()
            }
            .addOnFailureListener {
                Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show()
            }

        val popupMenu = PopupMenu(this, null)
        popupMenu.inflate(R.menu.bottom_nav)
        binding.bottomBar.setupWithNavController(popupMenu.menu, navController)
        //To hide bottom bar in login and register
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment -> {
                    binding.bottomBar.visibility = View.GONE
                }
                R.id.registerFragment -> {
                    binding.bottomBar.visibility = View.GONE
                }
                else -> {
                    binding.bottomBar.visibility = View.VISIBLE
                }
            }
        }
        binding.bottomBar.onItemSelected = {
            when (it) {
                0 -> {
                    i = 0
                    navController.navigate(R.id.homeFragment)
//                    replaceFragment(HomeFragment())
                }
                1 ->
                    i = 1

                2 ->{
                    i = 2
                }
            }
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment = navController.currentDestination!!.id
                Log.d("MyApp", "current i : $i")
                if(currentFragment ==  R.id.homeFragment)
                    finish()
                else if(currentFragment ==  R.id.cartFragment ){
                    navController.navigate(R.id.homeFragment)
                    i = 0
                }
                else if(currentFragment ==  R.id.moreFragment){
                    navController.navigate(R.id.homeFragment)
                    i = 0
                }
                else {
                    navController.popBackStack()
                }
            }

        }
        NavigationUI.setupActionBarWithNavController(this,navController)
        onBackPressedDispatcher.addCallback(this, callback)
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }
}