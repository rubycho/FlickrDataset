package components

import utils.Config
import utils.consoleWarn
import utils.fmtWsPath
import java.awt.Component
import java.awt.FlowLayout
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*


const val FRAME_SIZE = 500
const val IMG_SIZE = 256
const val STATUS_PANEL_MARGIN = 10

/**
 * Reviewer class
 *
 * @param _c Config instance
 *
 * @property idx index state
 * @property pathLength length of [paths] after init
 *
 * @property paths list which contains image paths
 * @property history mark history of each image
 *
 * @constructor initialize swing window, check workspace dir
 *  if dir is empty or doesn't exists it will throw exception
 * @throws Exception
 */
class Reviewer(_c: Config) {
    private val markText = "Will be deleted: YES"
    private val unmarkText = "Will be deleted: NO"

    private var idx = 0
    private var pathLength = 0

    private val c = _c

    private val paths = ArrayList<String>()
    private val history = HashMap<String, Boolean>()

    private val frame = JFrame()

    private val statusLabel = JLabel(" ")
    private val pathLabel = JLabel(" ")
    private val markLabel = JLabel(" ")

    private val imgLabel = JLabel()

    private val markBtn = JButton("Mark/Unmark")
    private val nextBtn = JButton("Next")
    private val prevBtn = JButton("Prev")
    private val stopBtn = JButton("Stop")

    init {
        val wsPath = fmtWsPath(c.keyword)
        val wsDir = File(wsPath)
        if (!wsDir.exists())
            throw Exception("workspace doesn't exists: $wsPath")

        val files = wsDir.listFiles { _, name ->
            name.endsWith(".jpg")
        }
            ?: throw Exception("failed to list directory: $wsPath")
        if (files.isEmpty()) throw Exception("no image found")

        for (file in files) paths.add(file.canonicalPath)
        for (path in paths) history[path] = false

        pathLength = paths.size
        initWindow()
    }

    /**
     * initialize window, register callbacks
     */
    private fun initWindow() {
        frame.setSize(FRAME_SIZE, FRAME_SIZE)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        val statusPanel = JPanel()
        statusPanel.layout = BoxLayout(statusPanel, BoxLayout.Y_AXIS)
        statusPanel.border = BorderFactory.createEmptyBorder(
            STATUS_PANEL_MARGIN,
            STATUS_PANEL_MARGIN,
            STATUS_PANEL_MARGIN,
            STATUS_PANEL_MARGIN
        )
        statusPanel.add(statusLabel)
        statusPanel.add(pathLabel)
        statusPanel.add(markLabel)

        val imgPanel = JPanel()
        imgLabel.setSize(IMG_SIZE, IMG_SIZE)
        imgLabel.icon = ImageIcon(
            BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_RGB)
        )
        imgLabel.horizontalAlignment = SwingConstants.CENTER
        imgPanel.add(imgLabel)

        markBtn.addActionListener { onMark() }
        nextBtn.addActionListener { onNext() }
        prevBtn.addActionListener { onPrev() }
        stopBtn.addActionListener { onStop() }

        val btnPanel = JPanel()
        btnPanel.layout = FlowLayout()
        btnPanel.add(markBtn)
        btnPanel.add(nextBtn)
        btnPanel.add(prevBtn)
        btnPanel.add(stopBtn)

        val rootPanel = JPanel()
        rootPanel.layout = BoxLayout(rootPanel, BoxLayout.Y_AXIS)
        statusPanel.alignmentX = Component.LEFT_ALIGNMENT
        imgPanel.alignmentX = Component.LEFT_ALIGNMENT
        btnPanel.alignmentX = Component.LEFT_ALIGNMENT
        rootPanel.add(statusPanel)
        rootPanel.add(imgPanel)
        rootPanel.add(btnPanel)

        frame.add(rootPanel)
        frame.pack()
        frame.isVisible = true

        onClick()
    }

    private fun onMark() {
        val currPath = paths[idx]
        history[currPath] = !history[currPath]!!
        onClick()
    }

    /**
     * function to be called on next button clicked
     */
    private fun onNext() { idx++; onClick(); }

    /**
     * function to be called on prev button clicked
     */
    private fun onPrev() { idx--; onClick(); }

    /**
     * function to be called on stop button clicked
     */
    private fun onStop() {
        frame.isEnabled = false
        for (path in paths) {
            if (history[path] == true) {
                val file = File(path)
                if (file.delete()) println("Removed $path")
                else consoleWarn("Failed to remove $path")
            }
        }
        frame.dispatchEvent(WindowEvent(frame, WindowEvent.WINDOW_CLOSING))
    }

    /**
     * function to be called on any button click
     * this function should be called on on* functions, not directly
     */
    private fun onClick() {
        val currPath = paths[idx]

        statusLabel.text = "Current ${idx + 1} / Total $pathLength"
        pathLabel.text = "Path $currPath"
        markLabel.text = if (history[currPath] == true) markText else unmarkText

        val img = ImageIO.read(File(currPath))
        val scaledImg = img.getScaledInstance(IMG_SIZE, IMG_SIZE, BufferedImage.SCALE_AREA_AVERAGING)
        imgLabel.icon = ImageIcon(scaledImg)

        nextBtn.isEnabled = (idx + 1 < pathLength)
        prevBtn.isEnabled = (idx > 0)

        frame.revalidate()
    }
}
