package com.pairshot.core.database

import androidx.room.TypeConverter
import com.pairshot.core.model.PairStatus

class Converters {
    @TypeConverter
    fun fromPairStatus(value: String): PairStatus = PairStatus.valueOf(value)

    @TypeConverter
    fun toPairStatus(status: PairStatus): String = status.name
}
