package com.pairshot.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pairshot.data.local.db.entity.ProjectEntity
import com.pairshot.data.local.db.entity.ProjectWithCountsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query(
        """
        SELECT
            p.*,
            (
                SELECT COUNT(*)
                FROM photo_pairs pp
                WHERE pp.projectId = p.id
            ) AS pairCount,
            (
                SELECT COUNT(*)
                FROM photo_pairs pp
                WHERE pp.projectId = p.id
                AND pp.status IN ('PAIRED', 'COMBINED')
            ) AS completedCount,
            (
                SELECT COUNT(*)
                FROM photo_pairs pp
                WHERE pp.projectId = p.id
                AND pp.status = 'COMBINED'
            ) AS combinedCount
        FROM projects p
        ORDER BY p.updatedAt DESC
        """,
    )
    fun getAll(): Flow<List<ProjectWithCountsEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: Long): ProjectEntity?

    @Insert
    suspend fun insert(project: ProjectEntity): Long

    @Update
    suspend fun update(project: ProjectEntity)

    @Query("UPDATE projects SET updatedAt = :timestamp WHERE id = :id")
    suspend fun updateTimestamp(
        id: Long,
        timestamp: Long,
    )

    @Delete
    suspend fun delete(project: ProjectEntity)
}
