package org.example

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Semaphore
import org.example.api.NewsApiClient
import org.example.api.getMostRatedNews
import org.example.api.saveNews
import org.example.dto.News
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.time.LocalDate
import java.util.Properties

val logger = LoggerFactory.getLogger("Main")
fun main() = runBlocking {
    val client = NewsApiClient()
    val channel = Channel<List<News>>(Channel.UNLIMITED)
    val workerCount = 3
    val conf = loadConf()
    val semaphore = Semaphore(conf.getProperty("maxConcurrentRequests").toInt())
    val workers = launchWorkers(workerCount, channel, client, semaphore)
    val processorJob = launchProcessor(channel)
    workers.forEach { it.join() }

    channel.close()
    processorJob.join()

    logger.info("Данные успешно сохранены в файлы.")
}

fun loadConf(): Properties {
    val props = Properties();
    val confPath = "app/src/main/resources/config.properties"
    FileInputStream(confPath).use { stream -> props.load(stream) }
    return props
}

fun CoroutineScope.launchWorkers(workerCount: Int, channel: Channel<List<News>>,
                                 client: NewsApiClient, semaphore: Semaphore): List<Job> {
    val workerDispatcher = newFixedThreadPoolContext(workerCount, "WorkerPool")

    return List(workerCount) { workerId ->
        launch(workerDispatcher) {
            try {
                var page = workerId + 1
                while (page <= 10) {
                    semaphore.acquire()
                    logger.info("Воркер {}, страница {}", workerId, page)
                    try {
                        val news = client.getNews(50, page)
                        if (news.isNotEmpty()) {
                            channel.send(news)
                        }
                    } catch (e: Exception) {
                        logger.error("Ошибка в воркере $workerId: ${e.message}")
                    } finally {
                        semaphore.release()
                    }
                    page += workerCount
                }
            } catch (e: Exception) {
                logger.error("Ошибка в воркере $workerId: ${e.message}")
            }
        }
    }
}


fun CoroutineScope.launchProcessor(channel: Channel<List<News>>): Job = launch(Dispatchers.IO) {
    val allNewsList = mutableListOf<News>()

    for (newsList in channel) {
        allNewsList.addAll(newsList)
    }

    val startDate = LocalDate.of(2024, 8, 1)
    val endDate = LocalDate.of(2024, 10, 10)
    val period = startDate..endDate
    val mostRatedNews = allNewsList.getMostRatedNews(20, period)

    saveNews("app/src/main/resources/mostRatedNews.csv", mostRatedNews)
    saveNews("app/src/main/resources/allNews.csv", allNewsList)

    logger.info("Новости сохранены в файлы allNews.csv и mostRatedNews.csv")
}