[English README](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/README.md)

# 筑波大学 卒業要件チェックツール README(ja)

筑波大学の卒業要件をチェックする非公式のツールです。

右のURLにアクセスすることでこのツールを使用することができます: https://maru2213.github.io/tsukuba-graduation-checker/

## Features
- Implemented in Kotlin/JS with Gradle

## rule_definitions.json
卒業に必要な単位数などの条件はrule_definitions.jsonに定義されています。

このツールは、入力された科目のリストがrule_definitions.jsonで定められている定義を満たすかどうかの判定を行います。

### フォーマット
- [rule_definitions.json](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/src/main/kotlin/model/RuleDefinition.kt)
    - version : `String` JSONファイルのバージョン (e.g. 1.0.0)
    - updated_at : `String` JSONファイルの最終更新日 (e.g. 20210603)
    - author : `String` JSONファイルの製作者
    - [faculties](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/src/main/kotlin/model/Faculty.kt) : `List<Faculty>`
        - faculty_name : `String` 学類・専門学群の名称 (e.g. 情報科学類)
        - [majors](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/src/main/kotlin/model/Major.kt) : `List<Major>`
            - major_name : `String` 主専攻の名称 (e.g. ソフトウェアサイエンス)
            - credits_graduation : `Integer` 卒業に必要な単位数
            - [subject_types](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/src/main/kotlin/model/SubjectType.kt) : `List<SubjectType>`
                - subject_type_name : `String` subject_typeの名称 (専門科目/専門基礎科目/基礎科目-共通科目/基礎科目-関連科目)
                - [sub_subject_types](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/src/main/kotlin/model/SubSubjectType.kt) : `List<SubSubjectType>`
                    - sub_subject_type_name : `String` sub_subject_typeの名称 (必修科目/選択科目)
                    - credits_min : `Integer` このsub_subject_typeにおいて卒業に必要な最低単位数
                    - credtis_max : `Integer` このsub_subject_typeにおいて卒業に必要な単位として算入される最高単位数
                    - [subject_groups](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/src/main/kotlin/model/SubjectGroup.kt) : `List<SubjectGroup>`
                        - description : `String` subject_groupの名称もしくは説明
                        - credits_min : `Integer` このsubject_groupにおいて卒業に必要な最低単位数
                        - credits_max(Optional) : `Integer` このsubject_groupにおいて卒業に必要な単位として算入される最高単位数
                        - [subjects](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/src/main/kotlin/model/Subject.kt) : `List<Subject>`
                            - name(Optional) : `String` この科目の名称 (e.g. フレッシュマン・セミナー)
                            - subject_numbers : `List<String>` この科目の科目番号
                            - except_subject_numbers : `List<String>` 除外する科目の科目番号 (〇〇は除く系)
                            - credits(Optional) : `Double` この科目の単位数 (e.g. 1.0)

#### "Optional"な要素について
- `subject_groups/credits_max` : この要素がない場合、内部的には`Int.MAX_VALUE`として扱われ、その`subject_groups`は最高単位数の制限がないものとされます。
- `subjects/name` : 親の`subject_groups`要素が`subjects`にこの科目一つのみしか含まない場合にのみ省略できます。
- `subjects/credits` : 省略可能ですが、パフォーマンスが落ちてしまうのでできるだけ書いてください。

## ライセンス
このソフトウェアはMPL-2.0 Licenseでライセンスされています。[LICENSE](https://github.com/maru2213/tsukuba-graduation-checker/blob/master/LICENSE)もご覧ください。

このソフトウェアは[itsu-dev](https://github.com/itsu-dev)氏および[Mimori256](https://github.com/Mimori256)氏制作の[scs-migration-checker](https://github.com/itsu-dev/scs-migration-checker)を基にしています。
