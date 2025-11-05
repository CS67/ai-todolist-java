package com.example.tasks.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tasks.R;
import com.example.tasks.data.models.Todo;
import com.example.tasks.ui.adapters.OnTodoClickListener;
import com.example.tasks.ui.adapters.TodoAdapter;
import com.example.tasks.databinding.FragmentAllTasksBinding;
import com.example.tasks.ui.viewmodel.TodoViewModel;

public class AllTasksFragment extends Fragment {
    private FragmentAllTasksBinding binding;
    private TodoViewModel viewModel;
    private TodoAdapter incompleteAdapter;
    private TodoAdapter completedAdapter;
    private boolean isCompletedExpanded = false;
    private boolean isIncompleteExpanded = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAllTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 从Activity获取ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);

        setupRecyclerViews();
        setupObservers();
        setupClickListeners();
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
            public void onAddSubTask(String todoId, String subTaskTitle) {
                viewModel.addSubTask(todoId, subTaskTitle);
            }
        };

        incompleteAdapter.setOnTodoClickListener(todoClickListener);
        completedAdapter.setOnTodoClickListener(todoClickListener);
    }

    private void setupObservers() {
        // 观察Todo列表并分类
        viewModel.getAllTodos().observe(getViewLifecycleOwner(), todos -> {
            if (todos != null) {
                // 分离未完成和已完成任务
                java.util.List<Todo> incompleteTodos = new java.util.ArrayList<>();
                java.util.List<Todo> completedTodos = new java.util.ArrayList<>();

                for (Todo todo : todos) {
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
        viewModel.getTotalCount().observe(getViewLifecycleOwner(), count ->
                binding.tvTotalCount.setText(String.valueOf(count)));

        viewModel.getCompletedCount().observe(getViewLifecycleOwner(), count ->
                binding.tvCompletedCount.setText(String.valueOf(count)));

        viewModel.getIncompleteCount().observe(getViewLifecycleOwner(), count ->
                binding.tvIncompleteCount.setText(String.valueOf(count)));
    }

    private void setupClickListeners() {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
