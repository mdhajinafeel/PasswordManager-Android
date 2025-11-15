package com.nprotech.passwordmanager.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.nprotech.passwordmanager.BuildConfig;
import com.nprotech.passwordmanager.db.dao.CategoryDao;
import com.nprotech.passwordmanager.db.dao.IconDao;
import com.nprotech.passwordmanager.db.dao.PasswordDao;
import com.nprotech.passwordmanager.db.dao.SchedulerDao;
import com.nprotech.passwordmanager.db.entities.CategoryEntity;
import com.nprotech.passwordmanager.db.entities.IconEntity;
import com.nprotech.passwordmanager.db.entities.PasswordEntity;
import com.nprotech.passwordmanager.db.entities.SchedulerEntity;
import com.nprotech.passwordmanager.utils.AppLogger;
import com.nprotech.passwordmanager.utils.SecureKeyManager;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

@Database(entities = {CategoryEntity.class, IconEntity.class, PasswordEntity.class, SchedulerEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final String DB_NAME = "password_manager_nprotech.db";

    public abstract CategoryDao categoryDao();

    public abstract IconDao iconDao();

    public abstract PasswordDao passwordDao();

    public abstract SchedulerDao schedulerDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // ✅ Initialize SQLCipher
                    SQLiteDatabase.loadLibs(context);

                    // ✅ Get database passphrase
                    byte[] passphrase = getDatabasePassphrase(context);

                    // ✅ Create SupportFactory (optional for SQLCipher)
                    SupportFactory factory = new SupportFactory(passphrase);

                    // ✅ Build Room database
                    RoomDatabase.Builder<AppDatabase> builder = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME)
                            .fallbackToDestructiveMigration(true);

                    // ✅ Allow main thread queries in DEBUG only
                    if (BuildConfig.DEBUG) {
                        builder.allowMainThreadQueries();
                    }

                    // ✅ Optionally use SQLCipher
                    // builder.openHelperFactory(factory);

                    INSTANCE = builder.build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Returns the database passphrase based on build type.
     */
    private static byte[] getDatabasePassphrase(Context context) {
        try {
            if (BuildConfig.DEBUG) {
                return SQLiteDatabase.getBytes("npro@2025".toCharArray());
            } else {
                return SecureKeyManager.getDatabasePassphrase(context);
            }
        } catch (Exception e) {
            AppLogger.e(AppDatabase.class, "getDatabasePassphrase", e);
            return new byte[0];
        }
    }
}