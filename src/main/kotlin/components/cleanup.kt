package components

import org.apache.commons.io.FileUtils
import utils.Config
import utils.fmtWsPath
import java.io.File


/**
 * cleanup workspace
 *
 * @param c Config Instance
 */
fun cleanup(c: Config) {
    FileUtils.deleteDirectory(
        File(fmtWsPath(c.keyword))
    )
}
