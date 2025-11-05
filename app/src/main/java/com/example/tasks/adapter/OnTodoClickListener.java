package com.example.tasks.adapter;

public interface OnTodoClickListener {
    void onTodoToggle(String todoId);
    void onTodoDelete(String todoId);
    void onTodoEdit(String todoId);
    void onSubTaskToggle(String todoId, String subTaskId);
    void onAddSubTask(String todoId, String subTaskTitle);
}