package com.example.tasks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tasks.adapter.OnTodoClickListener;
import com.example.tasks.adapter.TodoAdapter;
import com.example.tasks.database.TodoDatabase;
import com.example.tasks.databinding.ActivityMainBinding;
import com.example.tasks.repository.TodoRepository;
import com.example.tasks.ui.AddTodoDialogFragment;
import com.example.tasks.ui.EditTodoDialogFragment;
import com.example.tasks.ui.SettingsActivity;
import com.example.tasks.viewmodel.TodoViewModel;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private TodoViewModel viewModel;
    private TodoAdapter incompleteAdapter;
    private TodoAdapter completedAdapter;
    private boolean isCompletedExpanded = false;
    private boolean isIncompleteExpanded = true;

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

        // 设置RecyclerView
        setupRecyclerViews();

        // 设置观察者
        setupObservers();

        // 设置点击事件
        setupClickListeners();
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
                viewModel.startEditingTodo(todoId);
            }

            @Override
            public void onSubTaskToggle(String todoId, String subTaskId) {
                viewModel.toggleSubTaskCompletion(todoId, subTaskId);
            }
            
            @Override
            public void onAddSubTask(String todoId, String subTaskTitle) {
                viewModel.addSubTask(todoId, subTaskTitle);
            }
        };

        incompleteAdapter.setOnTodoClickListener(todoClickListener);
        completedAdapter.setOnTodoClickListener(todoClickListener);
    }

    private void setupObservers() {
        // 观察Todo列表并分类
        viewModel.getAllTodos().observe(this, todos -> {
            if (todos != null) {
                // 分离未完成和已完成任务
                java.util.List<com.example.tasks.data.Todo> incompleteTodos = new java.util.ArrayList<>();
                java.util.List<com.example.tasks.data.Todo> completedTodos = new java.util.ArrayList<>();
                
                for (com.example.tasks.data.Todo todo : todos) {
                    if (todo.isCompleted()) {
                        completedTodos.add(todo);
                    } else {
                        incompleteTodos.add(todo);
                    }
                }
                
                // 更新适配器
                incompleteAdapter.submitList(incompleteTodos);
                completedAdapter.submitList(completedTodos);
                
                // 更新UI和计数
                updateCategoryCounts(incompleteTodos.size(), completedTodos.size());
                updateUI(todos.size());
            }
        });

        // 观察统计数据
        viewModel.getTotalCount().observe(this, count -> 
            binding.tvTotalCount.setText(String.valueOf(count)));
            
        viewModel.getCompletedCount().observe(this, count -> 
            binding.tvCompletedCount.setText(String.valueOf(count)));
            
        viewModel.getIncompleteCount().observe(this, count -> 
            binding.tvIncompleteCount.setText(String.valueOf(count)));
        
        // 观察编辑Todo
        viewModel.getEditingTodo().observe(this, todo -> {
            if (todo != null) {
                showEditTodoDialog(todo);
            }
        });
    }

    private void setupClickListeners() {
        binding.fabAddTodo.setOnClickListener(v -> showAddTodoDialog());
        
        // 设置按钮
        binding.btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        
        // 未完成分类折叠/展开
        binding.headerIncomplete.setOnClickListener(v -> toggleIncompleteSection());
        
        // 已完成分类折叠/展开
        binding.headerCompleted.setOnClickListener(v -> toggleCompletedSection());
    }
    
    private void toggleIncompleteSection() {
        isIncompleteExpanded = !isIncompleteExpanded;
        
        if (isIncompleteExpanded) {
            binding.recyclerViewIncomplete.setVisibility(View.VISIBLE);
            binding.iconIncompleteExpand.setImageResource(R.drawable.ic_expand_less_24);
        } else {
            binding.recyclerViewIncomplete.setVisibility(View.GONE);
            binding.iconIncompleteExpand.setImageResource(R.drawable.ic_expand_more_24);
        }
    }
    
    private void toggleCompletedSection() {
        isCompletedExpanded = !isCompletedExpanded;
        
        if (isCompletedExpanded) {
            binding.recyclerViewCompleted.setVisibility(View.VISIBLE);
            binding.iconCompletedExpand.setImageResource(R.drawable.ic_expand_less_24);
        } else {
            binding.recyclerViewCompleted.setVisibility(View.GONE);
            binding.iconCompletedExpand.setImageResource(R.drawable.ic_expand_more_24);
        }
    }
    
    private void updateCategoryCounts(int incompleteCount, int completedCount) {
        binding.chipIncompleteCount.setText(String.valueOf(incompleteCount));
        binding.chipCompletedCount.setText(String.valueOf(completedCount));
        
        // 更新分类标题
        binding.tvIncompleteTitle.setText(getString(R.string.incomplete_section, incompleteCount));
        binding.tvCompletedTitle.setText(getString(R.string.completed_section, completedCount));
    }

    private void updateUI(int todoCount) {
        if (todoCount == 0) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.nestedScrollView.setVisibility(View.GONE);
            binding.statsCard.setVisibility(View.GONE);
        } else {
            binding.emptyStateLayout.setVisibility(View.GONE);
            binding.nestedScrollView.setVisibility(View.VISIBLE);
            binding.statsCard.setVisibility(View.VISIBLE);
        }
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