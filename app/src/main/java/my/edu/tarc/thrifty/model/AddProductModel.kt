package my.edu.tarc.thrifty.model

//These properties might be useful for displaying more information about the product or identifying the user who added it.
// However, they might not be necessary for storing the product in the database, which is why the ProductModel has fewer properties.
data class AddProductModel(
    val productName :String? = "",
    val productDescription: String? = "",
    val productCoverImg: String?= "",
    val productCategory: String?= "",
    val productId: String?= "",
    val carbon: String?= "",
    val productSp: String?= "",
    val productImages: ArrayList<String>  = ArrayList(),
    val userEmail: String?="",
    val availability:String?=""
    )