// MenuItem
package com.example.colfi.data.model

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val categoryID: String = "", // Added this field since your repo uses it
    val imageURL: String = "",
    val availability: Boolean = true, // Changed from 'available' to 'availability'
) {
    constructor() : this("", "", "", 0.0, "", "", true)

    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "description" to description,
            "price" to price,
            "categoryID" to categoryID,
            "imageURL" to imageURL,
            "availability" to availability, // Match your Firestore field name
        )
    }
}
