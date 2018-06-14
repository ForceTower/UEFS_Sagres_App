package com.forcetower.uefs.db;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.migration.Migration;
import androidx.annotation.NonNull;

import timber.log.Timber;

/**
 * Created by João Paulo on 29/03/2018.
 */
public class DatabaseMigrations {
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Timber.d("Executing migration 1 -> 2");
            database.execSQL("ALTER TABLE DisciplineClassLocation ADD COLUMN class_group TEXT");
            database.execSQL("ALTER TABLE DisciplineClassLocation ADD COLUMN class_code TEXT");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Timber.d("Executing migration 2 -> 3");
            database.execSQL("CREATE TABLE IF NOT EXISTS `CalendarEvent` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `calendar_id` TEXT, `event_id` TEXT, `semester` TEXT)");
        }
    };

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Timber.d("Executing migration 3 -> 4");
            database.execSQL("ALTER TABLE DisciplineClassItem ADD COLUMN class_material_link TEXT");
            database.execSQL("CREATE TABLE IF NOT EXISTS DisciplineClassMaterialLink (uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, class_id INTEGER NOT NULL, name TEXT, link TEXT)");
            Timber.d("Migration 3 -> 4 Executed");
        }
    };

    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Timber.d("Executing migration 4 -> 5");
            database.execSQL("DROP TABLE Profile");
            database.execSQL("CREATE TABLE IF NOT EXISTS ProfileUNES (uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT, score REAL NOT NULL, last_sync INTEGER NOT NULL, last_sync_attempt INTEGER NOT NULL)");
            Timber.d("Migration 4 -> 5 Executed");
        }
    };
}
