package model

import kotlinx.serialization.*

@Serializable
data class SubjectType(
    @SerialName("subject_type_name") val subject_type_name: String,
    @SerialName("sub_subject_types") val sub_subject_types: List<SubSubjectType>
)