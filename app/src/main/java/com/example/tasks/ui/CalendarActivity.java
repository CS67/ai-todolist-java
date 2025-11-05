package com.example.tasks.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tasks.adapter.OnTodoClickListener;
import com.example.tasks.adapter.TodoAdapter;
import com.example.tasks.data.Todo;
import com.example.tasks.database.TodoDatabase;
import com.example.tasks.databinding.ActivityCalendarBinding;
import com.example.tasks.repository.TodoRepository;
import com.example.tasks.viewmodel.TodoViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 日历视图Activity
 */
public class CalendarActivity extends AppCompatActivity {
    
    private ActivityCalendarBinding binding;
    private TodoViewModel viewModel;
    private TodoAdapter incompleteAdapter;
    private TodoAdapter completedAdapter;
    private long selectedDateMillis;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalendarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupToolbar();
        setupViewModel();
        setupRecyclerViews();
        setupCalendar();
        
        // 默认显示今天的任务
        selectedDateMillis = System.currentTimeMillis();
        updateSelectedDateDisplay();
        loadTasksForDate(selectedDateMillis);
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupViewModel() {
        TodoDatabase database = TodoDatabase.getDatabase(this);
        TodoRepository repository = new TodoRepository(database.todoDao());
        TodoViewModel.Factory factory = new TodoViewModel.Factory(repository);
        viewModel = new ViewModelProvider(this, factory).get(TodoViewModel.class);
    }
    
    private void setupRecyclerViews() {
        // 设置未完成任务RecyclerView
        incompleteAdapter = new TodoAdapter();
        binding.recyclerViewIncomplete.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewIncomplete.setAdapter(incompleteAdapter);
        
        // 设置已完成任务RecyclerView
        completedAdapter = new TodoAdapter();
        binding.recyclerViewCompleted.setLayoutManager(new LinearLayoutManager(this));
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
                // 日历视图不支持编辑，可以跳转回主页面或显示提示
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
        viewModel.getAllTodos().observe(this, todos -> {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
