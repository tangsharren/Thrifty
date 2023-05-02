package my.edu.tarc.thrifty.model


data class AddProductModel(
    val productName :String? = "",
    val productDescription: String? = "",
    val productCoverImg: String?= "",
    val productCategory: String?= "",
    val productId: String?= "",
    val carbon: String?= "",
    val productSp: String?= "",
    val productImages: ArrayList<String>  = ArrayList()
    )