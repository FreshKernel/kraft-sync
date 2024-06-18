package utils

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilsTest {
    @Test
    fun `test convertBytesToReadableMegabytesAsString`() {
        assertEquals(
            // 0.09999942779541016 in megabytes
            104857L.convertBytesToReadableMegabytesAsString(),
            "0.10",
        )

        assertEquals(
            // 1.0536737442016602 in megabytes
            1104857L.convertBytesToReadableMegabytesAsString(),
            "1",
        )
    }
}
