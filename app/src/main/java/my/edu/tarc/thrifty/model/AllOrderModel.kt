package my.edu.tarc.thrifty.model

data class AllOrderModel(
    val name :String? = "",
    val orderId: String?= "",
    val userId: String?= "",
    val status: String?= "",
    val productId: String?= "",
    val price: String?= "",
    val carbon: String?= "",
    val orderDate: String?="",
    val orderTime:String?="",
)