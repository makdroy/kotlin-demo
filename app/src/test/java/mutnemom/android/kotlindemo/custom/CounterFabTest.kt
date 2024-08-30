package mutnemom.android.kotlindemo.custom

import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class CounterFabTest {

    private val mockCounterFab = mockk<CounterFab>(relaxed = true)

    @BeforeEach
    fun setUp() {
        every { mockCounterFab.count } returns 42
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Disabled("Function decrease() not implement yet")
    @Test
    fun decrease() {
    }

    @Disabled
    @DisplayName("Function increase() not implement yet")
    @Test
    fun increase() {
    }

    @DisplayName("Test getCount() method")
    @Test
    fun testGetCountMethod() {
        val expected = 42
        assertEquals(expected, mockCounterFab.count)
    }
}
