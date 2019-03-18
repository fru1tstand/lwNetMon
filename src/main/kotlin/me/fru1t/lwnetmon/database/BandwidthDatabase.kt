package me.fru1t.lwnetmon.database

/** Stores, retrieves, modifies, or deletes transmission metrics, most notably, bandwidth usage. */
interface BandwidthDatabase {
  /**
   * Stores the amount of bytes received and sent in the next sequential row in the database. No
   * restriction is imposed on the frequency that this method is called as a tick may represent
   * varying quantities of time depending on user's choice.
   *
   * @return the row id that is used as referenced internal to this database.
   */
  fun addNextTick(deltaRxBytes: Int, deltaTxBytes: Int): Int
}
