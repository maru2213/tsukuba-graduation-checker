package model.RuleDefinition

import kotlinx.serialization.*

@Serializable
data class SubSubjectType(
    @SerialName("sub_subject_type_name") val sub_subject_type_name: String,
    @SerialName("credits_min") val credits_min: Int,
    @SerialName("credits_max") val credits_max: Int,
    @SerialName("subject_groups") val subject_groups: List<SubjectGroup>
)
