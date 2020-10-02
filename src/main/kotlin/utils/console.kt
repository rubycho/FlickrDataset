package utils


/**
 * prints [msg] with warn prefix
 */
fun consoleWarn(msg: String) {
    println("[WARN] $msg")
}

/**
 * prints progress text based on [task], [curr], [total]
 *
 * @param task task name which will be put at the end
 * @param curr current idx
 * @param total total idx
 */
fun consoleProgress(task: String, curr: Int, total: Int) {
    println("Current: $curr / Total: $total [$task]")
}
