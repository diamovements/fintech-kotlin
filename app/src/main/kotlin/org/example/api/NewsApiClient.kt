package org.example.api

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.example.dto.GetNewsResponse
import org.example.dto.News
import org.slf4j.LoggerFactory
import io.ktor.client.plugins.logging.*
import kotlinx.html.*
import java.io.File
import kotlinx.html.stream.createHTML
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
                parameter("fields", "id,title,place,description,site_url,favorites_count,comments_count,publication_date")
            }
            logger.debug("Формируемый URL: {}", requestBuilder.url)
            val response: HttpResponse = client.request(requestBuilder)

            if (response.status.value != 200) {
                throw Exception("Ошибка получения новостей: ${response.status}")
            }

            val jsonResponse = response.bodyAsText()
            logger.info("Получен JSON: $jsonResponse")
            val newsResponse = json.decodeFromString(GetNewsResponse.serializer(), jsonResponse)
            logger.info("Количество полученных новостей: ${newsResponse.results.size}")
            return newsResponse.results

        } catch (e: Exception) {
            logger.error("Ошибка при получении новостей: ${e.message}")
            return emptyList()
        } finally {
            client.close()
        }
    }

    fun saveNews(path: String, news: Collection<News>) {
        val file = File(path)
        if (file.exists()) {
            logger.error("Файл существует: {}", path)
            throw IllegalArgumentException("Файл $path уже существует")
        }
        val htmlContent = typeSafeBuilder(news)
        file.writeText(htmlContent)
    }

    fun typeSafeBuilder(news: Collection<News>): String {
        val htmlContent = createHTML().html {
            head {
                title("Новости")
                meta { charset = "UTF-8" }
            }
            body {
                h1 { +"Новости" }
                table {
                    tr {
                        th { +"ID" }
                        th { +"Заголовок" }
                        th { +"Дата публикации" }
                        th { +"Место" }
                        th { +"Описание" }
                        th { +"URL" }
                        th { +"Избранное" }
                        th { +"Комментарии" }
                        th { +"Рейтинг" }
                    }
                    news.forEach { item ->
                        tr {
                            td { +item.id.toString() }
                            td { +item.title }
                            td { +longToLocalDate(item.publicationDate).toString() }
                            td { +(item.place?.id?.toString() ?: "Не указано") }
                            td { unsafe { +item.description } }
                            td {
                                a(href = item.siteUrl) { +"Ссылка" }
                            }
                            td { +item.favoritesCount.toString() }
                            td { +item.commentsCount.toString() }
                            td { +item.rating.toString() }
                        }
                    }
                }
            }
        }
        return htmlContent
    }
}

fun longToLocalDate(timestamp: Long): LocalDate {
    val instant = Instant.ofEpochSecond(timestamp)
    return instant.atZone(ZoneId.systemDefault()).toLocalDate()
}

fun List<News>.getMostRatedNews(count: Int, period: ClosedRange<LocalDate>): List<News> {
    val filteredNews = this.filter {
        logger.info("Дата ${it.publicationDate} входит в период: $period")
        longToLocalDate(it.publicationDate) in period
    }
    val sortedNews = filteredNews.sortedByDescending {
        it.rating
    }
    return sortedNews.take(count)
}

