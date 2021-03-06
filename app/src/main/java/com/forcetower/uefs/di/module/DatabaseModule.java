package com.forcetower.uefs.di.module;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.forcetower.uefs.db.AppDatabase;
import com.forcetower.uefs.db.dao.AccessDao;
import com.forcetower.uefs.db.dao.CalendarEventDao;
import com.forcetower.uefs.db.dao.CalendarItemDao;
import com.forcetower.uefs.db.dao.DisciplineClassItemDao;
import com.forcetower.uefs.db.dao.DisciplineClassLocationDao;
import com.forcetower.uefs.db.dao.DisciplineClassMaterialLinkDao;
import com.forcetower.uefs.db.dao.DisciplineDao;
import com.forcetower.uefs.db.dao.DisciplineGroupDao;
import com.forcetower.uefs.db.dao.DisciplineMissedClassesDao;
import com.forcetower.uefs.db.dao.GradeDao;
import com.forcetower.uefs.db.dao.GradeInfoDao;
import com.forcetower.uefs.db.dao.GradeSectionDao;
import com.forcetower.uefs.db.dao.MessageDao;
import com.forcetower.uefs.db.dao.MessageUNESDao;
import com.forcetower.uefs.db.dao.ProfileDao;
import com.forcetower.uefs.db.dao.SemesterDao;
import com.forcetower.uefs.db.dao.TodoItemDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_10_11;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_11_12;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_12_13;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_13_14;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_14_15;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_15_16;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_16_17;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_17_18;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_18_19;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_19_20;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_1_2;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_2_3;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_3_4;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_4_5;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_5_6;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_6_7;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_7_8;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_8_9;
import static com.forcetower.uefs.db.DatabaseMigrations.MIGRATION_9_10;

/**
 * Created by João Paulo on 07/03/2018.
 */
@Module
public class DatabaseModule {
    @Provides
    @Singleton
    AppDatabase provideDatabase(Application application) {
        return Room.databaseBuilder(application, AppDatabase.class, "unes_uefs_5.db")
                .addMigrations(
                        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                        MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
                        MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17,
                        MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20)
                .build();
    }

    @Singleton
    @Provides
    AccessDao provideAccessDao(AppDatabase database) {
        return database.accessDao();
    }

    @Singleton
    @Provides
    CalendarItemDao provideCalendarItemDao(AppDatabase database) {
        return database.calendarItemDao();
    }

    @Singleton
    @Provides
    DisciplineClassItemDao provideDisciplineClassItemDao(AppDatabase database) {
        return database.disciplineClassItemDao();
    }

    @Singleton
    @Provides
    DisciplineClassLocationDao provideDisciplineLocationDao(AppDatabase database) {
        return database.disciplineClassLocationDao();
    }

    @Provides
    @Singleton
    DisciplineDao provideDisciplineDao(AppDatabase database) {
        return database.disciplineDao();
    }

    @Singleton
    @Provides
    DisciplineGroupDao provideDisciplineGroupDao(AppDatabase database) {
        return database.disciplineGroupDao();
    }

    @Singleton
    @Provides
    GradeDao provideGradeDao(AppDatabase database) {
        return database.gradeDao();
    }

    @Singleton
    @Provides
    GradeInfoDao provideGradeInfoDao(AppDatabase database) {
        return database.gradeInfoDao();
    }

    @Singleton
    @Provides
    GradeSectionDao provideGradeSectionDao(AppDatabase database) {
        return database.gradeSectionDao();
    }

    @Provides
    @Singleton
    MessageDao provideMessageDao(AppDatabase database) {
        return database.messageDao();
    }

    @Singleton
    @Provides
    ProfileDao provideProfileDao(AppDatabase database) {
        return database.profileDao();
    }

    @Singleton
    @Provides
    SemesterDao provideSemesterDao(AppDatabase database) {
        return database.semesterDao();
    }

    @Singleton
    @Provides
    TodoItemDao provideTodoItemDao(AppDatabase database) {
        return database.todoItemDao();
    }

    @Singleton
    @Provides
    CalendarEventDao provideCalendarEventDao(AppDatabase database) {
        return database.calendarEventDao();
    }

    @Provides
    @Singleton
    DisciplineClassMaterialLinkDao provideDisciplineClassMaterialLinkDao(AppDatabase database) {
        return database.disciplineClassMaterialLinkDao();
    }

    @Provides
    @Singleton
    DisciplineMissedClassesDao provideDisciplineMissedClassesDao(AppDatabase database) {
        return database.disciplineMissedClassesDao();
    }

    @Provides
    @Singleton
    MessageUNESDao provideMessageUNESDao(AppDatabase database) {
        return database.messageUNESDao();
    }
}
