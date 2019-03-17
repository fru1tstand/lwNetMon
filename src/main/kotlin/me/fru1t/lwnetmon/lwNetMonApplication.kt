package me.fru1t.lwnetmon

import java.io.File
import java.util.regex.Pattern

fun main(args: Array<String>) {
  val dev = File("/proc/net/dev")
  if (!dev.exists()) {
    println("Couldn't find network file at '/proc/net/dev")
    return
  }

  //             |   Receive                                                     |  Transmit
  // 1 interface |2bytes 3packets 4errs 5drop 6fifo 7frame 8compressed 9multicast|10bytes 11packets 12errs 13drop 14fifo 15colls 16carrier 17compressed
  val format = Pattern.compile("\\s*([^:]+): (\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)")
  val lInterface = "enp30s0"
  var lastRx = 0L
  var lastTx = 0L
  var currentRx = 0L
  var currentTx = 0L
  var deltaRx = 0L
  var deltaTx = 0L

  while (true) {
    dev.forEachLine {
      val matcher = format.matcher(it)
      if (!matcher.matches()) {
        return@forEachLine
      }
      if (matcher.group(1) != lInterface) {
        return@forEachLine
      }

      currentRx = matcher.group(2).toLong()
      currentTx = matcher.group(10).toLong()
      deltaRx = (currentRx - lastRx)
      deltaTx = (currentTx - lastTx)

      println("RX: " + toHumanReadableString(deltaRx) + ", TX: " + toHumanReadableString(deltaTx))

      lastRx = currentRx
      lastTx = currentTx
    }

    Thread.sleep(1000)
  }
}

fun toHumanReadableString(bytes: Long): String {
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
