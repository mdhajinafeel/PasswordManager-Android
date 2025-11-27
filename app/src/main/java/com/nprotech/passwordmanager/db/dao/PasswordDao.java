package com.nprotech.passwordmanager.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.passwordmanager.db.entities.PasswordEntity;
import com.nprotech.passwordmanager.model.PasswordModel;

import java.util.List;

@Dao
public interface PasswordDao {

    @Query("SELECT pwd.link AS applicationLink, pwd.passwordStrength AS passwordStrength, pwd.databaseId AS databaseId, pwd.timeStamp AS timeStamp, pwd.applicationName AS applicationName, pwd.userName AS userName, pwd.password AS password, pwd.iconId AS iconId," +
            "pwd.isCustomIcon AS isCustomIcon, pwd.isFavourite AS isFavourite, pwd.category AS categoryId, ctg.categoryName AS category," +
            "CASE WHEN pwd.isCustomIcon = 1 THEN pwd.icon ELSE icn.icon END AS icon " +
            "FROM passwords pwd " +
            "INNER JOIN categories ctg ON ctg.id = pwd.category " +
            "LEFT JOIN icons icn ON icn.id = pwd.iconId " +
            "ORDER BY pwd.databaseId DESC")
    List<PasswordModel> getPasswords();

    @Query("SELECT pwd.link AS applicationLink, pwd.passwordStrength AS passwordStrength, pwd.databaseId AS databaseId, pwd.timeStamp AS timeStamp, pwd.applicationName AS applicationName, pwd.userName AS userName, pwd.password AS password, pwd.iconId AS iconId," +
            "pwd.isCustomIcon AS isCustomIcon, pwd.isFavourite AS isFavourite, pwd.category AS categoryId, ctg.categoryName AS category," +
            "CASE WHEN pwd.isCustomIcon = 1 THEN pwd.icon ELSE icn.icon END AS icon " +
            "FROM passwords pwd " +
            "INNER JOIN categories ctg ON ctg.id = pwd.category " +
            "LEFT JOIN icons icn ON icn.id = pwd.iconId " +
            "ORDER BY pwd.databaseId DESC")
    LiveData<List<PasswordModel>> getPasswordsLive();

    @Query("SELECT pwd.link AS applicationLink, pwd.passwordStrength AS passwordStrength, pwd.databaseId AS databaseId, pwd.timeStamp AS timeStamp, pwd.applicationName AS applicationName, pwd.userName AS userName, pwd.password AS password, pwd.iconId AS iconId," +
            "pwd.isCustomIcon AS isCustomIcon, pwd.isFavourite AS isFavourite, pwd.category AS categoryId, ctg.categoryName AS category," +
            "CASE WHEN pwd.isCustomIcon = 1 THEN pwd.icon ELSE icn.icon END AS icon " +
            "FROM passwords pwd " +
            "INNER JOIN categories ctg ON ctg.id = pwd.category " +
            "LEFT JOIN icons icn ON icn.id = pwd.iconId WHERE timeStamp = :timeStamp")
    PasswordModel getPasswordModel(long timeStamp);

    @Query("SELECT * FROM passwords WHERE timeStamp = :timeStamp")
    PasswordEntity getPassword(long timeStamp);

    @Query("SELECT pwd.link AS applicationLink, pwd.passwordStrength AS passwordStrength, pwd.databaseId AS databaseId, pwd.timeStamp AS timeStamp, pwd.applicationName AS applicationName, pwd.userName AS userName, pwd.password AS password, pwd.iconId AS iconId," +
            "pwd.isCustomIcon AS isCustomIcon, pwd.isFavourite AS isFavourite, pwd.category AS categoryId, ctg.categoryName AS category," +
            "CASE WHEN pwd.isCustomIcon = 1 THEN pwd.icon ELSE icn.icon END AS icon " +
            "FROM passwords pwd " +
            "INNER JOIN categories ctg ON ctg.id = pwd.category " +
            "LEFT JOIN icons icn ON icn.id = pwd.iconId WHERE categoryId = :categoryId " +
            "ORDER BY pwd.databaseId DESC")
    List<PasswordModel> getPasswordsByCategory(int categoryId);

    @Query("SELECT pwd.link AS applicationLink, pwd.passwordStrength AS passwordStrength, pwd.databaseId AS databaseId, pwd.timeStamp AS timeStamp, pwd.applicationName AS applicationName, pwd.userName AS userName, pwd.password AS password, pwd.iconId AS iconId," +
            "pwd.isCustomIcon AS isCustomIcon, pwd.isFavourite AS isFavourite, pwd.category AS categoryId, ctg.categoryName AS category," +
            "CASE WHEN pwd.isCustomIcon = 1 THEN pwd.icon ELSE icn.icon END AS icon " +
            "FROM passwords pwd " +
            "INNER JOIN categories ctg ON ctg.id = pwd.category " +
            "LEFT JOIN icons icn ON icn.id = pwd.iconId WHERE isFavourite = 1 " +
            "ORDER BY pwd.databaseId DESC")
    List<PasswordModel> getPasswordsFavorites();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPassword(PasswordEntity passwords);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPasswords(List<PasswordEntity> passwords);

    @Query("UPDATE passwords SET isFavourite = :isFavourite WHERE timeStamp = :timeStamp")
    void updateFavouriteDB(long timeStamp, boolean isFavourite);

    @Query("UPDATE passwords SET icon = :icon, iconId = :iconId, isCustomIcon = :isCustomIcon, password = :password, " +
            "category = :category, link = :link, userName = :userName, " +
            "applicationName = :applicationName WHERE timeStamp = :timeStamp")
    int updatedPassword(long timeStamp, String applicationName, String userName, String link, int category, String password, boolean isCustomIcon,
                         int iconId, byte[] icon);

    @Query("DELETE FROM passwords WHERE isSynced = 1 OR isDeleted = 1")
    void clearAll();
}