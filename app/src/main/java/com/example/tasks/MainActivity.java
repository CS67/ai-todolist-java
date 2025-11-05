package com.example.tasks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.tasks.database.TodoDatabase;
import com.example.tasks.databinding.ActivityMainBinding;
import com.example.tasks.repository.TodoRepository;
import com.example.tasks.ui.AddTodoDialogFragment;
import com.example.tasks.ui.AllTasksFragment;
import com.example.tasks.ui.CalendarFragment;
import com.example.tasks.ui.EditTodoDialogFragment;
import com.example.tasks.ui.SettingsActivity;
import com.example.tasks.viewmodel.TodoViewModel;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private TodoViewModel viewModel;
    private boolean isCalendarView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 应用保存的主题设置
        applyThemeFromPreferences();
        
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 设置Toolbar
        setSupportActionBar(binding.toolbar);

        // 初始化数据库和Repository
        TodoDatabase database = TodoDatabase.getDatabase(this);
        TodoRepository repository = new TodoRepository(database.todoDao());
        
        // 初始化ViewModel
        TodoViewModel.Factory factory = new TodoViewModel.Factory(repository);
        viewModel = new ViewModelProvider(this, factory).get(TodoViewModel.class);

        // 设置观察者
        setupObservers();

        // 设置点击事件
        setupClickListeners();

        // 加载默认视图
        if (savedInstanceState == null) {
            loadDefaultView();
        }
    }
    
    /**
     * 从设置中应用主题
     */
    private void applyThemeFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String themeMode = prefs.getString("theme_mode", "system");
        
        switch (themeMode) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    /**
     * 加载默认视图
     */
    private void loadDefaultView() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultView = prefs.getString("default_view", "all_tasks");
        
        if ("calendar".equals(defaultView)) {
            showCalendarView();
        } else {
            showAllTasksView();
        }
    }

    /**
     * 切换视图
     */
    private void toggleView() {
        if (isCalendarView) {
            showAllTasksView();
        } else {
            showCalendarView();
        }
    }

    /**
     * 显示所有任务视图
     */
    private void showAllTasksView() {
        isCalendarView = false;
        binding.btnToggleView.setIconResource(R.drawable.ic_calendar_view_24);
        
        Fragment fragment = new AllTasksFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /**
     * 显示日历视图
     */
    private void showCalendarView() {
        isCalendarView = true;
        binding.btnToggleView.setIconResource(R.drawable.ic_view_list_24);
        
        Fragment fragment = new CalendarFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void setupObservers() {
        // 观察编辑Todo
        viewModel.getEditingTodo().observe(this, todo -> {
            if (todo != null) {
                showEditTodoDialog(todo);
            }
        });
    }

    private void setupClickListeners() {
        binding.fabAddTodo.setOnClickListener(v -> showAddTodoDialog());
        
        // 切换视图按钮
        binding.btnToggleView.setOnClickListener(v -> toggleView());
        
        // 设置按钮
        binding.btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void showAddTodoDialog() {
        AddTodoDialogFragment dialog = new AddTodoDialogFragment();
        dialog.setOnTodoAddedListener((title, description, priority, dueDate, subTasks) -> {
            viewModel.addTodo(title, description, priority, dueDate, subTasks);
        });
        dialog.show(getSupportFragmentManager(), "AddTodoDialog");
    }
    
    private void showEditTodoDialog(com.example.tasks.data.Todo todo) {
        EditTodoDialogFragment dialog = new EditTodoDialogFragment();
        dialog.setTodo(todo);
        dialog.setOnTodoUpdatedListener((todoId, title, description, priority, dueDate, subTasks) -> {
            viewModel.updateTodo(todoId, title, description, priority, dueDate, subTasks);
        });
        dialog.show(getSupportFragmentManager(), "EditTodoDialog");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}