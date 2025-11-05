package com.example.tasks.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Todo数据访问对象
 */
@Dao
public interface TodoDao {
    
    /**
     * 获取所有待办事项
     */
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    LiveData<List<TodoEntity>> getAllTodos();
    
    /**
     * 根据ID获取待办事项
     */
    @Query("SELECT * FROM todos WHERE id = :id")
    TodoEntity getTodoById(String id);
    
    /**
     * 获取未完成的待办事项
     */
    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY priority DESC, dueDate ASC")
    LiveData<List<TodoEntity>> getIncompleteTodos();
    
    /**
     * 获取已完成的待办事项
     */
    @Query("SELECT * FROM todos WHERE isCompleted = 1 ORDER BY completedAt DESC")
    LiveData<List<TodoEntity>> getCompletedTodos();
    
    /**
     * 插入新的待办事项
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTodo(TodoEntity todo);
    
    /**
     * 更新待办事项
     */
    @Update
    void updateTodo(TodoEntity todo);
    
    /**
     * 删除待办事项
     */
    @Delete
    void deleteTodo(TodoEntity todo);
    
    /**
     * 根据ID删除待办事项
     */
    @Query("DELETE FROM todos WHERE id = :id")
    void deleteTodoById(String id);
    
    /**
     * 删除所有已完成的待办事项
     */
    @Query("DELETE FROM todos WHERE isCompleted = 1")
    void deleteCompletedTodos();
    
    /**
     * 获取待办事项总数
     */
    @Query("SELECT COUNT(*) FROM todos")
    int getTodoCount();
    
    /**
     * 获取未完成待办事项数量
     */
    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 0")
    int getIncompleteCount();
    
    /**
     * 获取已完成待办事项数量
     */
    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 1")
    int getCompletedCount();
}