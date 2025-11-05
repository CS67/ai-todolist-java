package com.example.tasks.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Todo数据模型
 */
public class Todo {
    private String id;
    private String title;
    private String description;
    private boolean isCompleted;
    private Priority priority;
    private Long dueDate; // 截止日期时间戳
    private List<SubTask> subTasks;
    private long createdAt;
    private Long completedAt;

    public Todo() {
        this.id = UUID.randomUUID().toString();
        this.description = "";
        this.isCompleted = false;
        this.priority = Priority.MEDIUM;
        this.subTasks = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public Todo(String title) {
        this();
        this.title = title;
    }

    public Todo(String title, String description, Priority priority, Long dueDate, List<SubTask> subTasks) {
        this();
        this.title = title;
        this.description = description != null ? description : "";
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.dueDate = dueDate;
        this.subTasks = subTasks != null ? new ArrayList<>(subTasks) : new ArrayList<>();
    }

    public Todo(String id, String title, String description, boolean isCompleted, 
                Priority priority, Long dueDate, List<SubTask> subTasks, 
                long createdAt, Long completedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
        this.priority = priority;
        this.dueDate = dueDate;
        this.subTasks = subTasks != null ? new ArrayList<>(subTasks) : new ArrayList<>();
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public Priority getPriority() {
        return priority;
    }

    public Long getDueDate() {
        return dueDate;
    }

    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks); // Return defensive copy
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setDueDate(Long dueDate) {
        this.dueDate = dueDate;
    }

    public void setSubTasks(List<SubTask> subTasks) {
        this.subTasks = subTasks != null ? new ArrayList<>(subTasks) : new ArrayList<>();
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

    // Business logic methods
    
    /**
     * 获取已完成的子任务数量
     */
    public int getCompletedSubTasksCount() {
        int count = 0;
        for (SubTask subTask : subTasks) {
            if (subTask.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取子任务完成进度 (0.0 - 1.0)
     */
    public float getSubTaskProgress() {
        if (subTasks.isEmpty()) {
            return 1.0f;
        }
        return (float) getCompletedSubTasksCount() / subTasks.size();
    }

    /**
     * 检查是否过期
     */
    public boolean isOverdue() {
        return dueDate != null && dueDate < System.currentTimeMillis() && !isCompleted;
    }

    /**
     * 检查是否即将到期 (24小时内)
     */
    public boolean isDueSoon() {
        if (dueDate == null || isCompleted) {
            return false;
        }
        long now = System.currentTimeMillis();
        return dueDate > now && dueDate <= now + 24 * 60 * 60 * 1000;
    }

    // Copy methods for immutability patterns
    public Todo copy() {
        return new Todo(this.id, this.title, this.description, this.isCompleted,
                this.priority, this.dueDate, this.subTasks, this.createdAt, this.completedAt);
    }

    public Todo copyWith(String title, String description, Boolean isCompleted, 
                        Priority priority, Long dueDate, List<SubTask> subTasks, Long completedAt) {
        return new Todo(
            this.id,
            title != null ? title : this.title,
            description != null ? description : this.description,
            isCompleted != null ? isCompleted : this.isCompleted,
            priority != null ? priority : this.priority,
            dueDate != null ? dueDate : this.dueDate,
            subTasks != null ? subTasks : this.subTasks,
            this.createdAt,
            completedAt != null ? completedAt : this.completedAt
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Todo todo = (Todo) o;
        return id.equals(todo.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}