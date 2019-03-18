package me.fru1t.lwnetmon

import me.fru1t.lwnetmon.database.impl.FileBandwidthDatabase
import java.io.File
import java.util.regex.Pattern

fun main(args: Array<String>) {
  val dev = File("/proc/net/dev")
  if (!dev.exists()) {
    println("Couldn't find network file at '/proc/net/dev")
    return
  }

  val xDb = FileBandwidthDatabase(File("bandwidth-database.db"))

  //             |   Receive                                                     |  Transmit
  // 1 interface |2bytes 3packets 4errs 5drop 6fifo 7frame 8compressed 9multicast|10bytes 11packets 12errs 13drop 14fifo 15colls 16carrier 17compressed
  val format = Pattern.compile("\\s*([^:]+): (\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)")
  val lInterface = "enp30s0"
  var lastRx = 0
  var lastTx = 0
  var currentRx = 0
  var currentTx = 0
  var deltaRx = 0
  var deltaTx = 0

  val baselineTimestampMs = System.nanoTime() / 1000 / 1000
  var currentLoopTime: Long
  var loopTimeDrift: Long

  while (!Thread.interrupted()) {
    currentLoopTime = System.nanoTime()
    loopTimeDrift = (currentLoopTime - baselineTimestampMs) / 1000 / 1000 % 1000

    dev.forEachLine {
      val matcher = format.matcher(it)
      if (!matcher.matches()) {
        return@forEachLine
      }
      if (matcher.group(1) != lInterface) {
        return@forEachLine
      }

      currentRx = matcher.group(2).toInt()
      currentTx = matcher.group(10).toInt()
      deltaRx = (currentRx - lastRx)
      deltaTx = (currentTx - lastTx)

      println(
        "Drift: $loopTimeDrift, RX: "
            + toHumanReadableString(deltaRx)
            + ", TX: "
            + toHumanReadableString(deltaTx))
      xDb.addNextTick(deltaRx, deltaTx)

      lastRx = currentRx
      lastTx = currentTx
    }

    if (loopTimeDrift < 0) {
      loopTimeDrift = 0
    }
    Thread.sleep(1000 - loopTimeDrift)
  }
}

fun toHumanReadableString(bytes: Int): String {
  if (bytes < 1024) {
    return "$bytes B/s"
  }
  if (bytes < 1024 * 1024) {
    return "${bytes/1024} KB/s"
  }
  if (bytes < 1024 * 1024 * 1024) {
    return "${bytes/1024/1024} MB/s"
  }
  return "${bytes/1024/1024/1024} GB/s"
}
