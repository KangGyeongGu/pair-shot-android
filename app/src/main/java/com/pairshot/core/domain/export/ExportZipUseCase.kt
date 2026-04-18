package com.pairshot.core.domain.export

import javax.inject.Inject

class ExportZipUseCase
    @Inject
    constructor(
        private val exportRepository: ExportRepository,
    ) {
        suspend operator fun invoke(
            pairIds: List<Long>,
            outputUri: String,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            onProgress: (current: Int, total: Int) -> Unit,
        ) {
            require(includeBefore || includeAfter || includeCombined) {
                "최소 하나의 항목을 포함해야 합니다"
            }
            exportRepository.exportZip(
                pairIds = pairIds,
                outputUri = outputUri,
                includeBefore = includeBefore,
                includeAfter = includeAfter,
                includeCombined = includeCombined,
                onProgress = onProgress,
            )
        }
    }
