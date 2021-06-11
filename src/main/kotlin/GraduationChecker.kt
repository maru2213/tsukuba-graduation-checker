import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import model.RuleDefinition.*
import model.Table.TableProperty
import org.w3c.fetch.Request

object GraduationChecker {

    private lateinit var ruleDefinitions: RuleDefinition
    private var isChecking = false

    fun loadRuleDefinitions() {
        window.fetch(Request("https://maru2213.github.io/tsukuba-graduation-checker/rule_definitions.json"))
            .then(onFulfilled = {
                it.text().then { json ->
                    onLoadFinished(json)
                }
            })
    }

    private fun onLoadFinished(json: String) {
        ruleDefinitions = Json.decodeFromString(RuleDefinition.serializer(), json)
        //console.log("[Rule Definitions] Version: ${ruleDefinitions.version} Last Updated At: ${ruleDefinitions.updatedAt}")

        val facultySelect = document.getElementById("faculty") ?: run {
            window.alert("エラーが発生しました。- G1")
            return
        }
        val majorSelect = document.getElementById("major") ?: run {
            window.alert("エラーが発生しました。- G2")
            return
        }

        if (ruleDefinitions.faculties.size == 1) {
            val faculty = ruleDefinitions.faculties[0]
            facultySelect.innerHTML += "<option>${faculty.facultyName}</option>"

            if (faculty.majors.size >= 2) {
                majorSelect.innerHTML += """<option value="null">選択してください</option>"""
                addInputMajorEventListener()
            }
            faculty.majors.forEach { major ->
                majorSelect.innerHTML += "<option>${major.major_name}</option>"
            }
        } else {
            facultySelect.innerHTML += """<option value="null">選択してください</option>"""
            ruleDefinitions.faculties.forEach { faculty ->
                facultySelect.innerHTML += "<option>${faculty.facultyName}</option>"
            }
            addInputFacultyEventListener(ruleDefinitions)
            addInputMajorEventListener()
        }
    }

