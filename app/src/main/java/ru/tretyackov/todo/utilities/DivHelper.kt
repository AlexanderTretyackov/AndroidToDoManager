package ru.tretyackov.todo.utilities

import com.yandex.div.data.DivParsingEnvironment
import com.yandex.div.json.ParsingErrorLogger
import com.yandex.div2.DivData
import org.json.JSONObject

fun JSONObject.asDiv2DataWithTemplates(): DivData {
    val card = getJSONObject("card")
    val environment = DivParsingEnvironment(ParsingErrorLogger.LOG)
    return DivData(environment, card)
}
