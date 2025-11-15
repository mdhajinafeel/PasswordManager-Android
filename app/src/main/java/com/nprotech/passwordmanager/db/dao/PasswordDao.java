package com.nprotech.passwordmanager.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.passwordmanager.db.entities.PasswordEntity;
import com.nprotech.passwordmanager.model.PasswordModel;

import java.util.List;

@Dao
public interface PasswordDao {

    @Query("SELECT pwd.passwordStrength AS passwordStrength, pwd.id AS id, pwd.databaseId AS databaseId, pwd.timeStamp AS timeStamp, pwd.applicationName AS applicationName, pwd.userName AS userName, pwd.password AS password, pwd.iconId AS iconId," +
            "pwd.isCustomIcon AS isCustomIcon, pwd.isFavourite AS isFavourite, pwd.category AS categoryId, ctg.categoryName AS category," +
            "CASE WHEN pwd.isCustomIcon = 1 THEN pwd.icon ELSE icn.icon END AS icon " +
            "FROM passwords pwd " +
            "INNER JOIN categories ctg ON ctg.id = pwd.category " +
            "LEFT JOIN icons icn ON icn.id = pwd.iconId " +
            "ORDER BY pwd.id DESC")
    List<PasswordModel> getPasswords();

    @Query("SELECT pwd.passwordStrength AS passwordStrength, pwd.id AS id, pwd.databaseId AS databaseId, pwd.timeStamp AS timeStamp, pwd.applicationName AS applicationName, pwd.userName AS userName, pwd.password AS password, pwd.iconId AS iconId," +
            "pwd.isCustomIcon AS isCustomIcon, pwd.isFavourite AS isFavourite, pwd.category AS categoryId, ctg.categoryName AS category," +
            "CASE WHEN pwd.isCustomIcon = 1 THEN pwd.icon ELSE icn.icon END AS icon " +
            "FROM passwords pwd " +
            "INNER JOIN categories ctg ON ctg.id = pwd.category " +
            "LEFT JOIN icons icn ON icn.id = pwd.iconId WHERE timeStamp = :timeStamp")
    PasswordModel getPasswordModel(long timeStamp);

    @Query("SELECT * FROM passwords WHERE timeStamp = :timeStamp")
    PasswordEntity getPassword(long timeStamp);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPassword(PasswordEntity passwords);

    @Query("UPDATE passwords SET isFavourite = :isFavourite WHERE timeStamp = :timeStamp")
    void updateFavourite(long timeStamp, boolean isFavourite);

    @Query("UPDATE passwords SET icon = :icon, iconId = :iconId, isCustomIcon = :isCustomIcon, password = :password, " +
            "category = :category, link = :link, userName = :userName, " +
            "applicationName = :applicationName WHERE timeStamp = :timeStamp")
    int updatedPassword(long timeStamp, String applicationName, String userName, String link, int category, String password, boolean isCustomIcon,
                         int iconId, byte[] icon);
}