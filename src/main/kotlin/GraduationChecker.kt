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
        if (ruleDefinitions.faculties.size >= 2) {
            facultySelect.innerHTML += """<option value="null">選択してください</option>"""
        }
        ruleDefinitions.faculties.forEach { faculty ->
            facultySelect.innerHTML += "<option>${faculty.facultyName}</option>"
        }

        document.getElementById("faculty")?.addEventListener("input", EventListener { event ->
            resetTable()
            val majorSelect = document.getElementById("major") ?: run {
                //TODO 文言これでいい？
                window.alert("エラーが発生しました")
                return@EventListener
            }

            val selectedValue = (event.target as HTMLSelectElement).value
            if (selectedValue == "null") {
                majorSelect.innerHTML = ""
                return@EventListener
            }

            ruleDefinitions.faculties.forEach { faculty ->
                if (selectedValue != faculty.facultyName) {
                    //continueみたいなモノ
                    return@forEach
                }

                if (faculty.majors.size >= 2) {
                    majorSelect.innerHTML += """<option value="null">選択してください</option>"""
                }
                faculty.majors.forEach { major ->
                    majorSelect.innerHTML += "<option>${major.major_name}</option>"
                }
                return@EventListener
            }
        })
        document.getElementById("major")?.addEventListener("input", EventListener { resetTable() })
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

        val array = Array(countChildSubject(major)) { Array(7) { TableProperty() } }
        var i = 0

        //4重ループをぶん回す
        major.subject_types.forEach { subjectType ->
            subjectType.sub_subject_types.forEach { subSubjectType ->
                subSubjectType.subject_groups.forEach { subjectGroup ->
                    subjectGroup.subjects.forEach { subject ->
                        if (!array[i][0].isFilled) {
                            array[i][0].data.text = subjectType.subject_type_name
                            array[i][0].data.colspan = 1
                            array[i][0].data.rowspan = countChildSubject(subjectType)
                            for (j in i until i + array[i][0].data.rowspan) {
                                array[j][0].isFilled = true
                            }
                        }
                        if (!array[i][1].isFilled) {
                            array[i][1].data.text = subSubjectType.sub_subject_type_name
                            array[i][1].data.colspan = 1
                            array[i][1].data.rowspan = countChildSubject(subSubjectType)
                            for (j in i until i + array[i][1].data.rowspan) {
                                array[j][1].isFilled = true
                            }
                        }
                        if (!array[i][2].isFilled) {
                            array[i][2].data.text = subjectGroup.description.replace("\n", "<br>")
                            array[i][2].data.colspan = if (countChildSubject(subjectGroup) == 1) 2 else 1
                            array[i][2].data.rowspan = countChildSubject(subjectGroup)
                            for (j in i until i + array[i][2].data.rowspan) {
                                for (k in 2 until 2 + array[i][2].data.colspan) {
                                    array[j][k].isFilled = true
                                }
                            }
                        }
                        if (!array[i][3].isFilled) {
                            array[i][3].data.text = subject.name
                            array[i][3].data.colspan = 1
                            array[i][3].data.rowspan = 1
                            array[i][3].isFilled = true
                        }
                        if (!array[i][4].isFilled) {
                            if (subjectGroup.credits_min == subjectGroup.credits_max) {
                                array[i][4].data.text = subjectGroup.credits_min.toString()
                            } else {
                                if (subjectGroup.credits_max == Int.MAX_VALUE) {
                                    array[i][4].data.text = "${subjectGroup.credits_min}〜"
                                } else {
                                    array[i][4].data.text = "${subjectGroup.credits_min}〜${subjectGroup.credits_max}"
                                }
                            }
                            array[i][4].data.colspan = 1
                            array[i][4].data.rowspan = countChildSubject(subjectGroup)
                            for (j in i until i + array[i][4].data.rowspan) {
                                array[j][4].isFilled = true
                            }
                        }
                        if (!array[i][5].isFilled) {
                            if (subSubjectType.credits_min == subSubjectType.credits_max) {
                                array[i][5].data.text = subSubjectType.credits_min.toString()
                            } else {
                                if (subSubjectType.credits_max == Int.MAX_VALUE) {
                                    array[i][5].data.text = "${subSubjectType.credits_min}〜"
                                } else {
                                    array[i][5].data.text = "${subSubjectType.credits_min}〜${subSubjectType.credits_max}"
                                }
                            }
                            array[i][5].data.colspan = 1
                            array[i][5].data.rowspan = countChildSubject(subSubjectType)
                            for (j in i until i + array[i][5].data.rowspan) {
                                array[j][5].isFilled = true
                            }
                        }
                        if (!array[i][6].isFilled) {
                            array[i][6].data.text = major.credits_graduation.toString()
                            array[i][6].data.colspan = 1
                            array[i][6].data.rowspan = countChildSubject(major)
                            for (j in i until i + array[i][6].data.rowspan) {
                                array[j][6].isFilled = true
                            }
                        }
                        i++
                    }
                }
            }
        }

        //TODO 単位計算

        for (j in array.indices) {
            val tr = document.createElement("tr")
            document.getElementById("result")!!.appendChild(tr)
            for (k in array[j].indices) {
                if (array[j][k].data.text == "") {
                    continue
                }
                val td = document.createElement("td").also {
                    it.innerHTML = array[j][k].data.text
                    it.setAttribute("colspan", array[j][k].data.colspan.toString())
                    it.setAttribute("rowspan", array[j][k].data.rowspan.toString())
                }
                tr.appendChild(td)
            }
        }

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

    private fun countChildSubject(major: Major): Int {
        var count = 0
        major.subject_types.forEach { subjectType ->
            count += countChildSubject(subjectType)
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
