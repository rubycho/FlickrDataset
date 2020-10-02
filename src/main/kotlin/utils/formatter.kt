package utils


/**
 * formats workspace path
 *
 * @param keyword value of Config.keyword
 *
 * @return workspace path
 */
fun fmtWsPath(keyword: String): String {
    return "./ws_$keyword"
}

/**
 * formats image path based on index
 *
 * @param wsPath string made with fmtWsPath
 * @param idx index of image
 * @param zw padding with
 *
 * @return image path
 */
fun fmtImgPath(wsPath: String, idx: Int, zw: Int): String {
    val imgIdx = idx.toString().padStart(zw, '0')
    return "$wsPath/$imgIdx.jpg"
}
