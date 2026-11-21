package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DaySlot(
    val isoDate: String, // yyyy-MM-dd
    val dayShort: String, // Mon, Tue...
    val dayNumber: String, // 21
    val isToday: Boolean
)

object DateUtils {

    private val isoFormat get() = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val monthPrefixFormat get() = SimpleDateFormat("yyyy-MM", Locale.US)
    private val prettyDateFormat get() = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    private val dayShortFormat get() = SimpleDateFormat("EEE", Locale.US)
    private val dayNumFormat get() = SimpleDateFormat("dd", Locale.US)

    fun todayIso(): String = isoFormat.format(Date())

    fun currentMonthPrefix(): String = monthPrefixFormat.format(Date())

    fun currentHour(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    fun formatPrettyDate(isoDate: String): String {
        return runCatching {
            val parsed = isoFormat.parse(isoDate)
            if (parsed != null) prettyDateFormat.format(parsed) else isoDate
        }.getOrDefault(isoDate)
    }

    fun getNext7Days(): List<DaySlot> {
        val todayStr = todayIso()
        val cal = Calendar.getInstance()
        return (0..6).map { offset ->
            val copy = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
            val date = copy.time
            val iso = isoFormat.format(date)
            DaySlot(
                isoDate = iso,
                dayShort = dayShortFormat.format(date),
                dayNumber = dayNumFormat.format(date),
                isToday = iso == todayStr
            )
        }
    }

    fun formatCurrency(amount: Double, symbol: String = "Rs."): String {
        val nf = NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
            minimumFractionDigits = 0
        }
        return "$symbol ${nf.format(amount)}"
    }
}
