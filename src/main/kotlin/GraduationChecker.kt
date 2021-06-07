import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import model.*
import org.w3c.dom.Attr
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.SelectionMode
import org.w3c.dom.events.EventListener
import org.w3c.fetch.Request

object GraduationChecker {

    private lateinit var ruleDefinitions: RuleDefinition
    private var isChecking = false

    // rule_definitions.jsonを読み込む
    fun loadRuleDefinitions() {
        //TODO URL変更
        window.fetch(Request("https://maru2213.github.io/private-json/rule_definitions.json"))
            .then(onFulfilled = {
                it.text().then { json ->
                    onLoadFinished(json)
                }
            })
    }

    private fun onLoadFinished(json: String) {
        ruleDefinitions = Json.decodeFromString(RuleDefinition.serializer(), json)
        console.log("[Rule Definitions] Version: ${ruleDefinitions.version} Last Updated At: ${ruleDefinitions.updatedAt}")

        val facultySelect = document.getElementById("faculty") ?: run {
            //TODO 文言これでいい？
            window.alert("エラーが発生しました")
            return
        }
        facultySelect.innerHTML += """<option value="null">選択してください</option>"""
        ruleDefinitions.faculties.forEach { faculty ->
            facultySelect.innerHTML += "<option>${faculty.facultyName}</option>"
        }

        document.getElementById("faculty")?.addEventListener("input", EventListener { event ->
            val majorSelect = document.getElementById("major") ?: run {
                //TODO 文言これでいい？
                window.alert("エラーが発生しました")
                return@EventListener
            }

            val selectedValue = (event.target as HTMLSelectElement).value
            if (selectedValue == "null") {
                majorSelect.innerHTML = "";
                return@EventListener
            }

            ruleDefinitions.faculties.forEach { faculty ->
                if (selectedValue != faculty.facultyName) {
                    //continueみたいなモノ
                    return@forEach
                }

                majorSelect.innerHTML += """<option value="null">選択してください</option>"""
                faculty.majors.forEach { major ->
                    majorSelect.innerHTML += "<option>${major.major_name}</option>"
                }
                return@EventListener
            }
        })
    }

