package model

import kotlinx.serialization.*

@Serializable
data class Kdb(
    @SerialName("updated") val updated: String,
    @SerialName("subject") val subject: List<List<String>>
)
