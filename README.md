# scs-migration-checker (English)
An unofficial migration requirements checking tool for University of Tsukuba - School of Comprehension Studies faculty.  
You can use this tool at: https://itsu-dev.github.io/scs-migration-checker/

## Features
- Implemented in Kotlin/JS with Gradle

## rule_definitions.json
rule_definitions.json is used by this tool to define the migration requirements. Programs read this file and 
 check whether users' timetable adapts migration requirements each faculty defines.

### Format
- [rule_definitions.json](https://github.com/maru2213/scs-migration-checker/blob/master/src/main/kotlin/model/RuleDefinition.kt)
    - version : ```String``` Define version (e.g. 1.0.0)
    - updated_at : ```String``` Define last update date (e.g. 20210603)
    - author : ```String``` Define author
    - [faculties](https://github.com/maru2213/scs-migration-checker/blob/master/src/main/kotlin/model/Faculty.kt) : ```Array<Faculty>```
        - faculty_name : ```String``` Define faculty name. (e.g. 情報科学類)
        - [majors](https://github.com/maru2213/scs-migration-checker/blob/master/src/main/kotlin/model/Major.kt) : ```Array<Major>```
            - major_name : ```String``` Define major name. (e.g. ソフトウェアサイエンス)
            - credits_graduation : ```Integer``` Credits required for graduation 
            - [subject_types](https://github.com/maru2213/scs-migration-checker/blob/master/src/main/kotlin/model/SubjectType.kt) : ```Array<SubjectType>```
                - subject_type_name : ```String``` Define subject_type name. (専門科目/専門基礎科目/基礎科目-共通科目/基礎科目-関連科目)
                - [sub_subject_types](https://github.com/maru2213/scs-migration-checker/blob/master/src/main/kotlin/model/SubSubjectType.kt) : ```Array<SubSubjectType>```
                    - sub_subject_type_name : ```String``` Define sub_subject_type name. (必修科目/選択科目)
                    - credits_min : ```Integer``` Minimum credits of this sub_subject_type
                    - credtis_max : ```Integer``` Maximum credits of this sub_subject_type
                    - [subject_groups](https://github.com/maru2213/scs-migration-checker/blob/master/src/main/kotlin/model/SubjectGroup.kt) : ```Array<SubjectGroup>```
                        - description : ```String``` Define description of this subject_group.
                        - credits_min : ```Integer``` Minimum credits of this subject_group
                        - credits_max(Optional) : ```Integer``` Maximum credits of this subject_group
                        - [subjects](https://github.com/maru2213/scs-migration-checker/blob/master/src/main/kotlin/model/Subject.kt) : ```Array<Subject>```
                            - name(Optional) : ```String``` The subject's name (e.g. フレッシュマンセミナー)
                            - subject_numbers : ```Array<String>``` The subject's numbers
                            - except_subject_numbers : ```Array<String>``` The numbers of except subjects (〇〇は除く系)
                            - credits(Optional) : ```Double``` The subject's credits (e.g. 1.0)
    
#### Subject name
Subject name must be defined at `````/faculties/rules/subjects`````.
- You must write **the name of the subject, not ID.**
- If you want to specify unit of the subject, you can write ```::(UNIT)``` end of the name. (e.g. ```"微分積分A::2"```)
- If you don't specify unit of the subject, the subject unit will be processed as 1.
- If you want to specify subjects which content "ABC", you can write ```#CONTENTS:ABC```. (e.g. ```#CONTENTS:基礎体育```)
- If you want to specify whole subjects which don't include the required subjects, you can write ```#OTHER_SUBJECTS:(MAX_UNIT)```.
    
# 総合学域群 移行要件チェックツール 
筑波大学 総合学域群生向けの非公式移行要件チェックツールです。
自分の履修時間割をアップロードすることで判定ができます。
こちらから使用することができます：https://itsu-dev.github.io/scs-migration-checker/?lahl
