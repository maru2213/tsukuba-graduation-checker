# tsukuba-graduation-checker README(en)
An unofficial graduation criteria checking tool for University of Tsukuba - College of Information Science.  
You can use this tool at: https://maru2213.github.io/tsukuba-graduation-checker/

## Features
- Implemented in Kotlin/JS with Gradle

## rule_definitions.json
rule_definitions.json used by this tool defines the criteria for graduation.
This tool read this file and check whether inputted timetable meets the criteria.

### Format
- [rule_definitions.json](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/src/main/kotlin/model/RuleDefinition.kt)
    - version : `String` Define version (e.g. 1.0.0)
    - updated_at : `String` Define last update date (e.g. 20210603)
    - author : `String` Define author
    - [faculties](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/src/main/kotlin/model/Faculty.kt) : `List<Faculty>`
        - faculty_name : `String` Define faculty name. (e.g. 情報科学類)
        - [majors](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/src/main/kotlin/model/Major.kt) : `List<Major>`
            - major_name : `String` Define major name. (e.g. ソフトウェアサイエンス)
            - credits_graduation : `Integer` Credits required for graduation 
            - [subject_types](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/src/main/kotlin/model/SubjectType.kt) : `List<SubjectType>`
                - subject_type_name : `String` Define subject_type name. (専門科目/専門基礎科目/基礎科目-共通科目/基礎科目-関連科目)
                - [sub_subject_types](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/src/main/kotlin/model/SubSubjectType.kt) : `List<SubSubjectType>`
                    - sub_subject_type_name : `String` Define sub_subject_type name. (必修科目/選択科目)
                    - credits_min : `Integer` Minimum credits of this sub_subject_type
                    - credtis_max : `Integer` Maximum credits of this sub_subject_type
                    - [subject_groups](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/src/main/kotlin/model/SubjectGroup.kt) : `List<SubjectGroup>`
                        - description : `String` Define description(or name) of this subject_group.
                        - credits_min : `Integer` Minimum credits of this subject_group
                        - credits_max(Optional) : `Integer` Maximum credits of this subject_group
                        - [subjects](https://github.com/maru2213/tsukuba-graduationn-checker/blob/master/src/main/kotlin/model/Subject.kt) : `List<Subject>`
                            - name(Optional) : `String` The subject's name (e.g. フレッシュマン・セミナー)
                            - subject_numbers : `List<String>` The subject's numbers
                            - except_subject_numbers : `List<String>` The numbers of except subjects (〇〇は除く系)
                            - credits(Optional) : `Double` The subject's credits (e.g. 1.0)

#### About "Optional" elements
- `subject_groups/credits_max` : If this element is omitted, it will be `Int.MAX_VALUE`. It means the `subject_groups` has no limit.
- `subjects/name` : You can omit this element **only** if the parent `subject_groups` element has one subject.
- `subjects/credits` : You had better write this element when the subject can take only one credit value.
    
<!--
#### Subject name
Subject name must be defined at `/faculties/majors/subject_types/sub_subject_types/subject_groups/subjects`.
- You must write **the name of the subject, not ID.**
- If you want to specify unit of the subject, you can write `::(UNIT)` end of the name. (e.g. `"微分積分A::2"`)
- If you don't specify unit of the subject, the subject unit will be processed as 1.
- If you want to specify subjects which content "ABC", you can write `#CONTENTS:ABC`. (e.g. `#CONTENTS:基礎体育`)
- If you want to specify whole subjects which don't include the required subjects, you can write `#OTHER_SUBJECTS:(MAX_UNIT)`.
-->
