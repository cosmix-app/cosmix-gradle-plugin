package app.cosmix.gradle.utils.network

import java.net.HttpURLConnection
import java.net.URL
import java.io.InputStreamReader
import java.io.BufferedReader

object FaviconFetcher {
    fun fetchFavicon(websiteUrl: String): String? {
        try {
            val url = URL(websiteUrl)
            val domain = url.host
            val protocol = url.protocol
            val baseUrl = "$protocol://$domain"
            
            // 1. Try Google Favicon API
            val googleUrlStr = "https://www.google.com/s2/favicons?domain=$domain&sz=128"
            if (isImageValid(googleUrlStr)) {
                return googleUrlStr
            }

            // 2. Parse HTML
            val html = fetchHtml(websiteUrl)
            if (html != null) {
                val tags = listOf("apple-touch-icon", "icon", "shortcut icon")
                for (tag in tags) {
                    val match = extractIconHref(html, tag)
                    if (match != null) {
                        return resolveUrl(baseUrl, match)
                    }
                }
            }

            // 3. Fallback to /favicon.ico
            val fallbackUrl = "$baseUrl/favicon.ico"
            if (isImageValid(fallbackUrl)) {
                return fallbackUrl
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun isImageValid(urlString: String): Boolean {
        return try {
            val conn = URL(urlString).openConnection() as HttpURLConnection
            conn.requestMethod = "HEAD"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.responseCode in 200..299
        } catch (e: Exception) {
            false
        }
    }

    private fun fetchHtml(urlString: String): String? {
        return try {
            val conn = URL(urlString).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                reader.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun extractIconHref(html: String, relType: String): String? {
        val regex1 = Regex("""<link[^>]+rel=["']${relType}["'][^>]+href=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val regex2 = Regex("""<link[^>]+href=["']([^"']+)["'][^>]+rel=["']${relType}["']""", RegexOption.IGNORE_CASE)
        
        val match1 = regex1.find(html)
        if (match1 != null) return match1.groupValues[1]
        
        val match2 = regex2.find(html)
        if (match2 != null) return match2.groupValues[1]
        
        return null
    }

    private fun resolveUrl(baseUrl: String, href: String): String {
        return if (href.startsWith("http://") || href.startsWith("https://")) {
            href
        } else if (href.startsWith("//")) {
            "https:$href"
        } else if (href.startsWith("/")) {
            "$baseUrl$href"
        } else {
            "$baseUrl/$href"
        }
    }
}