    // 移行要件をチェックする
    // userSubjects: ユーザの登録済み講義 <講義名, 単位>
    private fun check(userSubjects: Map<String, Double>, major: Major) {

        if (isChecking) {
            window.alert("判定中です")
            return
        }

        isChecking = true

        console.log(userSubjects)
        console.log(major)

        //4重ループをぶん回す
        major.subject_types.forEachIndexed { index1, subjectType ->
            subjectType.sub_subject_types.forEachIndexed { index2, subSubjectType ->
                subSubjectType.subject_groups.forEachIndexed { index3, subjectGroup ->
                    subjectGroup.subjects.forEachIndexed { index4, subject ->
                        /*
                        index1, subjectType : 専門科目 etc.
                        index2, subSubjectType : 必修科目 etc.
                        index3, subjectGroup : E,F,G,H... etc.
                        index4, subject : 確率論 etc.
                         */

                        val tr = document.createElement("tr")
                        document.getElementById("result")!!.appendChild(tr)

                        if (index4 == 0) {
                            if (index3 == 0) {
                                if (index2 == 0) {
                                    val childCount1 = countChildSubject(subjectType)
                                    console.log("1:" + subject.name + ", " + childCount1)
                                    if (childCount1 != 0) {
                                        val group1_td = document.createElement("td").also {
                                            it.innerHTML = subjectType.subject_type_name
                                            it.setAttribute("rowspan", childCount1.toString())
                                        }
                                        tr.appendChild(group1_td)
                                    }
                                }

                                val childCount2 = countChildSubject(subSubjectType)
                                console.log("2:" + subject.name + ", " + childCount2)
                                if (childCount2 != 0) {
                                    val group2_td = document.createElement("td").also {
                                        it.innerHTML = subSubjectType.sub_subject_type_name
                                        it.setAttribute("rowspan", childCount2.toString())
                                    }
                                    tr.appendChild(group2_td)
                                }
                            }

                            val childCount3 = countChildSubject(subjectGroup)
                            console.log("3:" + subject.name + ", " + childCount3)
                            if (childCount3 != 0) {
                                val subjectGroup_td = document.createElement("td").also {
                                    it.innerHTML = subjectGroup.description.replace("\n", "<br>")
                                    it.setAttribute("rowspan", childCount3.toString())
                                    if (subjectGroup.subjects.size == 1) {
                                        it.setAttribute("colspan", "2")
                                    }
                                }
                                tr.appendChild(subjectGroup_td)
                            }
                        }

                        if (subjectGroup.subjects.size >= 2) {
                            val subject_td = document.createElement("td").also {
                                it.innerHTML = subject.name
                            }
                            tr.appendChild(subject_td)
                        }
                    }
                }
            }
        }

        // rule_definitions.jsonの学群・学類で回す
        /*
        ruleDefinitions.faculties.forEach { faculty ->
            var passedRequiredSubjects: Boolean? = null // 応募要件を満足したか
            var passedImportantSubjects: Boolean? = null // 重点科目上限単位数を満たしたか

            val tr = document.createElement("tr")
            document.getElementById("result")!!.appendChild(tr)

            // テーブルの「講義名」
            val facultyName = document.createElement("td").also {
                it.innerHTML = faculty.facultyName
            }
            tr.appendChild(facultyName)

            // テーブルの「メッセージ」
            val comments = document.createElement("td").also {
                it.classList.add("message-box")
            }
            tr.appendChild(comments)

            // 各学群・学類で定義された要件を回す
            faculty.rules.forEach { rule ->
                when (rule.type) {
                    // 応募要件
                    "required_subjects" -> {
                        passedRequiredSubjects ?: run {
                            passedRequiredSubjects = true
                        }
                        val count = countUnit(userSubjects, rule.subjects)
                        if (count < rule.minimum || count > rule.maximum) passedRequiredSubjects = false
                    }

                    // 重点科目上限単位数
                    "important_subjects" -> {
                        passedImportantSubjects ?: run {
                            passedImportantSubjects = true
                        }
                        val count = countUnit(userSubjects, rule.subjects)
                        if (count < rule.minimum || count > rule.maximum) passedImportantSubjects = false
                    }

                    // 応募要件の制限単位
                    "required_subjects_limit" -> {
                        val count = countUnit(userSubjects, rule.subjects)
                        if (count > rule.maximum) {
                            var text = ""
                            rule.subjects.forEach {
                                val split = it.split("::") // 講義名::単位
                                text +=
                                    if (split.size == 1) ",　${split[0]} (1単位)"
                                    else ",　${split[0]} (${split[1]}単位)"
                            }
                            comments.innerHTML += "・${text.substring(2)}のうち、最大で取ることができるのは${rule.maximum}単位までです (履修予定：${count.toInt()}単位)<br />"
                        }
                    }
                }

                // メッセージがあれば表示
                if (rule.message.isNotEmpty()) {
                    comments.innerHTML += "・${rule.message}<br />"
                }
            }

            // メッセージを表示
            if (passedRequiredSubjects == false) comments.innerHTML += "・応募要件を満たしていません<br />"
            if (passedImportantSubjects == false) comments.innerHTML += "・重点科目上限を超えていません<br />"

            // 応募要件の〇×-
            tr.appendChild(
                document.createElement("td").also {
                    it.innerHTML = when (passedRequiredSubjects) {
                        true -> "<span class=\"passed\">〇</span>"
                        false -> "<span class=\"missed\">×</span>"
                        else -> "<span>-</span>"
                    }
                }
            )

            // 重点科目上限の〇×-
            tr.appendChild(
                document.createElement("td").also {
                    it.innerHTML = when (passedImportantSubjects) {
                        true -> "<span class=\"passed\">〇</span>"
                        false -> "<span class=\"missed\">×</span>"
                        else -> "<span>-</span>"
                    }
                }
            )

            // 移行要件の適合度によって学類の色を変える
            when {
                // 応募要件と重点科目上限を満足
                passedRequiredSubjects != false && passedImportantSubjects != false ->
                    facultyName.classList.add("faculty-name-passed")

                // 応募要件のみ満足
                passedRequiredSubjects != false && passedImportantSubjects == false ->
                    facultyName.classList.add("faculty-name-ok")

                // 何も満足していない
                else -> facultyName.classList.add("faculty-name-missed")
            }

        }
        */

        isChecking = false
    }

