package components

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.json.JSONObject
import utils.*
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.OutputStream
import javax.imageio.IIOException
import javax.imageio.ImageIO
import kotlin.math.pow


/**
 * Downloader class
 *
 * @param _c Config Instance
 *
 * @property c saves _c
 * @property wsPath path to workspace
 * @property client HttpClient instance
 *
 * @property pageUrls list which contain urls of json
 * @property imgUrls list which contain urls of downloadable image
 * @property imgPaths list which contain paths of saved image
 *
 * @constructor create workspace directory and call download()
 *  the constructor will throw if creation fails
 * @throws Exception
 */
class Downloader(_c: Config) {
    private val c = _c
    private val wsPath = fmtWsPath(_c.keyword)

    private val client = HttpClients.createDefault()

    private val pageUrls = ArrayList<String>()
    private val imgUrls = ArrayList<String>()
    private val imgPaths = ArrayList<String>()

    init {
        if (!File(wsPath).mkdir())
            throw Exception("workspace already exists: $wsPath")
        download()
    }

    /**
     * download pipeline
     */
    private fun download() {
        fillPageUrls()
        fillImgUrls()
        downloadImgs()
        processImgs()

        consoleWarn("Download complete")
        consoleWarn("number of saved images may vary with printed total;")
        consoleWarn("cause images with CMYK color are deleted.")
    }

    /**
     * util function for sending request
     * if stream given & request is successful,
     * the request body will be saved to the stream, and <true, null> will be returned
     *
     * @param url request url
     * @param stream output stream (optional)
     *
     * @returns <whether succeeded, request body (null if stream != null)>
     */
    private fun sendRequest(url: String, stream: OutputStream? = null): Pair<Boolean, String?> {
        val httpGet = HttpGet(url)
        val resp = client.execute(httpGet)

        var result = false
        var body: String? = null

        try {
            val statusCode = resp.statusLine.statusCode
            val entity = resp.entity
            if (200 <= statusCode || statusCode < 400) {
                result = true
                if (stream != null) entity.writeTo(stream)
                else body = EntityUtils.toString(entity)
            }
            EntityUtils.consume(entity)
        } finally {
            resp.close()
        }
        return Pair(result, body)
    }

    /**
     * fills [pageUrls]
     */
    private fun fillPageUrls() {
        pageUrls.clear()
        for (i in c.sp..c.ep) {
            pageUrls.add(
                "https://www.flickr.com/services/rest/" +
                        "?method=flickr.photos.search" +
                        "&api_key=${c.apiKey}" +
                        "&text=${c.keyword}" +
                        "&sort=relevance" +
                        "&extras=url_z" +
                        "&per_page=100" +
                        "&page=$i" +
                        "&format=json" +
                        "&nojsoncallback=1"
            )
        }
    }

    /**
     * fills [imgUrls] with [pageUrls]
     */
    private fun fillImgUrls() {
        val taskName = "Fetch image urls"
        val taskLength = pageUrls.size

        imgUrls.clear()
        for ((idx, url) in pageUrls.withIndex()) {
            consoleProgress(taskName, idx + 1, taskLength)

            val ret = sendRequest(url)
            if (!ret.first) {
                consoleWarn("request failed: $url")
                continue
            }

            val json = JSONObject(ret.second)
            val stat = json.getString("stat")
            if (stat == "fail") {
                consoleWarn("Flickr responsed as: ${json.getString("message")}")
                continue
            }

            val page = json.getJSONObject("photos").getInt("page")
            if ((c.sp + idx) != page) {
                consoleWarn("stopping at page: ${c.sp + idx}")
                break
            }

            val photos = json.getJSONObject("photos").getJSONArray("photo")
            for (photo in photos)
                if ((photo as JSONObject).has("url_z"))
                    imgUrls.add(photo.getString("url_z"))
        }
    }

    /**
     * download images, fill [imgPaths]
     */
    private fun downloadImgs() {
        val taskName = "Download images"
        val taskLength = imgUrls.size
        var zw = 1; while (10.0.pow(zw.toDouble()) <= taskLength) zw++

        imgPaths.clear()
        for ((idx, url) in imgUrls.withIndex()) {
            consoleProgress(taskName, idx + 1, taskLength)

            val imgPath = fmtImgPath(wsPath, idx, zw)
            val imgFile = File(imgPath); imgFile.createNewFile()

            val ret = sendRequest(url, imgFile.outputStream())
            if (!ret.first) consoleWarn("request failed: $url")
            else imgPaths.add(imgPath)
        }
    }

    /**
     * resize downloaded images
     */
    private fun processImgs() {
        val taskName = "Scale images"
        val taskLength = imgPaths.size

        for ((idx, path) in imgPaths.withIndex()) {
            consoleProgress(taskName, idx + 1, taskLength)

            val imgFile = File(path)
            val img: BufferedImage

            /* JPEG CMYK issue */
            try {
                img = ImageIO.read(imgFile)
            } catch (e: IIOException) {
                consoleWarn("invalid jpg type detected.")
                imgFile.delete(); continue
            }
            val scaledImg = img.getScaledInstance(
                c.size, c.size, Image.SCALE_SMOOTH
            )

            val bfImg = BufferedImage(
                c.size, c.size, BufferedImage.TYPE_INT_RGB
            )
            val g = bfImg.createGraphics()
            g.drawImage(scaledImg, 0, 0, null)
            g.dispose()

            ImageIO.write(bfImg, "jpg", imgFile)
        }
    }
}
