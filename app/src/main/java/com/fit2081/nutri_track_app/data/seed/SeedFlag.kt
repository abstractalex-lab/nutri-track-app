package com.fit2081.nutri_track_app.data.seed

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for tracking seed operations to prevent re-seeding
 *
 * @property key a string identifier for seed type (e.g. "csv")
 * @property seeded true if seeding is done
 *
 */
@Entity(tableName = "seed_flags")
data class SeedFlag(
    @PrimaryKey val key: String,  // e.g. "csv"
    val seeded: Boolean
)