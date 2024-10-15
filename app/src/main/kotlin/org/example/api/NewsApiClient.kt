package org.example.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.dto.GetNewsResponse
import org.example.dto.News
import org.slf4j.LoggerFactory
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.request.parameter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

val logger = LoggerFactory.getLogger("NewsLogger")

private const val API_VERSION = "v1.4"

private const val API_URL = "https://kudago.com/public-api/$API_VERSION/news/?"

private val json = Json {
    ignoreUnknownKeys = true
}
open class NewsApiClient {

    suspend fun getNews(count: Int = 100, pagination: Int = 1): List<News> {
        val startTime = System.currentTimeMillis()

        val client = HttpClient(CIO) {
            install(Logging) {
                logger = Logger.DEFAULT
            }
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val requestBuilder = HttpRequestBuilder().apply {
                url(API_URL)
                parameter("page", pagination)
                parameter("page_size", count)
                parameter("location", "msk")
                parameter("text_format", "text")
                parameter("expand", "place")
                parameter(
                    "fields",
                    "id,title,place,description,site_url,favorites_count,comments_count,publication_date"
                )
            }
            logger.debug("Формируемый URL: {}", requestBuilder.url)
            val response: HttpResponse = client.request(requestBuilder)

            if (response.status.value != 200) {
                logger.error("Ошибка при получении новостей: {}", response.status)
                throw Exception("Ошибка при получении новостей: ${response.status}")
            }

            val jsonResponse = response.bodyAsText()
            logger.info("Получен JSON: $jsonResponse")
            val newsResponse = json.decodeFromString(GetNewsResponse.serializer(), jsonResponse)
            logger.info("Количество полученных новостей: ${newsResponse.results.size}")
            return ArrayList(newsResponse.results.sortedBy { it.publicationDate })

        } catch (e: Exception) {
            logger.error("Ошибка при получении новостей: ${e.message}")
            return emptyList()
        } finally {
            client.close()
            val endTime = System.currentTimeMillis()
            logger.info("Initialization took: ${endTime - startTime} ms")
        }
    }
}

fun longToLocalDate(timestamp: Long): LocalDate {
    val instant = Instant.ofEpochSecond(timestamp)
    return instant.atZone(ZoneId.systemDefault()).toLocalDate()
}

fun List<News>.getMostRatedNews(count: Int, period: ClosedRange<LocalDate>): List<News> {
    return this.asSequence()
        .filter {
            logger.info("Дата ${it.publicationDate} входит в период: $period")
            longToLocalDate(it.publicationDate) in period
        }
        .sortedBy {
            it.rating
        }
        .take(count)
        .toList()
}

