package com.nprotech.passwordmanager.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.passwordmanager.db.entities.IconEntity;
import com.nprotech.passwordmanager.db.entities.SchedulerEntity;

import java.util.List;

@Dao
public interface SchedulerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertScheduler(SchedulerEntity schedulerEntity);

    @Query("DELETE FROM scheduler")
    void clearAll();

    @Query("UPDATE scheduler SET status = :status WHERE apiId = :apiId")
    void updatedScheduler(int apiId, boolean status);
}