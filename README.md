[日本語READMEはこちら](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/README_ja.md)

# tsukuba-graduation-checker README(en)
An unofficial graduation criteria checking tool for University of Tsukuba.

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
                        - [subjects](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/src/main/kotlin/model/Subject.kt) : `List<Subject>`
                            - name(Optional) : `String` The subject's name (e.g. フレッシュマン・セミナー)
                            - subject_numbers : `List<String>` The subject's numbers
                            - except_subject_numbers : `List<String>` The numbers of except subjects (e.g. 「〇〇は除く」)
                            - credits(Optional) : `Double` The subject's credits (e.g. 1.0)

#### About "Optional" elements
- `subject_groups/credits_max` : If this element is omitted, it will be `Int.MAX_VALUE`. It means the `subject_groups` has no limit.
- `subjects/name` : You can omit this element **only** if the parent `subject_group` element has one subject.
- `subjects/credits` : You had better write this element when the subject can take only one credit value.

## License
This software is released under the MPL-2.0 License, see "[LICENSE](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/LICENSE)".

This software is based on [scs-migration-checker](https://github.com/itsu-dev/scs-migration-checker) by [itsu-dev](https://github.com/itsu-dev) and [Mimori256](https://github.com/Mimori256).

This software uses [a script](https://github.com/Make-IT-TSUKUBA/alternative-tsukuba-kdb/tree/09569d959f23a89071c382bda69df6dae5d2e295) downloading a csv data from KdB, which is created by [inaniwaudon](https://github.com/inaniwaudon) and [Mimori256](https://github.com/Mimori256).
