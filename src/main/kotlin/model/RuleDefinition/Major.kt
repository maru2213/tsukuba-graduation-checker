package model.RuleDefinition

import kotlinx.serialization.*

@Serializable
data class Major(
    @SerialName("major_name") val major_name: String,
    @SerialName("credits_graduation") val credits_graduation: Int,
    @SerialName("subject_types") val subject_types: List<SubjectType>
)