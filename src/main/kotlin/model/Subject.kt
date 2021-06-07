package model

import kotlinx.serialization.*

@Serializable
data class Subject(
    @SerialName("name") val name: String,
    @SerialName("subject_number") val subject_number: List<String>,
    @SerialName("credits") val credits: Double,
    @SerialName("is_except") val is_except: Boolean
    //TODO 履修条件？
)