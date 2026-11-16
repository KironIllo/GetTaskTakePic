package com.kironillo.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    private val databaseName = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migration1To2PreservesTasks() {
        helper.createDatabase(databaseName, 1).apply {
            execSQL(
                """
                INSERT INTO tasks (id, topic, difficulty, targetObject)
                VALUES ('task-1', 'animals', 'EASY', 'Find a cat')
                """.trimIndent(),
            )
            close()
        }

        val migratedDatabase = helper.runMigrationsAndValidate(
            databaseName,
            2,
            true,
            AppDatabase.MIGRATION_1_2,
        )

        val cursor = migratedDatabase.query(
            "SELECT id, topic, difficulty, targetObject, cachedAt FROM tasks",
        )

        cursor.use {
            assertEquals(1, it.count)
            it.moveToFirst()
            assertEquals("task-1", it.getString(0))
            assertEquals("animals", it.getString(1))
            assertEquals("EASY", it.getString(2))
            assertEquals("Find a cat", it.getString(3))
            assertEquals(0L, it.getLong(4))
        }

        migratedDatabase.close()
    }
}
