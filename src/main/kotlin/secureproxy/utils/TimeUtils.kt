package secureproxy.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun unixTimestampToDateTime(unixTimestamp: Long): String {
    return Instant.ofEpochSecond(unixTimestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
}