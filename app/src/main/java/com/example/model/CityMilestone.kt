package com.example.model

data class CityMilestone(
    val id: String,
    val title: String,
    val description: String,
    val targetPopulation: Int,
    val rewardFunds: Long,
    val rewardDescription: String
) {
    companion object {
        val ALL_MILESTONES = listOf(
            CityMilestone(
                id = "pioneer",
                title = "Settler's Outpost",
                description = "Attract your first citizen to Neo Horizon.",
                targetPopulation = 1,
                rewardFunds = 500L,
                rewardDescription = "$500 Founding Grant"
            ),
            CityMilestone(
                id = "hamlet",
                title = "Growing Hamlet",
                description = "Reach a population of 50 residents.",
                targetPopulation = 50,
                rewardFunds = 1200L,
                rewardDescription = "$1,200 & Modern Housing Unlocked"
            ),
            CityMilestone(
                id = "village",
                title = "Thriving Town",
                description = "Grow the community to 150 citizens.",
                targetPopulation = 150,
                rewardFunds = 3000L,
                rewardDescription = "$3,000 & Civic Services Unlocked"
            ),
            CityMilestone(
                id = "city",
                title = "Flourishing City",
                description = "Reach 400 citizens with stable utilities.",
                targetPopulation = 400,
                rewardFunds = 6500L,
                rewardDescription = "$6,500 & Tech Hubs Unlocked"
            ),
            CityMilestone(
                id = "metropolis",
                title = "Grand Metropolis",
                description = "Reach 1,000 proud citizens in your urban paradise.",
                targetPopulation = 1000,
                rewardFunds = 15000L,
                rewardDescription = "$15,000 & Megastructure Unlocked"
            ),
            CityMilestone(
                id = "megacity",
                title = "Futuristic Megacity",
                description = "Attain an empire of 2,500 citizens.",
                targetPopulation = 2500,
                rewardFunds = 35000L,
                rewardDescription = "$35,000 & Golden Key to the City"
            )
        )
    }
}

data class CityEvent(
    val id: String,
    val title: String,
    val message: String,
    val optionA: EventOption,
    val optionB: EventOption
)

data class EventOption(
    val label: String,
    val cost: Long = 0L,
    val fundsReward: Long = 0L,
    val happinessDelta: Int = 0,
    val feedback: String
)
