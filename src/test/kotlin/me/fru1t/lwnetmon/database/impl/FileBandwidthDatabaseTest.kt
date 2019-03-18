package me.fru1t.lwnetmon.database.impl

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.io.File

internal class FileBandwidthDatabaseTest {
  private companion object {
    private const val TEST_DB_DIRECTORY = "test"
    private val TEST_DB_FILE: File = File("$TEST_DB_DIRECTORY/bandwidth.db")

    /** Returns a new instance of a test [FileBandwidthDatabase]. */
    private fun createTestBandwidthDatabase(): FileBandwidthDatabase =
      FileBandwidthDatabase(TEST_DB_FILE)
  }

  @AfterEach
  fun tearDown() {
    File(TEST_DB_DIRECTORY).deleteRecursively()
  }

  @Test
  fun constructor_requiresDirectory() {
    File(TEST_DB_DIRECTORY).mkdir()

    try {
      FileBandwidthDatabase(File(TEST_DB_DIRECTORY))
      fail("Expecting an IllegalArgumentException due to path being a directory.")
    } catch (e: IllegalArgumentException) {
      // expected
    }
  }

  @Test
  fun constructor_createsDatabaseFile() {
    createTestBandwidthDatabase()
    assertThat(TEST_DB_FILE.exists()).isTrue()
  }

  @Test
  fun addNextTick() {
    val db = createTestBandwidthDatabase()
    val testDeltaRx = 300L
    val testDeltaTx = 400L

    val resultRow = db.addNextTick(testDeltaRx, testDeltaTx)

    assertThat(resultRow).isEqualTo(1)
    assertThat(TEST_DB_FILE.readLines(Charsets.US_ASCII))
      .containsExactly("$resultRow,$testDeltaRx,$testDeltaTx")
  }
}
