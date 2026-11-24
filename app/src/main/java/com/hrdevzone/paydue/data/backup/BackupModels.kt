package com.hrdevzone.paydue.data.backup

import com.hrdevzone.paydue.data.local.Due
import com.hrdevzone.paydue.data.model.DueCategory
import com.hrdevzone.paydue.data.model.DueDirection
import com.hrdevzone.paydue.data.model.RepeatType
import org.json.JSONArray
import org.json.JSONObject

private const val SCHEMA_VERSION = 1

object BackupCodec {

    fun encode(dues: List<Due>): String {
        val root = JSONObject()
        root.put("schemaVersion", SCHEMA_VERSION)
        root.put("exportedAt", System.currentTimeMillis())
        val array = JSONArray()
        dues.forEach { due ->
            val obj = JSONObject()
            obj.put("title", due.title)
            obj.put("amount", due.amount)
            obj.put("dueDateEpochDay", due.dueDateEpochDay)
            obj.put("category", due.category.name)
            obj.put("notes", due.notes)
            obj.put("repeatType", due.repeatType.name)
            obj.put("direction", due.direction.name)
            obj.put("personName", due.personName)
            obj.put("isPaid", due.isPaid)
            obj.put("lastPaidEpochDay", due.lastPaidEpochDay ?: JSONObject.NULL)
            obj.put("reminderEnabled", due.reminderEnabled)
            obj.put("createdAt", due.createdAt)
            array.put(obj)
        }
        root.put("dues", array)
        return root.toString(2)
    }

    fun decode(json: String): List<Due> {
        val root = JSONObject(json)
        val array = root.optJSONArray("dues") ?: JSONArray()
        val result = mutableListOf<Due>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val due = Due(
                title = obj.getString("title"),
                amount = obj.getDouble("amount"),
                dueDateEpochDay = obj.getLong("dueDateEpochDay"),
                category = runCatching { DueCategory.valueOf(obj.getString("category")) }
                    .getOrDefault(DueCategory.OTHER),
                notes = obj.optString("notes", ""),
                repeatType = runCatching { RepeatType.valueOf(obj.getString("repeatType")) }
                    .getOrDefault(RepeatType.ONCE),
                direction = runCatching { DueDirection.valueOf(obj.getString("direction")) }
                    .getOrDefault(DueDirection.PAYABLE),
                personName = obj.optString("personName", ""),
                isPaid = obj.optBoolean("isPaid", false),
                lastPaidEpochDay = if (obj.isNull("lastPaidEpochDay")) null else obj.optLong("lastPaidEpochDay"),
                reminderEnabled = obj.optBoolean("reminderEnabled", true),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
            result.add(due)
        }
        return result
    }

    fun encodeCsv(dues: List<Due>): String {
        val header = "Title,Amount,DueDate,Category,Direction,Repeat,Paid,PersonName,Notes"
        val rows = dues.joinToString("\n") { due ->
            val date = java.time.LocalDate.ofEpochDay(due.dueDateEpochDay)
            listOf(
                due.title,
                due.amount.toString(),
                date.toString(),
                due.category.name,
                due.direction.name,
                due.repeatType.name,
                due.isPaid.toString(),
                due.personName,
                due.notes
            ).joinToString(",") { field -> csvEscape(field) }
        }
        return "$header\n$rows"
    }

    private fun csvEscape(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value
    }
}
