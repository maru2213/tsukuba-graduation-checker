import kotlinx.browser.document
import kotlinx.browser.window
import model.RuleDefinition
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.EventListener
import org.w3c.files.FileList
import org.w3c.files.FileReader
import org.w3c.files.get

private var csvFiles: FileList? = null
private var csvString = ""
private var readIndex = 0

fun main() {
    GraduationChecker.loadRuleDefinitions()

    document.getElementById("start-checking")?.addEventListener("click", EventListener { onStartCheckingButtonClicked() })
    document.getElementById("input-csv")?.addEventListener("change", EventListener { event ->
        csvFiles = null
        resetTable()

        val files = (event.target as HTMLInputElement).files ?: run {
            return@EventListener
        }

        val fileCount = files.length
        if (fileCount == 0) {
            return@EventListener
        }

        for (i in 0 until fileCount) {
            val file = files[i] ?: run {
                window.alert("エラーが発生しました。- M1")
                return@EventListener
            }

            if (!file.name.endsWith(".csv")) {
                window.alert("CSVファイルにのみ対応しています。")
                return@EventListener
            }
        }

        csvFiles = files
    })
}

private fun onStartCheckingButtonClicked() {
    val elements = document.getElementsByName("data-source")
    var inputMode = ""
    for (i in 0 until elements.length) {
        if ((elements.item(i) as HTMLInputElement).checked) {
            inputMode = (elements.item(i) as HTMLInputElement).value
        }
    }
    if (inputMode == "") {
        window.alert("データのインポート元を選択してください。")
        return
    }

    val fileCount = csvFiles?.length ?: run {
        window.alert("ファイルを選択してください。")
        return
    }
    if (fileCount == 0) {
        window.alert("ファイルを選択してください。")
        return
    }

    val f = document.getElementById("faculty") ?: run {
        window.alert("エラーが発生しました。- M2")
        return
    }
    val m = document.getElementById("major") ?: run {
        window.alert("エラーが発生しました。- M3")
        return
    }
    val facultySelect = f as HTMLSelectElement
    val majorSelect = m as HTMLSelectElement
    if (facultySelect.value == "null" || majorSelect.value == "null") {
        window.alert("学類・主専攻を選択してください")
        return
    }

    readCSVFiles(fileCount, inputMode, facultySelect.value, majorSelect.value)
}

private fun readCSVFiles(fileCount: Int, inputMode: String, faculty: String, major: String) {
    val file = csvFiles?.get(readIndex) ?: run {
        window.alert("エラーが発生しました。- M4")
        return
    }
    val reader = FileReader()
    reader.readAsText(file)
    reader.onload = {
        val stringBuilder = StringBuilder().also {
            it.append(csvString)
            it.append(reader.result.toString())
        }
        stringBuilder.toString().also { csvString = it }
        if (readIndex == fileCount - 1) {
            GraduationChecker.checkWithCSV(csvString, inputMode, faculty, major)
        } else {
            readIndex++
            readCSVFiles(fileCount, inputMode, faculty, major)
        }
    }
}

fun resetTable() {
    document.getElementById("result")!!.innerHTML =
        """
            <thead>
                <tr>
                    <th class="group" rowspan="2">分類1</th>
                    <th class="group" rowspan="2">分類2</th>
                    <th class="subject_group" rowspan="2">科目群名</th>
                    <th class="subject" rowspan="2">科目名</th>
                    <th class="credit_count" colspan="3">履修単位数(必要単位数)</th>
                </tr>
                <tr>
                    <th class="credit_count">科目群</th>
                    <th class="credit_count">分類2</th>
                    <th class="credit_count">卒業</th>
                </tr>
            </thead>
        """.trimIndent()
}

fun addInputFacultyEventListener(ruleDefinitions: RuleDefinition) {
    document.getElementById("faculty")?.addEventListener("input", EventListener { event ->
        resetTable()
        val majorSelect = document.getElementById("major") ?: run {
            window.alert("エラーが発生しました。- M5")
            return@EventListener
        }

        val selectedValue = (event.target as HTMLSelectElement).value
        if (selectedValue == "null") {
            majorSelect.innerHTML = ""
            return@EventListener
        }

        ruleDefinitions.faculties.forEach { faculty ->
            if (selectedValue != faculty.facultyName) {
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
}

fun addInputMajorEventListener() {
    document.getElementById("major")?.addEventListener("input", EventListener { resetTable() })
}