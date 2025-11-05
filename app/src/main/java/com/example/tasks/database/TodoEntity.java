package com.example.tasks.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.tasks.data.Priority;
import com.example.tasks.data.SubTask;

import java.util.List;

/**
 * Todo数据库实体
 */
@Entity(tableName = "todos")
@TypeConverters(Converters.class)
public class TodoEntity {
    @PrimaryKey
    @NonNull
    public String id;
    
    public String title;
    public String description;
    public boolean isCompleted;
    public Priority priority;
    public Long dueDate;
    public List<SubTask> subTasks;
    public long createdAt;
    public Long completedAt;

    public TodoEntity() {
    }

    @Ignore
    public TodoEntity(String id, String title, String description, boolean isCompleted,
                     Priority priority, Long dueDate, List<SubTask> subTasks,
                     long createdAt, Long completedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
        this.priority = priority;
        this.dueDate = dueDate;
        this.subTasks = subTasks;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }
}