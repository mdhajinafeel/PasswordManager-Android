package com.nprotech.passwordmanager.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.passwordmanager.db.entities.IconEntity;

import java.util.List;

@Dao
public interface IconDao {

    @Query("SELECT * FROM icons ORDER BY id ASC")
    List<IconEntity> getAllIcons();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIcons(List<IconEntity> icons);

    @Query("DELETE FROM icons")
    void clearAll();
}