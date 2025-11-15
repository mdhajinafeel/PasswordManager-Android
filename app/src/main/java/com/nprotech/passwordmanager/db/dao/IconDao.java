package com.nprotech.passwordmanager.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.passwordmanager.db.entities.IconEntity;

import java.util.List;

@Dao
public interface IconDao {

    @Query("SELECT * FROM icons ORDER BY CASE WHEN name GLOB '[A-Za-z]*' THEN 1 WHEN name GLOB '[0-9]*' THEN 2 ELSE 3 END, name COLLATE NOCASE")
    List<IconEntity> getAllIcons();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIcons(List<IconEntity> icons);

    @Query("DELETE FROM icons")
    void clearAll();
}