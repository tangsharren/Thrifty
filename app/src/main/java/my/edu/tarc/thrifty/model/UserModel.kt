package my.edu.tarc.thrifty.model

data class UserModel (
    val userName: String? = "",
    val userEmail : String? = "",
    val userPassword : String? = "",
    val village : String? = "",
    val state : String? = "",
    val city : String? = "",
    val pinCode : String? = ""
)