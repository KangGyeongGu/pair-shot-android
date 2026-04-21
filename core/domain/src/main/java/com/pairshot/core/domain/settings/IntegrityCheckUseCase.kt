package com.pairshot.core.domain.settings

import com.pairshot.core.model.IntegrityResult

import com.pairshot.core.domain.pair.PhotoPairRepository
import javax.inject.Inject

class IntegrityCheckUseCase
    @Inject
    constructor(
        private val photoPairRepository: PhotoPairRepository,
    ) {
        suspend operator fun invoke(): IntegrityResult {
            val allPairs = photoPairRepository.getAll()
            val allUris =
                allPairs.flatMap { pair ->
                    listOfNotNull(pair.beforePhotoUri, pair.afterPhotoUri)
                }
            val existingUris = photoPairRepository.checkUrisExist(allUris)

            val orphanedIds =
                allPairs
                    .filter { pair ->
                        val beforeExists = pair.beforePhotoUri in existingUris
                        val afterExists = pair.afterPhotoUri?.let { it in existingUris } ?: true
                        !beforeExists || !afterExists
                    }.map { it.id }

            return IntegrityResult(
                total = allPairs.size,
                orphanedCount = orphanedIds.size,
                orphanedPairIds = orphanedIds,
            )
        }
    }
