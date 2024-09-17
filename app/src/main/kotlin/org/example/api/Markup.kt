package org.example.api

import org.example.dto.News
import java.io.File
import java.lang.StringBuilder

fun createMarkup(init: Markup.() -> Unit): String {
    val markup = Markup()
    markup.init()
    return markup.toString()
}

fun Markup.newsList(news: Collection<News>) {
    body {
        news.forEach {
            h1 { +it.title }

            p { +"Описание: ${it.description}"
                br()
                +"Дата публикации: ${longToLocalDate(it.publicationDate)}"
                br()
                +"Количество лайков: ${it.favoritesCount}"
                br()
                +"Количество комментариев: ${it.commentsCount}"
                br()
                +"Рейтинг: ${it.rating}"
                br()
            }
            a(href = it.siteUrl) { +"Подробности по ссылке" }
        }
    }
}

class Markup {
    private val content = StringBuilder()

    fun header(init: Header.() -> Unit) {
        val header = Header()
        header.init()
        content.append("<head>\n")
            .append(indent(header.getContent(), 1))
            .append("</head>\n")
    }
    fun body(init: Body.() -> Unit) {
        val body = Body()
        body.init()
        content.append("<body>\n")
            .append(indent(body.getContent(), 1))
            .append("</body>\n")
    }

    fun getContent(): String {
        val str = StringBuilder()
        str.append("<!DOCTYPE html>\n")
            .append(indent(content.toString(), 1))
        return "$str</html>"
    }

    override fun toString(): String {
        return getContent()
    }
    private fun indent(text: String, level: Int): String {
        val indentation = "\t".repeat(level)
        return text
            .lineSequence()
            .joinToString("\n") {line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("</")) {
                    "\t".repeat(level - 1) + line
                }
                else {
                    indentation + line
                }
            }
    }
}

class Header {
    private val content = StringBuilder()

    fun title(init: Title.() -> Unit) {
        val title = Title()
        title.init()
        content.append("<title>").append(title.getContent()).append("</title>\n")
    }

    fun getContent(): String {
        return content.toString()
    }
}

class Title {
    private val content = StringBuilder()

    operator fun String.unaryPlus() {
        content.append(this)
    }

    fun getContent(): String {
        return content.toString()
    }
}

class Body {
    private val content = StringBuilder()

    fun h1(init: H1.() -> Unit) {
        val h1 = H1()
        h1.init()
        content.append("<h1>").append(h1.getContent()).append("</h1>\n")
    }

    fun p(init: P.() -> Unit) {
        val p = P()
        p.init()
        content.append("<p>").append(p.getContent()).append("</p>\n")
    }

    fun a(href: String, init: A.() -> Unit) {
        val a = A(href)
        a.init()
        content.append("<a href=\"$href\">").append(a.getContent()).append("</a>\n")
    }
    fun getContent(): String {
        return content.toString()
    }
}

class H1 {
    private val content = StringBuilder()

    operator fun String.unaryPlus() {
        content.append(this)
    }

    fun getContent(): String {
        return content.toString()
    }
}

class P {
    private val content = StringBuilder()

    operator fun String.unaryPlus() {
        content.append(this)
    }

    fun getContent(): String {
        return content.toString()
    }

    fun br() {
        content.append("<br/>")
    }
}

class A(private val href: String) {
    private val content = StringBuilder()

    operator fun String.unaryPlus() {
        content.append(this)
    }

    fun getContent(): String {
        return content.toString()
    }
}

fun saveNews(path: String, news: Collection<News>) {
    val file = File(path)
    if (file.exists()) {
        throw IllegalArgumentException("Файл $path уже существует")
    }
    val writer = file.bufferedWriter()
    val content = createMarkup {
        header {
            title { +"Новости"}
        }
        newsList(news)
    }
    writer.write(content)
    writer.close()
}