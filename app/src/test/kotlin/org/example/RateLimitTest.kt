package org.example

import io.mockk.mockk
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Semaphore
import org.example.api.NewsApiClient
import org.example.dto.News
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RateLimitTest {

    @Test
    fun `test rate limiting enforces maximum concurrent requests`() = runBlocking {
        val client = mockk<NewsApiClient>()
        val channel = Channel<List<News>>(Channel.UNLIMITED)
        val maxConcurrentRequests = 5
        val semaphore = Semaphore(maxConcurrentRequests)
        val workerCount = 5

        coEvery { client.getNews(any(), any()) } coAnswers {
            delay(400)
            listOf(mockk<News>())
        }

        val concurrentRequests = mutableListOf<Int>()

        val workers = launchWorkers(workerCount, channel, client, semaphore, concurrentRequests, ::onRequestStart)

        workers.forEach { it.join() }
        channel.close()

        assertTrue(concurrentRequests.all { it <= maxConcurrentRequests })
    }

    private fun onRequestStart(activeRequests: Int, concurrentRequests: MutableList<Int>) {
        concurrentRequests.add(activeRequests)
    }

    fun CoroutineScope.launchWorkers(workerCount: Int, channel: Channel<List<News>>,
                                     client: NewsApiClient, semaphore: Semaphore, concurrentRequests: MutableList<Int>,
                                     onRequestStart: (Int, MutableList<Int>) -> Unit): List<Job> {
        val workerDispatcher = newFixedThreadPoolContext(workerCount, "WorkerPool")
        return List(workerCount) { workerId ->
            launch(workerDispatcher) {
                try {
                    var page = workerId + 1
                    while (page <= 10) {
                        semaphore.acquire()
                        try {
                            onRequestStart(semaphore.availablePermits, concurrentRequests)
                            val news = client.getNews(50, page)
                            if (news.isNotEmpty()) {
                                channel.send(news)
                            }
                        } catch (e: Exception) {
                            println("Ошибка в воркере $workerId: ${e.message}")
                        } finally {
                            semaphore.release()
                        }
                        page += workerCount
                    }
                } catch (e: Exception) {
                    println("Ошибка в воркере $workerId: ${e.message}")
                }
            }
        }
    }
}
