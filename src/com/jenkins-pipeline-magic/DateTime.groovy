package com.jenkins-pipeline-magic

import java.text.SimpleDateFormat

/**
 * Return the current time formatted
 *
 * Optionally add some number of minutes to the time 
 * generated with minutesOffset.
 *
 * @return Long Unix timestamp
 */
def formatCurrentTime(format="h:mm", minutesOffset=0) {
    def dateFormat = new SimpleDateFormat(format)
    def date = new Date()
    if (minutesOffset) {
    date = new Date(
      System.currentTimeMillis() + minutesOffset * 60 * 1000
    )
  }
  return dateFormat.format(date)
}

/**
 * Return the current unix timestamp
 *
 * @return Long Unix timestamp
 */
def getUnixTimestamp() {
  def date = new Date()
  return Math.round(date.getTime() / 1000)
}
