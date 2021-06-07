package model

import kotlinx.serialization.*

@Serializable
data class Subject(
    @SerialName("name") val name: String = "",
    @SerialName("subject_number") val subject_number: List<String>,
    @SerialName("except_subject_numbers") val except_subject_numbers: List<String>,
    @SerialName("credits") val credits: Double = -1.0
    //TODO 履修条件？
)