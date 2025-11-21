package com.nprotech.passwordmanager.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.passwordmanager.db.entities.CategoryEntity;

import java.util.List;

/**
 * Data Access Object for the categories table.
 */
@Dao
public interface CategoryDao {

    /**
     * Get all categories from the table.
     * @return all categories.
     */
    @Query("SELECT c.id, C.iconText, C.categoryName, C.colorCode, COUNT(p.id) AS passwordCount FROM categories c " +
            "LEFT JOIN passwords p ON p.category = c.id " +
            "GROUP BY c.id " +
            "ORDER BY c.id ASC")
    List<CategoryEntity> getAllCategories();

    /**
     * Get category by id from the table.
     * @return category.
     */
    @Query("SELECT * FROM categories WHERE id = :id")
    CategoryEntity getCategoryById(long id);

    /**
     * Insert a list of categories in the database. If the category already exists, replace it.
     * @param categories the categories to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategories(List<CategoryEntity> categories);

    /**
     * Delete all categories from the table.
     */
    @Query("DELETE FROM categories")
    void clearAll();
}