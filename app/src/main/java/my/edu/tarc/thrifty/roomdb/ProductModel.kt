package my.edu.tarc.thrifty.roomdb

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductModel (
    @PrimaryKey
    @NonNull
    val productId : String,
    @ColumnInfo("productName")
    val productName:String? = "" ,
    @ColumnInfo("productImage")
    val productImage:String? = "",
    @ColumnInfo("productSp")
    val productSp:String? = "",
    @ColumnInfo("carbon")
    val carbon:String? = "",
)