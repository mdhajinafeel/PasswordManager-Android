package com.nprotech.passwordmanager.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.passwordmanager.db.entities.SchedulerEntity;

/**
 * Data Access Object for the scheduler table.
 */
@Dao
public interface SchedulerDao {

    /**
     * Insert a scheduler entry in the database. If the scheduler entry already exists, replace it.
     * @param schedulerEntity the scheduler entry to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertScheduler(SchedulerEntity schedulerEntity);

    /**
     * Delete all scheduler entries from the table.
     */
    @Query("DELETE FROM scheduler")
    void clearAll();

    /**
     * Updates the status of a scheduler entry.
     * @param apiId The ID of the API to update.
     * @param status The new status to set.
     */
    @Query("UPDATE scheduler SET status = :status WHERE apiId = :apiId")
    void updatedScheduler(int apiId, boolean status);
}
