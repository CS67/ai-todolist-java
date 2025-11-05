package com.example.tasks.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tasks.ui.adapters.OnTodoClickListener;
import com.example.tasks.ui.adapters.TodoAdapter;
import com.example.tasks.data.models.Todo;
import com.example.tasks.databinding.FragmentCalendarBinding;
import com.example.tasks.ui.viewmodel.TodoViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {
    
    private FragmentCalendarBinding binding;
    private TodoViewModel viewModel;
    private TodoAdapter incompleteAdapter;
    private TodoAdapter completedAdapter;
    private long selectedDateMillis;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 从Activity获取ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);
        
        setupRecyclerViews();
        setupCalendar();
        
        // 默认显示今天的任务
        selectedDateMillis = System.currentTimeMillis();
        updateSelectedDateDisplay();
        loadTasksForDate(selectedDateMillis);
    }
    
    private void setupRecyclerViews() {
        // 设置未完成任务RecyclerView
        incompleteAdapter = new TodoAdapter();
        binding.recyclerViewIncomplete.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewIncomplete.setAdapter(incompleteAdapter);
        
        // 设置已完成任务RecyclerView
        completedAdapter = new TodoAdapter();
        binding.recyclerViewCompleted.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewCompleted.setAdapter(completedAdapter);
        
        // 设置适配器回调
        OnTodoClickListener todoClickListener = new OnTodoClickListener() {
            @Override
            public void onTodoToggle(String todoId) {
                viewModel.toggleTodoCompletion(todoId);
            }
            
            @Override
            public void onTodoDelete(String todoId) {
                viewModel.deleteTodo(todoId);
            }
            
            @Override
            public void onTodoEdit(String todoId) {
                viewModel.startEditingTodo(todoId);
            }
            
            @Override
            public void onSubTaskToggle(String todoId, String subTaskId) {
                viewModel.toggleSubTaskCompletion(todoId, subTaskId);
            }
            
            @Override
            public void onAddSubTask(String todoId, String title) {
                viewModel.addSubTask(todoId, title);
            }
        };
        
        incompleteAdapter.setOnTodoClickListener(todoClickListener);
        completedAdapter.setOnTodoClickListener(todoClickListener);
    }
    
    private void setupCalendar() {
        binding.calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth, 0, 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                selectedDateMillis = calendar.getTimeInMillis();
                
                updateSelectedDateDisplay();
                loadTasksForDate(selectedDateMillis);
            }
        });
    }
    
    private void updateSelectedDateDisplay() {
        String dateStr = dateFormat.format(selectedDateMillis);
        binding.tvSelectedDate.setText(dateStr);
    }
    
    /**
     * 加载指定日期的任务
     */
    private void loadTasksForDate(long dateMillis) {
        viewModel.getAllTodos().observe(getViewLifecycleOwner(), todos -> {
            if (todos != null) {
                // 获取选中日期的开始和结束时间戳
                Calendar startOfDay = Calendar.getInstance();
                startOfDay.setTimeInMillis(dateMillis);
                startOfDay.set(Calendar.HOUR_OF_DAY, 0);
                startOfDay.set(Calendar.MINUTE, 0);
                startOfDay.set(Calendar.SECOND, 0);
                startOfDay.set(Calendar.MILLISECOND, 0);
                
                Calendar endOfDay = Calendar.getInstance();
                endOfDay.setTimeInMillis(dateMillis);
                endOfDay.set(Calendar.HOUR_OF_DAY, 23);
                endOfDay.set(Calendar.MINUTE, 59);
                endOfDay.set(Calendar.SECOND, 59);
                endOfDay.set(Calendar.MILLISECOND, 999);
                
                long startTime = startOfDay.getTimeInMillis();
                long endTime = endOfDay.getTimeInMillis();
                
                // 筛选出该日期的任务（截止日期在这一天的）
                List<Todo> incompleteTodos = new ArrayList<>();
                List<Todo> completedTodos = new ArrayList<>();
                
                for (Todo todo : todos) {
                    if (todo.getDueDate() != null && 
                        todo.getDueDate() >= startTime && 
                        todo.getDueDate() <= endTime) {
                        if (todo.isCompleted()) {
                            completedTodos.add(todo);
                        } else {
                            incompleteTodos.add(todo);
                        }
                    }
                }
                
                // 更新UI
                updateTaskLists(incompleteTodos, completedTodos);
            }
        });
    }
    
    /**
     * 更新任务列表UI
     */
    private void updateTaskLists(List<Todo> incompleteTodos, List<Todo> completedTodos) {
        // 更新未完成任务
        incompleteAdapter.submitList(incompleteTodos);
        binding.chipIncompleteCount.setText(String.valueOf(incompleteTodos.size()));
        
        if (incompleteTodos.isEmpty()) {
            binding.recyclerViewIncomplete.setVisibility(View.GONE);
            binding.tvEmptyIncomplete.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewIncomplete.setVisibility(View.VISIBLE);
            binding.tvEmptyIncomplete.setVisibility(View.GONE);
        }
        
        // 更新已完成任务
        completedAdapter.submitList(completedTodos);
        binding.chipCompletedCount.setText(String.valueOf(completedTodos.size()));
        
        if (completedTodos.isEmpty()) {
            binding.recyclerViewCompleted.setVisibility(View.GONE);
            binding.tvEmptyCompleted.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewCompleted.setVisibility(View.VISIBLE);
            binding.tvEmptyCompleted.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
