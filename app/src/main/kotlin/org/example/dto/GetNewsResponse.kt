package org.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class GetNewsResponse(
    val results: List<News>
)