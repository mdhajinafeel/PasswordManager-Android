package com.nprotech.passwordmanager.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.passwordmanager.db.entities.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY id ASC")
    List<CategoryEntity> getAllCategories();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategories(List<CategoryEntity> categories);

    @Query("DELETE FROM categories")
    void clearAll();
}