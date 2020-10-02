import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MainTest {
    @Test
    fun whenNoCommand() {
        val ret = mainFunc(arrayOf<String>())
        assertNotEquals(ret, 0)
    }

    @Test
    fun whenNoFile() {
        val ret = mainFunc(arrayOf<String>(UUID.randomUUID().toString()))
        assertNotEquals(ret, 0)
    }
}
