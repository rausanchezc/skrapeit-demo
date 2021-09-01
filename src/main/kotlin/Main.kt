import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.div

fun main() {
    val result = skrape(HttpFetcher) {
        request {
            url = "https://github.com/rausanchezc"
        }
        response {
            htmlDocument{
                div {
                    withClass = "user-profile-bio"
                    findFirst { text }
                }

            }
        }
    }
    print(result)
}