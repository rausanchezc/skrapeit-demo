import it.skrape.core.htmlDocument
import it.skrape.fetcher.*
import it.skrape.selects.eachLink
import it.skrape.selects.eachText
import it.skrape.selects.html5.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID
import kotlin.math.floor
import kotlin.random.Random

fun main() = runBlocking {
    val result = getProperties()

    result.forEach{ (title, link) -> launch {
        val url = "https://www.idealista.com$link"
        println("$title ==> $url")
        val property = getPropertyDetail(url)
        println(property)
        delay(Random(3485).nextLong())
        }
    }
}

fun getHeaders(): Map<String, String> {
    val headers = listOf(
        mapOf(
            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
            "Accept-Encoding" to "gzip, deflate, br",
            "Accept-Language" to "en-US,en;q=0.9",
            "Sec-Ch-Ua" to "\"Chromium\";v=\"92\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"92\"",
            "Sec-Ch-Ua-Mobile" to "?0",
            "Sec-Fetch-Dest" to "document",
            "Sec-Fetch-Mode" to "navigate",
            "Sec-Fetch-Site" to "none",
            "Sec-Fetch-User" to "?1",
            "Upgrade-Insecure-Requests" to "1",
            "User-Agent" to "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36",
        ),
        mapOf(
            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
            "Accept-Encoding" to "gzip, deflate, br",
            "Accept-Language" to "en-US,en;q=0.5",
            "Sec-Fetch-Dest" to "document",
            "Sec-Fetch-Mode" to "navigate",
            "Sec-Fetch-Site" to "none",
            "Sec-Fetch-User" to "?1",
            "Upgrade-Insecure-Requests" to "1",
            "User-Agent" to "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv to90.0) Gecko/20100101 Firefox/90.0",
        )
    )

    return headers[floor(Random.nextDouble() * headers.size).toInt()]
}

fun getProperties(): Map<String, String> {
    return skrape(BrowserFetcher) {
        request {
            url = "https://www.idealista.com/venta-viviendas/navalcarnero/la-dehesa-el-pinar/con-precio-hasta_300000,de-cuatro-cinco-habitaciones-o-mas,garaje,trastero/"
            headers = getHeaders()
        }
        response {
            htmlDocument {
                div {
                    withClass = "item-info-container"
                    a {
                        withClass = "item-link"
                        findAll {
                            eachLink
                        }
                    }
                }
            }
        }
    }
}

fun getPropertyDetail(url: String): Property {
    return skrape(BrowserFetcher) {
        request {
            this.url = url
            headers = getHeaders()
        }

        response {
            val platformId = url.removeSuffix("/").substringAfterLast('/')
            htmlDocument {
                relaxed = true

                val prices = section {
                    withClass = "price-features__container"
                    p {
                        findAll {
                            withClass = "flex-feature-details"
                            eachText
                        }
                    }
                }.take(2)
                    .map { it.substringAfter(':').trim() }

               val description = div {
                   withClass = "comment"
                   p {
                       findFirst {
                           text
                       }
                   }.trim()
               }

                val details = div {
                    withClass = "details-property_features"
                    li {
                        findAll {
                            eachText
                        }
                    }
                }

                val lastUpdate = p {
                    withClass = "stats-text"
                    findFirst { text }
                }

                val location = div {
                    withId = "headerMap"
                    li {
                        findAll {
                            withClass = "header-map-list"
                            eachText
                        }
                    }
                }

                val agencyId = p {
                    withClass = "txt-ref"
                    findFirst { text }
                        .substringAfter(':')
                        .trim()
                }

                val title = h1 {
                    span {
                        withClass = "main-info__title-main"
                        findFirst {
                            text
                        }
                    }
                }

                Property(
                    platformId = platformId,
                    agencyId = agencyId,
                    title = title,
                    description = description,
                    price = Price( price = prices[0], ratioM2 = prices[1]),
                    details = details,
                    lastUpdate = lastUpdate,
                    location = location.toSet()
                )
            }
        }
    }
}

data class Property(
    val uuid: UUID = UUID.randomUUID(),
    val platformId: String,
    val agencyId: String = "(None)",
    val title: String = "(Empty)",
    val description: String,
    val price: Price,
    val details: List<String> = listOf(),
    val lastUpdate: String,
    val location: Set<String> = setOf()
    )

data class Price(val price: String, val ratioM2: String)

