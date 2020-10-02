import components.Downloader
import components.Reviewer
import components.cleanup
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import utils.Config


const val DOWNLOAD_COMMAND = "download"
const val REVIEW_COMMAND = "review"
const val CLEANUP_COMMAND = "cleanup"

/**
 * help function
 *
 * prints help text
 */
fun help(): Int {
    println("Usage:")
    println("\tdownload -c [path to config.ini]")
    println("\treview -c [path to config.ini]")
    println("\tcleanup -c [path to config.ini]")
    println("* c option is optional. The default value is ./config.ini.")

    return -1
}

/**
 * main function
 *
 * check commands, options
 * and call appropriate components
 *
 * @return status code (for testing)
 */
fun mainFunc(args: Array<String>): Int {
    if (args.isEmpty()) return help()

    val commands = listOf(
        DOWNLOAD_COMMAND,
        REVIEW_COMMAND,
        CLEANUP_COMMAND
    )
    if (args[0] !in commands) return help()

    val parser = DefaultParser()
    val options = Options()
    options.addOption(
        "c", true,
        "path to config.ini; default is ./config.ini"
    )

    val cmd = parser.parse(options, args)
    val confPath = cmd.getOptionValue("c") ?: "./config.ini"

    val conf = Config(confPath)
    conf.print()

    when (args[0]) {
        DOWNLOAD_COMMAND -> Downloader(conf)
        REVIEW_COMMAND -> Reviewer(conf)
        CLEANUP_COMMAND -> cleanup(conf)
        else -> return help()
    }
    return 0
}

fun main(args: Array<String>) {
    mainFunc(args)
}
