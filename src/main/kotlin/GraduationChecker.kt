import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import model.*
import org.w3c.dom.HTMLSelectElement
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

        var tr = document.createElement("tr")
        document.getElementById("result")!!.appendChild(tr)

        //4重ループをぶん回す
        //とてもspaghetti
        major.subject_types.forEachIndexed loop1@{ index1, subjectType ->
            if (subjectType.sub_subject_types.size == 0) {
                return@loop1
            }
            val group1_td = document.createElement("td")
            val childCount1 = countChildSubject(subjectType)
            if (childCount1 != 0) {
                group1_td.innerHTML = subjectType.subject_type_name
                group1_td.setAttribute("rowspan", childCount1.toString())
            }
            subjectType.sub_subject_types.forEachIndexed loop2@{ index2, subSubjectType ->
                if (subSubjectType.subject_groups.size == 0) {
                    tr.appendChild(group1_td)
                    return@loop2
                }
                val group2_td = document.createElement("td")
                val childCount2 = countChildSubject(subSubjectType)
                if (childCount2 != 0) {
                    group2_td.innerHTML = subSubjectType.sub_subject_type_name
                    group2_td.setAttribute("rowspan", childCount2.toString())
                }
                subSubjectType.subject_groups.forEachIndexed loop3@{ index3, subjectGroup ->
                    if (subjectGroup.subjects.size == 0) {
                        if (index2 == 0) {
                            tr.appendChild(group1_td)
                        }
                        tr.appendChild(group2_td)
                        return@loop3
                    }
                    val subjectGroup_td = document.createElement("td")
                    val childCount3 = countChildSubject(subjectGroup)
                    if (childCount3 != 0) {
                        subjectGroup_td.innerHTML = subjectGroup.description.replace("\n", "<br>")
                        subjectGroup_td.setAttribute("rowspan", childCount3.toString())
                        if (subjectGroup.subjects.size == 1) {
                            subjectGroup_td.setAttribute("colspan", "2")
                        }
                    }
                    subjectGroup.subjects.forEachIndexed { index4, subject ->
                        /*
                            index1, subjectType : 専門科目 etc.
                            index2, subSubjectType : 必修科目 etc.
                            index3, subjectGroup : E,F,G,H... etc.
                            index4, subject : 確率論 etc.
                             */

                        if (subjectGroup.subjects.size == 1) {
                            if (index3 == 0) {
                                if (index2 == 0) {
                                    tr.appendChild(group1_td)
                                }
                                tr.appendChild(group2_td)
                            }
                            tr.appendChild(subjectGroup_td)
                        } else if (subjectGroup.subjects.size >= 2) {
                            val subject_td = document.createElement("td").also {
                                it.innerHTML = subject.name
                            }
                            if (index4 == 0) {
                                if (index3 == 0) {
                                    if (index2 == 0) {
                                        tr.appendChild(group1_td)
                                    }
                                    tr.appendChild(group2_td)
                                }
                                tr.appendChild(subjectGroup_td)
                            }
                            tr.appendChild(subject_td)
                        }
                        tr = document.createElement("tr")
                        document.getElementById("result")!!.appendChild(tr)
                    }
                }
            }
        }

        document.getElementById("result")!!.lastElementChild?.remove()

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
