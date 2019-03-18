package me.fru1t.lwnetmon.database.impl

import me.fru1t.lwnetmon.database.BandwidthDatabase
import java.io.File
import java.lang.IllegalStateException
import java.util.regex.Pattern

/** A flat file-based implementation of [BandwidthDatabase]. */
class FileBandwidthDatabase(private val databaseFile: File) : BandwidthDatabase {
  private companion object {
    val rowPattern = Pattern.compile("(\\d+),(\\d+),(\\d+)")!!
  }

  private var currentIndex = 0

  init {
    if (databaseFile.isDirectory) {
      throw IllegalArgumentException(
        "FileBandwidthDatabase was supplied with a directory instead of a file: " +
            databaseFile.absolutePath
      )
    }

    if (!databaseFile.exists()) {
      databaseFile.parentFile.mkdirs()
      databaseFile.createNewFile()
    }

    var lastLine: String? = null
    databaseFile.forEachLine(Charsets.US_ASCII) {
      lastLine = it
    }

    if (lastLine != null) {
      val lastRowMatcher = rowPattern.matcher(lastLine)
      if (!lastRowMatcher.matches()) {
        throw IllegalStateException(
          "The database doesn't look like it's in a format I know how to read. Expecting " +
              "${rowPattern.pattern()}, but found $lastLine")
      }
      currentIndex = lastRowMatcher.group(1).toInt()
    }
  }

  override fun addNextTick(deltaRxBytes: Long, deltaTxBytes: Long): Int {
    databaseFile.appendText("${++currentIndex},$deltaRxBytes,$deltaTxBytes\n", Charsets.US_ASCII)
    return currentIndex
  }
}
