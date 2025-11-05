package com.example.tasks.ai;

import com.example.tasks.data.models.Priority;
import com.example.tasks.data.models.SubTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析结果数据类
 */
public class ParsedTask {
    private String title;
    private String description;
    private Priority priority;
    private Long dueDate;
    private String reasoning;
    private List<SubTask> subTasks;

    public ParsedTask() {
        this.subTasks = new ArrayList<>();
    }

    public ParsedTask(String title, String description, Priority priority, Long dueDate, String reasoning, List<SubTask> subTasks) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.reasoning = reasoning;
        this.subTasks = subTasks != null ? new ArrayList<>(subTasks) : new ArrayList<>();
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Priority getPriority() {
        return priority;
    }

    public Long getDueDate() {
        return dueDate;
    }

    public String getReasoning() {
        return reasoning;
    }

    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks);
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setDueDate(Long dueDate) {
        this.dueDate = dueDate;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public void setSubTasks(List<SubTask> subTasks) {
        this.subTasks = subTasks != null ? new ArrayList<>(subTasks) : new ArrayList<>();
    }
}