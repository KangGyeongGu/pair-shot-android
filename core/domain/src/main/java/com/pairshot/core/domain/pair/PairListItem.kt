package com.pairshot.core.domain.pair

import com.pairshot.core.model.PhotoPair

sealed interface PairListItem {
    data class Pair(
        val pair: PhotoPair,
    ) : PairListItem

    data class Ad(
        val slotIndex: Int,
    ) : PairListItem
}
