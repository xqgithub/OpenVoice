package com.shannon.openvoice.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DraftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(draft: DraftModel): Long

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM DraftModel WHERE accountId = :accountId ORDER BY id DESC")
    fun loadDrafts(accountId: String): LiveData<List<DraftModel>>

    @Query("DELETE FROM DraftModel WHERE id = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM DraftModel WHERE id = :id")
    fun find(id: Int): DraftModel?
}
