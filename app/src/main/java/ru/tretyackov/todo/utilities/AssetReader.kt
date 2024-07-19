package ru.tretyackov.todo.utilities

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream


class AssetReader(private val context: Context) {
    fun read(filename: String): JSONObject {
        var buffer: ByteArray? = null
        val stream: InputStream
        try {
            stream = context.assets.open(filename)
            val size = stream.available()
            buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val strSata = String(buffer!!)
        return JSONObject(strSata.trimIndent())
    }
}