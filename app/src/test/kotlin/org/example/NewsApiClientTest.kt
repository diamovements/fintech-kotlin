package org.example

import kotlinx.coroutines.test.runTest
import org.example.api.NewsApiClient
import org.example.api.getMostRatedNews
import org.example.api.saveNews
import org.junit.jupiter.api.BeforeEach
import java.io.File
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NewsApiClientTest {

    private val client = NewsApiClient()

    @BeforeEach
    fun setup() {
        NewsApiClient()
    }

    @Test
    fun `get news testing`() = runTest {
        val newsList = client.getNews(5, 1);
        assertEquals(5, newsList.size)
    }

    @Test
    fun `get most rated news testing`() = runTest {
        val newsList = client.getNews(5, 1)
        val mostRatedList = newsList.getMostRatedNews(5, LocalDate.of(2020, 1, 1)..LocalDate.of(2024, 12, 31))
        assertEquals(5, mostRatedList.size)
        assertEquals(0.5, mostRatedList[0].rating)
    }

    @Test
    fun `save news testing`() = runTest {
        val newsList = client.getNews(5, 1)
        val file = File("test.csv")
        saveNews(file.path, newsList)
        assertTrue(file.exists())
    }
}