    private fun countChildSubject(subjectGroup: SubjectGroup): Int {
        return subjectGroup.subjects.size
    }

    private fun countChildSubject(subSubjectType: SubSubjectType): Int {
        var count = 0
        subSubjectType.subject_groups.forEach { subjectGroup ->
            count += countChildSubject(subjectGroup)
        }
        return count
    }

    private fun countChildSubject(subjectType: SubjectType): Int {
        var count = 0
        subjectType.sub_subject_types.forEach { subSubjectType ->
            count += countChildSubject(subSubjectType)
        }
        return count
    }

    // TODO
    // 各要件が要求する単位の計算
    private fun countUnit(userSubjects: Map<String, Double>, ruleSubjects: List<String>): Double {
        var unit = 0.0
        ruleSubjects.forEach { ruleSubject ->
            when {
                // その他の講義の場合
                ruleSubject.startsWith("#OTHER_SUBJECTS") -> {
                    var unitCount = 0.0
                    val maxUnit = ruleSubject.split(":")[1].toInt()
                    userSubjects.forEach otherSubjects@{
                        if (!ruleSubjects.contains(it.key)) {
                            if (unitCount + it.value <= maxUnit) {
                                unit += it.value
                                unitCount += it.value
                                if (unitCount >= maxUnit) return@otherSubjects
                            }
                        }
                    }
                }

                // ~から始まる講義名の場合 ex) #CONTENTS:基礎体育
                ruleSubject.startsWith("#CONTENTS") -> {
                    userSubjects
                        .filter { it.key.startsWith(ruleSubject.split(":")[1]) }
                        .forEach { unit += it.value }
                }

                // いずれにも該当しない場合
                // 講義名::単位の講義名のみを抜き出す（単位はCSVから読み込んだものを使う）
                userSubjects.contains(ruleSubject.split("::")[0]) -> {
                    unit += userSubjects[ruleSubject.split("::")[0]]!!
                }
            }
        }
        return unit
    }

    /*
     CSVファイルを読み込む。CSVライブラリが使えなかったため独自実装。
     KdBもどきから得られるCSVは
     ～
     "FA01111
     数学リテラシー1","1.0単位
     春A火5
     ～
     のように、"講義番号\n講義名","単位のようになっているため、以下のような実装になっている。
     */
    fun checkWithCSV(csv: String) {
        //TODO 要る？
        resetTable()

        //document.getElementById("subjects-box")!!.innerHTML += "<h3>登録された授業</h3>"

        val subjects = mutableMapOf<String, Double>()
        val split = csv.split("\n")
        //var subjectText = ""

        //var sum = 0.0
        split.forEachIndexed { index, text ->
            if (text.matches("^(\")([a-zA-Z0-9]{7})\$") && split.size - 1 > index + 1) {
                val data = split[index + 1].split("\",\"")
                val subjectNumber = text.match("[a-zA-Z0-9]{7}")!![0]
                val unit = data[1].match("[+-]?\\d+(?:\\.\\d+)?")!![0].toDouble()
                //sum += unit
                subjects[subjectNumber] = unit
                //subjectText += ",　$subject (${unit}単位)"
            }
        }

        //document.getElementById("subjects-box")!!.innerHTML += "<p>合計${sum}単位：${subjectText.substring(2)}</p>"

        val selectedFaculty = (document.getElementById("faculty") as HTMLSelectElement).value
        val selectedMajor = (document.getElementById("major") as HTMLSelectElement).value

        run {
            ruleDefinitions.faculties.forEach { faculty ->
                if (selectedFaculty != faculty.facultyName) {
                    return@forEach
                }

                faculty.majors.forEach { major ->
                    if (selectedMajor == major.major_name) {
                        check(subjects, major)
                        return@run
                    }
                }
            }
        }
    }
}
