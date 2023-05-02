package my.edu.tarc.thrifty.model

data class UserModel (
    val userName: String? = "",
    val userEmail : String? = "",
    val userPassword : String? = "",
    val unitNo : String? = "",
    val state : String? = "",
    val street : String? = "",
    val postcode : String? = "",
    val profilePic: String?= "",
)