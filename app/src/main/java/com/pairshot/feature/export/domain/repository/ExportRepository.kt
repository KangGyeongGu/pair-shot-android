package com.pairshot.feature.export.domain.repository

import com.pairshot.feature.settings.domain.model.WatermarkConfig

interface ExportRepository {
    /**
     * 사진 쌍을 ZIP으로 내보낸다 (기기 저장 — SAF URI).
     *
     * @param pairIds 내보낼 PhotoPair ID 목록
     * @param outputUri SAF ACTION_CREATE_DOCUMENT에서 받은 출력 파일 URI (String — domain layer Uri 금지)
     * @param includeBefore before/ 폴더 포함 여부
     * @param includeAfter after/ 폴더 포함 여부
     * @param includeCombined combined/ 폴더 포함 여부
     * @param onProgress (current, total) — 각 파일 처리 후 호출
     */
    suspend fun exportZip(
        pairIds: List<Long>,
        outputUri: String,
        includeBefore: Boolean,
        includeAfter: Boolean,
        includeCombined: Boolean,
        watermarkConfig: WatermarkConfig? = null,
        onProgress: (current: Int, total: Int) -> Unit,
    )

    /**
     * 공유용 임시 ZIP을 cacheDir에 생성한다.
     *
     * @param pairIds 내보낼 PhotoPair ID 목록
     * @param projectName ZIP 파일명에 사용할 프로젝트명
     * @param includeBefore before/ 폴더 포함 여부
     * @param includeAfter after/ 폴더 포함 여부
     * @param includeCombined combined/ 폴더 포함 여부
     * @param onProgress (current, total) — 각 파일 처리 후 호출
     * @return 생성된 임시 ZIP 파일의 절대경로
     */
    suspend fun createShareableZip(
        pairIds: List<Long>,
        projectName: String,
        includeBefore: Boolean,
        includeAfter: Boolean,
        includeCombined: Boolean,
        watermarkConfig: WatermarkConfig? = null,
        onProgress: (current: Int, total: Int) -> Unit,
    ): String

    /**
     * 공유용 이미지를 cacheDir에 복사하고 FileProvider URI 목록을 반환한다.
     * MediaStore URI 대신 FileProvider URI를 사용해야 ShareSheet에서 원본 화질 미리보기가 가능.
     *
     * @param pairIds 대상 PhotoPair ID 목록
     * @param includeBefore Before 이미지 포함 여부
     * @param includeAfter After 이미지 포함 여부
     * @param includeCombined Combined 이미지 포함 여부
     * @param onProgress (current, total) — 각 파일 복사 후 호출
     * @return FileProvider URI String 목록
     */
    suspend fun prepareShareableImages(
        pairIds: List<Long>,
        includeBefore: Boolean,
        includeAfter: Boolean,
        includeCombined: Boolean,
        watermarkConfig: WatermarkConfig? = null,
        onProgress: (current: Int, total: Int) -> Unit,
    ): List<String>

    /**
     * 이미지를 개별적으로 갤러리(MediaStore)에 저장한다.
     *
     * @param pairIds 저장할 PhotoPair ID 목록
     * @param projectName Pictures/PairShot/{projectName}/ 경로에 사용
     * @param includeBefore Before 이미지 포함 여부
     * @param includeAfter After 이미지 포함 여부
     * @param includeCombined Combined 이미지 포함 여부
     * @param onProgress (current, total) — 각 파일 처리 후 호출
     */
    suspend fun saveImagesToGallery(
        pairIds: List<Long>,
        projectName: String,
        includeBefore: Boolean,
        includeAfter: Boolean,
        includeCombined: Boolean,
        watermarkConfig: WatermarkConfig? = null,
        onProgress: (current: Int, total: Int) -> Unit,
    )
}
