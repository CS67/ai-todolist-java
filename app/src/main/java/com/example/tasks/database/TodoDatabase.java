package com.example.tasks.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Room数据库配置类
 */
@Database(
    entities = {TodoEntity.class},
    version = 3,  // 增加版本号
    exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class TodoDatabase extends RoomDatabase {
    
    public abstract TodoDao todoDao();
    
    private static volatile TodoDatabase INSTANCE;
    
    public static TodoDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (TodoDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            TodoDatabase.class,
                            "todo_database"
                    )
                    .fallbackToDestructiveMigration()  // 简单起见，直接重建数据库
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}