package com.fit2081.alex_34662901_assignment3.data.seed

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO for reading and writing seed flags (e.g., CSV already seeded)
 */
@Dao
interface SeedFlagDao {

    /**
     * Fetch the flag by its key
     *
     * @param key flag key (e.g. "csv")
     */
    @Query("SELECT * FROM seed_flags WHERE `key` = :key")
    suspend fun getFlag(key: String): SeedFlag?

    /**
     * Insert or replace a flag to mark seeding status
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlag(flag: SeedFlag)
}
