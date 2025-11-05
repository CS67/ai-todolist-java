package com.example.tasks.database;

import androidx.room.TypeConverter;

import com.example.tasks.data.Priority;
import com.example.tasks.data.SubTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Room类型转换器
 */
public class Converters {
    private static final Gson gson = new Gson();
    
    @TypeConverter
    public static String fromPriority(Priority priority) {
        return priority.name();
    }
    
    @TypeConverter
    public static Priority toPriority(String priorityName) {
        return Priority.valueOf(priorityName);
    }
    
    @TypeConverter
    public static String fromSubTaskList(List<SubTask> subTasks) {
        return gson.toJson(subTasks);
    }
    
    @TypeConverter
    public static List<SubTask> toSubTaskList(String subTasksJson) {
        Type listType = new TypeToken<List<SubTask>>(){}.getType();
        List<SubTask> result = gson.fromJson(subTasksJson, listType);
        return result != null ? result : new ArrayList<>();
    }
}