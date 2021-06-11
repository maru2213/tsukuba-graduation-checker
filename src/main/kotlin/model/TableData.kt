package model

data class TableData(
    var text: String = "",
    var class_: MutableList<String> = mutableListOf(),
    var colspan: Int = 1,
    var rowspan: Int = 1
)