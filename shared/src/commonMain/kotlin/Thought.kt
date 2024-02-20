import kotlinx.serialization.Serializable

@Serializable
data class Thought(
    val id: Int,
    val content: String,
    val author: String,
    val score: Int
)
