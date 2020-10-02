package utils

import org.ini4j.Wini
import java.io.File


/**
 * Configuration Data class
 *
 * @param confPath path to ini file
 *
 * @property apiKey Flickr API Key
 * @property apiSecret Flickr API Secret
 * @property keyword Text to search
 * @property size Image size (128 <= size <= 640)
 * @property sp Start page
 * @property ep End page
 *
 * @constructor read ini file and map to property
 * @throws Exception
 */
class Config(confPath: String) {
    var apiKey = ""
    var apiSecret = ""
    var keyword = ""

    var size = 128
    var sp = 1
    var ep = 2

    init {
        val confFile = File(confPath)
        if (!confFile.exists())
            throw Exception("failed to read file: $confPath")

        val ini = Wini(File(confPath))

        apiKey = ini.get("run", "api_key") ?: ""
        apiSecret = ini.get("run", "api_secret") ?: ""
        keyword = ini.get("run", "keyword") ?: ""

        size = (ini.get("run", "size") ?: "128").toInt()
        sp = (ini.get("run", "sp") ?: "1").toInt()
        ep = (ini.get("run", "ep") ?: "2").toInt()

        if (size < 128) size = 128
        if (size > 640) size = 640
        if (ep - sp < 0)
            throw Exception("invalid range: start page $sp, end page $ep")
    }

    /**
     * prints properties
     */
    fun print() {
        println("========================================")
        println("api_key: $apiKey")
        println("api_secret: $apiSecret")
        println("keyword: $keyword")
        println("size: $size")
        println("start page: $sp")
        println("end page: $ep")
        println("========================================")
    }
}
