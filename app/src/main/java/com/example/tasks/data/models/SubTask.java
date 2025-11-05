package com.example.tasks.data.models;

import java.util.UUID;

/**
 * 子任务数据模型
 */
public class SubTask {
    private String id;
    private String title;
    private boolean isCompleted;
    private long createdAt;

    public SubTask() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
    }

    public SubTask(String title) {
        this();
        this.title = title;
        this.isCompleted = false;
    }

    public SubTask(String id, String title, boolean isCompleted, long createdAt) {
        this.id = id;
        this.title = title;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Copy constructor for immutability patterns
    public SubTask copy() {
        return new SubTask(this.id, this.title, this.isCompleted, this.createdAt);
    }

    public SubTask copyWith(String title, Boolean isCompleted) {
        return new SubTask(
            this.id,
            title != null ? title : this.title,
            isCompleted != null ? isCompleted : this.isCompleted,
            this.createdAt
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubTask subTask = (SubTask) o;
        return id.equals(subTask.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}