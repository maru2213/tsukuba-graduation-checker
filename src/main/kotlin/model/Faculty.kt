package model

import kotlinx.serialization.*

@Serializable
data class Faculty(
    @SerialName("faculty_name") val facultyName: String,
    @SerialName("majors") val majors: Array<Major>
)
