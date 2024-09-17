package org.example.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.exp

@Serializable
data class News(
    val id: Int = Int.MIN_VALUE,
    val title: String = "",
    val place: Place?,
    val textFormat: String = "",
    @SerialName("publication_date") val publicationDate: Long,
    val description: String = "",
    @SerialName("site_url") val siteUrl: String = "",
    @SerialName("favorites_count") val favoritesCount: Int = 0,
    @SerialName("comments_count") val commentsCount: Int = 0,
    val rating: Double = 1 / (1 + exp((-(favoritesCount / (commentsCount + 1))).toDouble()))
)


