package com.pairshot.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileNameGenerator
    @Inject
    constructor() {
        private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        private val timeFormat = SimpleDateFormat("HHmmss", Locale.US)

        fun generateBeforeFileName(sequenceNumber: Int): String {
            val now = Date()
            val date = dateFormat.format(now)
            val time = timeFormat.format(now)
            return "BEFORE_%03d_%s_%s.jpg".format(sequenceNumber, date, time)
        }

        fun generateAfterFileName(sequenceNumber: Int): String {
            val now = Date()
            val date = dateFormat.format(now)
            val time = timeFormat.format(now)
            return "AFTER_%03d_%s_%s.jpg".format(sequenceNumber, date, time)
        }

        fun generatePairFileName(sequenceNumber: Int): String {
            val now = Date()
            val date = dateFormat.format(now)
            val time = timeFormat.format(now)
            return "PAIR_%03d_%s_%s.jpg".format(sequenceNumber, date, time)
        }
    }
