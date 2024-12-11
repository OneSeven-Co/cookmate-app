import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Ingredient(
    val amount: Float = 0f,
    val unit: String? = "",
    val name: String = "",
    val substitutes: List<String>? = emptyList()
) : Parcelable {
    override fun describeContents(): Int = 0
} 