package com.example.model

data class CityStats(
    val cityName: String = "Neo Horizon",
    val mayorName: String = "Mayor",
    val funds: Long = 2500L,
    val population: Int = 0,
    val happiness: Int = 85,
    val powerSupply: Int = 0,
    val powerDemand: Int = 0,
    val waterSupply: Int = 0,
    val waterDemand: Int = 0,
    val dailyIncome: Long = 0L,
    val dailyExpense: Long = 0L,
    val netDaily: Long = 0L,
    val day: Int = 1,
    val timeOfDay: Float = 9.0f, // 0.0 to 24.0 hours
    val taxRate: Int = 9, // percentage
    val totalPlaySeconds: Long = 0L,
    val simulationSpeed: Int = 1 // 0 = paused, 1 = normal, 2 = fast, 3 = turbo
) {
    val isPowerDeficit: Boolean
        get() = powerDemand > powerSupply && powerDemand > 0

    val isWaterDeficit: Boolean
        get() = waterDemand > waterSupply && waterDemand > 0

    val timeFormatted: String
        get() {
            val hours = timeOfDay.toInt() % 24
            val minutes = ((timeOfDay - hours) * 60).toInt()
            val ampm = if (hours >= 12) "PM" else "AM"
            val displayHour = when {
                hours == 0 -> 12
                hours > 12 -> hours - 12
                else -> hours
            }
            return "%d:%02d %s".format(displayHour, minutes, ampm)
        }

    val isNight: Boolean
        get() = timeOfDay < 6.0f || timeOfDay >= 19.5f

    val isSunset: Boolean
        get() = timeOfDay in 17.5f..19.5f
}
