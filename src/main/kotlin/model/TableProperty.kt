package model

data class TableProperty(
    var isFilled: Boolean = false,
    val data: TableData = TableData()
)
