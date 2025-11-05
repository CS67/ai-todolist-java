package com.example.tasks.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tasks.data.Priority;
import com.example.tasks.data.SubTask;
import com.example.tasks.data.Todo;
import com.example.tasks.repository.TodoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Todo列表的ViewModel
 */
public class TodoViewModel extends ViewModel {
    private final TodoRepository repository;
    private final ExecutorService executor;
    
    // LiveData for UI
    private final LiveData<List<Todo>> allTodos;
    private final LiveData<Integer> totalCount;
    private final LiveData<Integer> completedCount;
    private final LiveData<Integer> incompleteCount;
    
    // Dialog state
    private final MutableLiveData<Boolean> showAddDialog = new MutableLiveData<>(false);
    private final MutableLiveData<Todo> editingTodo = new MutableLiveData<>(null);
    
    public TodoViewModel(TodoRepository repository) {
        this.repository = repository;
        this.executor = Executors.newFixedThreadPool(4);
        
        // 初始化LiveData
        this.allTodos = repository.getAllTodos();
        this.totalCount = Transformations.map(allTodos, List::size);
        this.completedCount = Transformations.map(allTodos, todos -> {
            int count = 0;
            for (Todo todo : todos) {
                if (todo.isCompleted()) count++;
            }
            return count;
        });
        this.incompleteCount = Transformations.map(allTodos, todos -> {
            int count = 0;
            for (Todo todo : todos) {
                if (!todo.isCompleted()) count++;
            }
            return count;
        });
    }
    
    // Getters for LiveData
    public LiveData<List<Todo>> getAllTodos() {
        return allTodos;
    }
    
    public LiveData<Integer> getTotalCount() {
        return totalCount;
    }
    
    public LiveData<Integer> getCompletedCount() {
        return completedCount;
    }
    
    public LiveData<Integer> getIncompleteCount() {
        return incompleteCount;
    }
    
    public LiveData<Boolean> getShowAddDialog() {
        return showAddDialog;
    }
    
    public LiveData<Todo> getEditingTodo() {
        return editingTodo;
    }
    
    /**
     * 添加新的Todo
     */
    public void addTodo(String title, String description, Priority priority, Long dueDate, List<SubTask> subTasks) {
        if (title == null || title.trim().isEmpty()) return;
        
        Todo newTodo = new Todo(title.trim(), 
                               description != null ? description.trim() : "", 
                               priority, dueDate, subTasks);
        
        executor.execute(() -> repository.insertTodo(newTodo));
    }
    
    /**
     * 切换Todo完成状态
     */
    public void toggleTodoCompletion(String todoId) {
        executor.execute(() -> {
            Todo todo = repository.getTodoById(todoId);
            if (todo != null) {
                Todo updatedTodo = todo.copyWith(
                    null, null, !todo.isCompleted(), null, null, null,
                    !todo.isCompleted() ? System.currentTimeMillis() : null
                );
                repository.updateTodo(updatedTodo);
            }
        });
    }
    
    /**
     * 删除Todo
     */
    public void deleteTodo(String todoId) {
        executor.execute(() -> repository.deleteTodoById(todoId));
    }
    
    /**
     * 显示添加对话框
     */
    public void showAddDialog() {
        showAddDialog.setValue(true);
    }
    
    /**
     * 隐藏添加对话框
     */
    public void hideAddDialog() {
        showAddDialog.setValue(false);
    }
    
    /**
     * 开始编辑Todo
     */
    public void startEditingTodo(String todoId) {
        executor.execute(() -> {
            Todo todo = repository.getTodoById(todoId);
            editingTodo.postValue(todo);
        });
    }
    
    /**
     * 取消编辑Todo
     */
    public void cancelEditingTodo() {
        editingTodo.setValue(null);
    }
    
    /**
     * 更新Todo
     */
    public void updateTodo(String todoId, String title, String description, 
                          Priority priority, Long dueDate, List<SubTask> subTasks) {
        if (title == null || title.trim().isEmpty()) return;
        
        executor.execute(() -> {
            Todo todo = repository.getTodoById(todoId);
            if (todo != null) {
                Todo updatedTodo = todo.copyWith(
                    title.trim(), 
                    description != null ? description.trim() : "",
                    null, priority, dueDate, subTasks, null
                );
                repository.updateTodo(updatedTodo);
            }
        });
        editingTodo.setValue(null);
    }
    
    /**
     * 切换子任务完成状态
     */
    public void toggleSubTaskCompletion(String todoId, String subTaskId) {
        executor.execute(() -> {
            Todo todo = repository.getTodoById(todoId);
            if (todo != null) {
                List<SubTask> updatedSubTasks = new ArrayList<>();
                for (SubTask subTask : todo.getSubTasks()) {
                    if (subTask.getId().equals(subTaskId)) {
                        updatedSubTasks.add(subTask.copyWith(null, !subTask.isCompleted()));
                    } else {
                        updatedSubTasks.add(subTask);
                    }
                }
                repository.updateTodo(todo.copyWith(null, null, null, null, null, updatedSubTasks, null));
            }
        });
    }
    
    /**
     * 添加子任务
     */
    public void addSubTask(String todoId, String subTaskTitle) {
        if (subTaskTitle == null || subTaskTitle.trim().isEmpty()) return;
        
        executor.execute(() -> {
            Todo todo = repository.getTodoById(todoId);
            if (todo != null) {
                List<SubTask> updatedSubTasks = new ArrayList<>(todo.getSubTasks());
                updatedSubTasks.add(new SubTask(subTaskTitle.trim()));
                repository.updateTodo(todo.copyWith(null, null, null, null, null, updatedSubTasks, null));
            }
        });
    }
    
    /**
     * 清除所有已完成的Todo
     */
    public void clearCompletedTodos() {
        executor.execute(() -> repository.deleteCompletedTodos());
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
    
    /**
     * TodoViewModel工厂类
     */
    public static class Factory implements ViewModelProvider.Factory {
        private final TodoRepository repository;
        
        public Factory(TodoRepository repository) {
            this.repository = repository;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(TodoViewModel.class)) {
                return (T) new TodoViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}