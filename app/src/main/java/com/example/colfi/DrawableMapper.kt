package com.example.colfi

object DrawableMapper {
    // Map image names to drawable resource IDs
    fun getDrawableForImageName(itemName: String): Int {
        val normalizedName = itemName.lowercase().replace(" ", "_")
        return when (normalizedName) {
            "nutty_black" -> R.drawable.nutty_black
            "nutty_white" -> R.drawable.nutty_white
            "mocha" -> R.drawable.mocha
            "osmanthus_latte" -> R.drawable.osmanthus
            "matcha_latte" -> R.drawable.matcha
            "chocolate_latte" -> R.drawable.chocolate
            //"lemonade" -> R.drawable.lemonade
            "babycino" -> R.drawable.babycino
            "momiji" -> R.drawable.momiji
            "nanten" -> R.drawable.nanten
            "asebi" -> R.drawable.asebi
            "mitsu" -> R.drawable.mitsu
            "espresso" -> R.drawable.espresso
            "milk" -> R.drawable.milk
            "oatside_milk" -> R.drawable.oatside
            // etc.
            else -> R.drawable.espresso
        }
    }

    // Generate image name from item name (useful for new items)
    fun generateImageName(itemName: String): String {
        return itemName.lowercase()
            .replace(" ", "_")
            .replace("-", "_")
            .filter { it.isLetterOrDigit() || it == '_' }
    }
}