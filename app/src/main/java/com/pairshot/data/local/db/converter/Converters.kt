package com.pairshot.data.local.db.converter

import androidx.room.TypeConverter
import com.pairshot.feature.pair.domain.model.PairStatus

class Converters {
    @TypeConverter
    fun fromPairStatus(value: String): PairStatus = PairStatus.valueOf(value)

    @TypeConverter
    fun toPairStatus(status: PairStatus): String = status.name
}
