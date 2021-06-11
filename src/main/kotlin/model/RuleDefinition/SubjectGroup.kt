package model.RuleDefinition

import kotlinx.serialization.*

@Serializable
data class SubjectGroup(
    @SerialName("description") val description: String,
    @SerialName("credits_min") val credits_min: Int,
    @SerialName("credits_max") val credits_max: Int = Int.MAX_VALUE,
    @SerialName("subjects") val subjects: List<Subject>
)