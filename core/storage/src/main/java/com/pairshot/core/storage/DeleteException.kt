package com.pairshot.core.storage

class DeleteException(
    val result: DeleteResult,
) : Exception("MediaStore delete failed: $result")
