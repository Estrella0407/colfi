// MenuItem
package com.example.colfi.data.model

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val imageURL: String = "",
    val availability: Boolean = true
) {
    constructor() : this("", "", "", 0.0, "", "", true)

    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "description" to description,
            "price" to price,
            "category" to category,
            "imageURL" to imageURL,
            "availability" to availability, // Match your Firestore field name
        )
    }
}
