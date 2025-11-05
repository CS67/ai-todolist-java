package com.example.tasks.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.tasks.data.Todo;
import com.example.tasks.database.TodoDao;
import com.example.tasks.database.TodoEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Todo数据仓库
 */
public class TodoRepository {
    private final TodoDao todoDao;
    private final ExecutorService executor;
    
    public TodoRepository(TodoDao todoDao) {
        this.todoDao = todoDao;
        this.executor = Executors.newFixedThreadPool(4);
    }
    
    /**
     * 获取所有待办事项
     */
    public LiveData<List<Todo>> getAllTodos() {
        return Transformations.map(todoDao.getAllTodos(), entities -> {
            List<Todo> todos = new ArrayList<>();
            for (TodoEntity entity : entities) {
                todos.add(entityToTodo(entity));
            }
            return todos;
        });
    }
    
    /**
     * 根据ID获取待办事项
     */
    public Todo getTodoById(String id) {
        TodoEntity entity = todoDao.getTodoById(id);
        return entity != null ? entityToTodo(entity) : null;
    }
    
    /**
     * 获取未完成的待办事项
     */
    public LiveData<List<Todo>> getIncompleteTodos() {
        return Transformations.map(todoDao.getIncompleteTodos(), entities -> {
            List<Todo> todos = new ArrayList<>();
            for (TodoEntity entity : entities) {
                todos.add(entityToTodo(entity));
            }
            return todos;
        });
    }
    
    /**
     * 获取已完成的待办事项
     */
    public LiveData<List<Todo>> getCompletedTodos() {
        return Transformations.map(todoDao.getCompletedTodos(), entities -> {
            List<Todo> todos = new ArrayList<>();
            for (TodoEntity entity : entities) {
                todos.add(entityToTodo(entity));
            }
            return todos;
        });
    }
    
    /**
     * 插入新的待办事项
     */
    public void insertTodo(Todo todo) {
        executor.execute(() -> todoDao.insertTodo(todoToEntity(todo)));
    }
    
    /**
     * 更新待办事项
     */
    public void updateTodo(Todo todo) {
        executor.execute(() -> todoDao.updateTodo(todoToEntity(todo)));
    }
    
    /**
     * 删除待办事项
     */
    public void deleteTodo(Todo todo) {
        executor.execute(() -> todoDao.deleteTodo(todoToEntity(todo)));
    }
    
    /**
     * 根据ID删除待办事项
     */
    public void deleteTodoById(String id) {
        executor.execute(() -> todoDao.deleteTodoById(id));
    }
    
    /**
     * 删除所有已完成的待办事项
     */
    public void deleteCompletedTodos() {
        executor.execute(() -> todoDao.deleteCompletedTodos());
    }
    
    /**
     * 获取待办事项总数
     */
    public int getTodoCount() {
        return todoDao.getTodoCount();
    }
    
    /**
     * 获取未完成待办事项数量
     */
    public int getIncompleteCount() {
        return todoDao.getIncompleteCount();
    }
    
    /**
     * 获取已完成待办事项数量
     */
    public int getCompletedCount() {
        return todoDao.getCompletedCount();
    }
    
    /**
     * TodoEntity转换为Todo
     */
    private Todo entityToTodo(TodoEntity entity) {
        return new Todo(
            entity.id,
            entity.title,
            entity.description,
            entity.isCompleted,
            entity.priority,
            entity.dueDate,
            entity.subTasks,
            entity.createdAt,
            entity.completedAt
        );
    }
    
    /**
     * Todo转换为TodoEntity
     */
    private TodoEntity todoToEntity(Todo todo) {
        return new TodoEntity(
            todo.getId(),
            todo.getTitle(),
            todo.getDescription(),
            todo.isCompleted(),
            todo.getPriority(),
            todo.getDueDate(),
            todo.getSubTasks(),
            todo.getCreatedAt(),
            todo.getCompletedAt()
        );
    }
}