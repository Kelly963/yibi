package com.yibi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yibi.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 分类数据访问对象
 */
@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteById(categoryId: String)

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getById(categoryId: String): CategoryEntity?

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    suspend fun getAllList(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY sortOrder ASC")
    suspend fun getByType(type: String): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY sortOrder ASC")
    fun getByTypeFlow(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories WHERE isBuiltIn = 1")
    suspend fun getBuiltInCount(): Int

    @Query("UPDATE categories SET isHidden = 1 WHERE id = :categoryId")
    suspend fun hideCategory(categoryId: String)

    @Query("UPDATE categories SET isHidden = 0 WHERE id = :categoryId")
    suspend fun unhideCategory(categoryId: String)

    @Query("SELECT * FROM categories WHERE isHidden = 0 ORDER BY sortOrder ASC")
    fun getVisible(): Flow<List<CategoryEntity>>
}
