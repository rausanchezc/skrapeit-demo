import it.skrape.core.htmlDocument
import it.skrape.fetcher.*
import it.skrape.selects.and
import it.skrape.selects.eachLink
import it.skrape.selects.eachText
import it.skrape.selects.html5.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.floor
import kotlin.random.Random

fun main() = runBlocking {
    val result = getProperties()

    result.forEach{ (title, link) -> launch {
        val url = "https://www.idealista.com$link"
        println("$title ==> $url")
        getPropertyDetails(url)
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

fun getPropertyDetails(url: String) {
    val price = getPropertyPrice(url)
    println(price)
}

fun getPropertyPrice(url: String): List<String> {
    return skrape(BrowserFetcher) {
        request {
            this.url = url
            headers = getHeaders()
        }
        response {
            htmlDocument {
                val prices = section {
                    withClass = "price-features__container"
                    p {
                        findAll {
                            withClass = "flex-feature-details"
                            eachText
                        }
                    }
                }.take(2)
                prices.map { it -> it.substringAfter(':').trim() }
            }
        }
    }
}

data class PropertyPrice(var price: Double = 0.0, var ratioM2: Double = 0.0)

