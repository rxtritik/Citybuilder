package com.example.model

enum class BuildingCategory(val displayName: String, val iconName: String) {
    ROADS("Roads", "directions_car"),
    RESIDENTIAL("Housing", "home"),
    COMMERCIAL("Commerce", "storefront"),
    INDUSTRIAL("Industry", "factory"),
    SERVICES("Services", "local_hospital"),
    LANDMARKS("Landmarks", "apartment")
}

enum class BuildingType(
    val id: String,
    val displayName: String,
    val category: BuildingCategory,
    val cost: Int,
    val dailyUpkeep: Int = 0,
    val populationBonus: Int = 0,
    val jobsProvided: Int = 0,
    val dailyRevenue: Int = 0,
    val powerDemand: Int = 0,
    val powerSupply: Int = 0,
    val waterDemand: Int = 0,
    val waterSupply: Int = 0,
    val happinessBonus: Int = 0,
    val serviceRadius: Int = 0,
    val pollutionLevel: Int = 0,
    val description: String,
    val unlockPopulation: Int = 0
) {
    // Roads & Transit
    ROAD(
        id = "road",
        displayName = "Paved Road",
        category = BuildingCategory.ROADS,
        cost = 25,
        dailyUpkeep = 1,
        description = "Connects homes to jobs and essential services.",
        unlockPopulation = 0
    ),
    BRIDGE(
        id = "bridge",
        displayName = "Suspension Bridge",
        category = BuildingCategory.ROADS,
        cost = 80,
        dailyUpkeep = 2,
        description = "Spans rivers to connect separated land masses.",
        unlockPopulation = 0
    ),

    // Residential
    HOUSE_SMALL(
        id = "house_small",
        displayName = "Suburban Home",
        category = BuildingCategory.RESIDENTIAL,
        cost = 90,
        dailyUpkeep = 0,
        populationBonus = 25,
        dailyRevenue = 6,
        powerDemand = 1,
        waterDemand = 1,
        happinessBonus = 2,
        description = "Cozy single-family homes for new citizens.",
        unlockPopulation = 0
    ),
    HOUSE_MODERN(
        id = "house_modern",
        displayName = "Modern Duplex",
        category = BuildingCategory.RESIDENTIAL,
        cost = 260,
        dailyUpkeep = 1,
        populationBonus = 80,
        dailyRevenue = 22,
        powerDemand = 2,
        waterDemand = 2,
        happinessBonus = 4,
        description = "Medium-density stylish duplex apartments.",
        unlockPopulation = 60
    ),
    RESIDENTIAL_TOWER(
        id = "res_tower",
        displayName = "Highrise Condo",
        category = BuildingCategory.RESIDENTIAL,
        cost = 850,
        dailyUpkeep = 4,
        populationBonus = 240,
        dailyRevenue = 70,
        powerDemand = 6,
        waterDemand = 5,
        happinessBonus = 6,
        description = "Vertical living complex housing hundreds of residents.",
        unlockPopulation = 350
    ),

    // Commercial
    SHOP_LOCAL(
        id = "shop_local",
        displayName = "Market & Cafe",
        category = BuildingCategory.COMMERCIAL,
        cost = 130,
        dailyUpkeep = 1,
        jobsProvided = 16,
        dailyRevenue = 18,
        powerDemand = 2,
        waterDemand = 1,
        description = "Local shops selling daily goods and hot coffee.",
        unlockPopulation = 0
    ),
    COMMERCIAL_MALL(
        id = "mall",
        displayName = "Shopping Plaza",
        category = BuildingCategory.COMMERCIAL,
        cost = 480,
        dailyUpkeep = 3,
        jobsProvided = 55,
        dailyRevenue = 65,
        powerDemand = 4,
        waterDemand = 3,
        happinessBonus = 4,
        description = "Retail hub with restaurants and entertainment.",
        unlockPopulation = 100
    ),
    TECH_OFFICE(
        id = "tech_office",
        displayName = "Cyber Tech Center",
        category = BuildingCategory.COMMERCIAL,
        cost = 1150,
        dailyUpkeep = 6,
        jobsProvided = 160,
        dailyRevenue = 175,
        powerDemand = 8,
        waterDemand = 4,
        description = "Cutting-edge software and design research park.",
        unlockPopulation = 400
    ),

    // Industrial & Energy
    WIND_TURBINE(
        id = "wind_turbine",
        displayName = "Wind Turbine",
        category = BuildingCategory.INDUSTRIAL,
        cost = 220,
        dailyUpkeep = 2,
        powerSupply = 18,
        description = "Clean, sustainable breeze-powered electricity generator.",
        unlockPopulation = 0
    ),
    SOLAR_ARRAY(
        id = "solar_array",
        displayName = "Solar Farm",
        category = BuildingCategory.INDUSTRIAL,
        cost = 450,
        dailyUpkeep = 3,
        powerSupply = 42,
        description = "Silent photovoltaic panels producing green power.",
        unlockPopulation = 80
    ),
    COAL_PLANT(
        id = "coal_plant",
        displayName = "Thermal Power Plant",
        category = BuildingCategory.INDUSTRIAL,
        cost = 780,
        dailyUpkeep = 12,
        powerSupply = 95,
        pollutionLevel = 8,
        description = "Heavy output plant. Produces smog and lowers nearby happiness.",
        unlockPopulation = 0
    ),
    WATER_TOWER(
        id = "water_tower",
        displayName = "Water Tower",
        category = BuildingCategory.INDUSTRIAL,
        cost = 180,
        dailyUpkeep = 3,
        waterSupply = 24,
        description = "Elevated reservoir supplying clean piped water.",
        unlockPopulation = 0
    ),
    WATER_STATION(
        id = "water_station",
        displayName = "Water Treatment",
        category = BuildingCategory.INDUSTRIAL,
        cost = 520,
        dailyUpkeep = 8,
        waterSupply = 75,
        description = "High-capacity filtration plant providing city-wide water.",
        unlockPopulation = 120
    ),
    FACTORY(
        id = "factory",
        displayName = "Manufacturing Plant",
        category = BuildingCategory.INDUSTRIAL,
        cost = 360,
        dailyUpkeep = 4,
        jobsProvided = 40,
        dailyRevenue = 48,
        powerDemand = 5,
        waterDemand = 3,
        pollutionLevel = 6,
        description = "Produces goods and brings steady industrial taxes.",
        unlockPopulation = 30
    ),

    // Services
    PARK_SMALL(
        id = "park_small",
        displayName = "Pocket Park",
        category = BuildingCategory.SERVICES,
        cost = 75,
        dailyUpkeep = 1,
        waterDemand = 1,
        happinessBonus = 8,
        serviceRadius = 3,
        description = "Green trees and benches lifting neighborhood spirits.",
        unlockPopulation = 0
    ),
    BOTANICAL_GARDEN(
        id = "botanical_garden",
        displayName = "Botanical Garden",
        category = BuildingCategory.SERVICES,
        cost = 320,
        dailyUpkeep = 4,
        waterDemand = 3,
        happinessBonus = 16,
        serviceRadius = 5,
        description = "Exotic flora sanctuary giving significant happiness.",
        unlockPopulation = 150
    ),
    POLICE_STATION(
        id = "police",
        displayName = "Police Precinct",
        category = BuildingCategory.SERVICES,
        cost = 340,
        dailyUpkeep = 8,
        powerDemand = 2,
        waterDemand = 1,
        happinessBonus = 8,
        serviceRadius = 4,
        description = "Keeps streets safe from petty crime.",
        unlockPopulation = 50
    ),
    FIRE_STATION(
        id = "fire",
        displayName = "Fire Department",
        category = BuildingCategory.SERVICES,
        cost = 340,
        dailyUpkeep = 8,
        powerDemand = 2,
        waterDemand = 2,
        happinessBonus = 8,
        serviceRadius = 4,
        description = "Rapid emergency response protecting city structures.",
        unlockPopulation = 80
    ),
    HOSPITAL(
        id = "hospital",
        displayName = "General Hospital",
        category = BuildingCategory.SERVICES,
        cost = 620,
        dailyUpkeep = 14,
        powerDemand = 5,
        waterDemand = 4,
        happinessBonus = 15,
        serviceRadius = 6,
        description = "Top-tier health care improving citizen life expectancy.",
        unlockPopulation = 200
    ),
    SCHOOL(
        id = "school",
        displayName = "City Academy",
        category = BuildingCategory.SERVICES,
        cost = 450,
        dailyUpkeep = 10,
        powerDemand = 3,
        waterDemand = 2,
        happinessBonus = 12,
        serviceRadius = 5,
        description = "Educates youth, creating a high-skilled workforce.",
        unlockPopulation = 120
    ),

    // Landmarks
    CITY_HALL(
        id = "city_hall",
        displayName = "Grand City Hall",
        category = BuildingCategory.LANDMARKS,
        cost = 1400,
        dailyUpkeep = 20,
        powerDemand = 4,
        waterDemand = 3,
        happinessBonus = 20,
        description = "The civic heart of your metropolis. Unlocks tax policies.",
        unlockPopulation = 300
    ),
    LANDMARK_MONUMENT(
        id = "landmark_monument",
        displayName = "Cosmo Spire",
        category = BuildingCategory.LANDMARKS,
        cost = 3200,
        dailyUpkeep = 35,
        dailyRevenue = 260,
        powerDemand = 10,
        waterDemand = 5,
        happinessBonus = 28,
        description = "World-famous monument drawing millions of tourists.",
        unlockPopulation = 800
    );

    companion object {
        fun fromId(id: String?): BuildingType? = entries.firstOrNull { it.id == id }
    }
}
