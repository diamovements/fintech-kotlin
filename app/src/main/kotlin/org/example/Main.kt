package org.example

import kotlinx.coroutines.runBlocking
import org.example.api.NewsApiClient
import org.example.api.getMostRatedNews
import java.time.LocalDate

fun main() = runBlocking {
    val client = NewsApiClient()
    val count = 20
    val pagination = 1
    val newsList = client.getNews(count, pagination)
    val startDate = LocalDate.of(2020, 1, 1)
    val endDate = LocalDate.of(2024, 12, 31)
    val period = startDate..endDate
    val mostRatedNews = newsList.getMostRatedNews(count, period)
    client.saveNews("allNews.csv", newsList)
    client.saveNews("mostRatedNews.csv", mostRatedNews)
}