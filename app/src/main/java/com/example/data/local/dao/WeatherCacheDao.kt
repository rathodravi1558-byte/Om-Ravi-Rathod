package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CachedWeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherCacheDao {

    @Query("SELECT * FROM cached_weather WHERE locationId = :locationId LIMIT 1")
    suspend fun getCachedWeather(locationId: String): CachedWeatherEntity?

    @Query("SELECT * FROM cached_weather WHERE locationId = :locationId LIMIT 1")
    fun observeCachedWeather(locationId: String): Flow<CachedWeatherEntity?>

    @Query("SELECT * FROM cached_weather ORDER BY cachedAtEpochMillis DESC")
    fun getAllCachedWeather(): Flow<List<CachedWeatherEntity>>

    @Query("SELECT * FROM cached_weather ORDER BY cachedAtEpochMillis DESC LIMIT 1")
    suspend fun getLatestCachedWeather(): CachedWeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedWeather(entity: CachedWeatherEntity)

    @Query("DELETE FROM cached_weather WHERE locationId = :locationId")
    suspend fun deleteCachedWeather(locationId: String)

    @Query("DELETE FROM cached_weather")
    suspend fun clearAllCache()
}
