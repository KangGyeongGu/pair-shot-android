package com.pairshot.core.ads.util

import com.pairshot.core.model.PhotoPair

private const val PAIRS_PER_AD = 6
private const val MIN_PAIRS_FOR_ADS = 7

fun buildPairListWithAds(
    pairs: List<PhotoPair>,
    adFree: Boolean,
): List<PairListItem> {
    if (adFree || pairs.size < MIN_PAIRS_FOR_ADS) {
        return pairs.map { PairListItem.Pair(it) }
    }

    val result = mutableListOf<PairListItem>()
    var adSlotIndex = 0
    pairs.forEachIndexed { index, pair ->
        result.add(PairListItem.Pair(pair))
        val isBoundary = (index + 1) % PAIRS_PER_AD == 0
        val hasMoreAfter = index < pairs.lastIndex
        if (isBoundary && hasMoreAfter) {
            result.add(PairListItem.Ad(adSlotIndex))
            adSlotIndex += 1
        }
    }
    return result
}