    private fun check(userSubjects: Map<String, Double>, major: Major) {
        if (isChecking) {
            window.alert("判定中です")
            return
        }

        isChecking = true

        val array = Array(countChildSubject(major)) { Array(7) { TableProperty() } }
        var i = 0

        //7重ループをぶん回す
        var majorCreditCount = 0.0
        major.subject_types.forEach { subjectType ->
            subjectType.sub_subject_types.forEach subSubjectType@{ subSubjectType ->
                var subSubjectTypeCreditCount = 0.0
                subSubjectType.subject_groups.forEach subjectGroup@{ subjectGroup ->
                    var subjectGroupCreditCount = 0.0
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
                            array[i][2].data.text = subjectGroup.description
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
                                array[i][4].data.text = "（${subjectGroup.credits_min}）"
                            } else {
                                if (subjectGroup.credits_max == Int.MAX_VALUE) {
                                    array[i][4].data.text = "（${subjectGroup.credits_min}〜）"
                                } else {
                                    array[i][4].data.text = "（${subjectGroup.credits_min}〜${subjectGroup.credits_max}）"
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
                                array[i][5].data.text = "（${subSubjectType.credits_min}）"
                            } else {
                                if (subSubjectType.credits_max == Int.MAX_VALUE) {
                                    array[i][5].data.text = "（${subSubjectType.credits_min}〜）"
                                } else {
                                    array[i][5].data.text =
                                        "（${subSubjectType.credits_min}〜${subSubjectType.credits_max}）"
                                }
                            }
                            array[i][5].data.colspan = 1
                            array[i][5].data.rowspan = countChildSubject(subSubjectType)
                            for (j in i until i + array[i][5].data.rowspan) {
                                array[j][5].isFilled = true
                            }
                        }
                        if (!array[i][6].isFilled) {
                            array[i][6].data.text = "（${major.credits_graduation}）"
                            array[i][6].data.colspan = 1
                            array[i][6].data.rowspan = countChildSubject(major)
                            for (j in i until i + array[i][6].data.rowspan) {
                                array[j][6].isFilled = true
                            }
                        }

                        //TODO 「〇〇から始まる科目」系から必修科目を自動的に除外できないか？
                        userSubjects.forEach userSubject@{ userSubject ->
                            subject.subject_number.forEach subjectNum@{ subjectNumber ->
                                if (subjectNumber == "" || !userSubject.key.startsWith(subjectNumber, true)) {
                                    return@subjectNum
                                }

                                subject.except_subject_numbers.forEach { exceptNumber ->
                                    if (userSubject.key.startsWith(exceptNumber, true)) {
                                        return@subjectNum
                                    }
                                }

                                if (userSubject.value == -1.0) {
                                    if (subject.credits == -1.0) {
                                        //TODO KdBから探す
                                    } else {
                                        subjectGroupCreditCount += subject.credits
                                        subSubjectTypeCreditCount += subject.credits
                                        majorCreditCount += subject.credits
                                    }
                                } else {
                                    subjectGroupCreditCount += userSubject.value
                                    subSubjectTypeCreditCount += userSubject.value
                                    majorCreditCount += userSubject.value
                                }
                                if (userSubject.key == subjectNumber) {
                                    return@subjectNum
                                }
                            }
                        }

                        i++
                    }
                    for (j in i - 1 downTo 0) {
                        if (array[j][4].isFilled && array[j][4].data.text.startsWith("（")) {
                            array[j][4].data.text = subjectGroupCreditCount.toString() + array[j][4].data.text
                            if (subjectGroup.credits_min <= subjectGroupCreditCount && subjectGroupCreditCount <= subjectGroup.credits_max) {
                                array[j][4].data.class_.add("satisfied")
                            } else if (subjectGroup.credits_max < subjectGroupCreditCount) {
                                array[j][4].data.class_.add("overed")
                            }
                            return@subjectGroup
                        }
                    }
                }
                for (j in i - 1 downTo 0) {
                    if (array[j][5].isFilled && array[j][5].data.text.startsWith("（")) {
                        array[j][5].data.text = subSubjectTypeCreditCount.toString() + array[j][5].data.text
                        if (subSubjectType.credits_min <= subSubjectTypeCreditCount && subSubjectTypeCreditCount <= subSubjectType.credits_max) {
                            array[j][5].data.class_.add("satisfied")
                        } else if (subSubjectType.credits_max < subSubjectTypeCreditCount) {
                            array[j][5].data.class_.add("overed")
                        }
                        return@subSubjectType
                    }
                }
            }
        }
        array[0][6].data.text = majorCreditCount.toString() + array[0][6].data.text
        if (major.credits_graduation.toDouble() == majorCreditCount) {
            array[0][6].data.class_.add("satisfied")
        } else if (major.credits_graduation < majorCreditCount) {
            array[0][6].data.class_.add("overed")
        }

        val tbody = document.createElement("tbody")
        document.getElementById("result")!!.appendChild(tbody)
        for (j in array.indices) {
            val tr = document.createElement("tr")
            tbody.appendChild(tr)
            for (k in array[j].indices) {
                if (array[j][k].data.text == "") {
                    continue
                }
                val td = document.createElement("td").also {
                    val stringBuilder = StringBuilder()
                    array[j][k].data.class_.forEach { c ->
                        stringBuilder.append(c)
                        stringBuilder.append(" ")
                    }
                    it.innerHTML = array[j][k].data.text.replace("\n", "<br>")
                    it.setAttribute("class", stringBuilder.toString().dropLast(1))
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

    fun checkWithCSV(csv: String, inputMode: String, selectedFaculty: String, selectedMajor: String) {
        resetTable()

        val subjects = mutableMapOf<String, Double>()
        //TODO 他サイト対応
        if (inputMode == "alternative-kdb") {
            val split = csv.split("\n")
            split.forEachIndexed { index, text ->
                if (text.matches("^(\")([a-zA-Z0-9]{7})\$") && split.size - 1 > index + 1) {
                    val subjectNumber = text.match("[a-zA-Z0-9]{7}")!![0]
                    val unit = split[index + 1].split("\",\"")[1].match("\\d+(?:\\.\\d+)?")!![0].toDouble()
                    subjects[subjectNumber] = unit
                }
            }
        } else {
            window.alert("エラーが発生しました。- G3")
            return
        }

        //TODO この辺のreturnがうまくいってるか不安
        ruleDefinitions.faculties.forEach { faculty ->
            if (selectedFaculty != faculty.facultyName) {
                return@forEach
            }

            faculty.majors.forEach { major ->
                if (selectedMajor == major.major_name) {
                    check(subjects.toList().sortedBy { it.first }.toMap(), major)
                    return
                }
            }
        }
    }
}
