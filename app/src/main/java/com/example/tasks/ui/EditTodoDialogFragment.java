package com.example.tasks.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.example.tasks.R;
import com.example.tasks.ai.AITaskParser;
import com.example.tasks.ai.ParsedTask;
import com.example.tasks.data.Priority;
import com.example.tasks.data.SubTask;
import com.example.tasks.data.Todo;
import com.example.tasks.databinding.DialogEditTodoBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;

/**
 * 编辑Todo对话框
 */
public class EditTodoDialogFragment extends DialogFragment {
    
    private static final int AI_SPEECH_REQUEST_CODE = 1001;
    
    private DialogEditTodoBinding binding;
    private OnTodoUpdatedListener listener;
    private Todo todo;
    private Long selectedDueDate = null;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault());
    
    // 日期时间组件
    private Calendar selectedCalendar = Calendar.getInstance();
    
    // AI相关
    private AITaskParser aiParser;
    private ParsedTask currentParsedTask;
    private Future<ParsedTask> aiParseTask;
    private boolean isAiModeEnabled = false;
    
    public interface OnTodoUpdatedListener {
        void onTodoUpdated(String todoId, String title, String description, Priority priority, Long dueDate, List<SubTask> subTasks);
    }
    
    public void setOnTodoUpdatedListener(OnTodoUpdatedListener listener) {
        this.listener = listener;
    }
    
    public void setTodo(Todo todo) {
        this.todo = todo;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            // 设置全屏显示
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            // 移除默认的背景和边距
            dialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);
        }
        return dialog;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        // 确保对话框全屏显示
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogEditTodoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeAI();
        
        if (todo != null) {
            populateTodoData();
        }
        
        setupClickListeners();
    }
    
    private void initializeAI() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String apiKey = prefs.getString("deepseek_api_key", "");
        
        if (!apiKey.isEmpty()) {
            aiParser = new AITaskParser(apiKey);
        }
    }
    
    private void populateTodoData() {
        // 设置标题和描述
        binding.etTitle.setText(todo.getTitle());
        binding.etDescription.setText(todo.getDescription());
        
        // 设置优先级
        setPriorityChip(todo.getPriority());
        
        // 设置截止日期
        if (todo.getDueDate() != null) {
            selectedDueDate = todo.getDueDate();
            selectedCalendar.setTimeInMillis(selectedDueDate);
            updateDateTimeDisplay();
        }
        
        // 设置子任务
        for (SubTask subTask : todo.getSubTasks()) {
            addSubTaskView(subTask.getTitle(), subTask.isCompleted());
        }
    }
    
    private void setPriorityChip(Priority priority) {
        // 清除所有选择
        binding.chipGroupPriority.clearCheck();
        
        // 根据优先级选择对应的chip
        switch (priority) {
            case LOW:
                binding.chipLow.setChecked(true);
                break;
            case MEDIUM:
                binding.chipMedium.setChecked(true);
                break;
            case HIGH:
                binding.chipHigh.setChecked(true);
                break;
            case URGENT:
                binding.chipUrgent.setChecked(true);
                break;
        }
    }
    
    private void setupClickListeners() {
        // 取消按钮
        binding.btnCancel.setOnClickListener(v -> dismiss());
        
        // 保存按钮
        binding.btnSave.setOnClickListener(v -> saveTodo());
        
        // 选择日期时间按钮（合并）
        binding.btnSelectDatetime.setOnClickListener(v -> showDateTimePicker());
        
        // AI模式切换按钮
        binding.btnToggleAiMode.setOnClickListener(v -> toggleAiMode());
        
        // AI输入语音按钮
        binding.layoutAiInput.setEndIconOnClickListener(view -> startAiVoiceInput());
        
        // AI分析按钮
        binding.btnAiAnalyze.setOnClickListener(v -> performAIAnalysisFromAiInput());
        
        // 添加子任务按钮
        binding.btnAddSubtask.setOnClickListener(v -> addSubTaskInput());
    }
    
    /**
     * 显示日期时间选择器（先选日期，再选时间）
     */
    private void showDateTimePicker() {
        showDatePicker();
    }
    
    private void saveTodo() {
        String title = binding.etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            binding.etTitle.setError("请输入任务标题");
            return;
        }
        
        String description = binding.etDescription.getText().toString().trim();
        Priority priority = getSelectedPriority();
        List<SubTask> subTasks = collectSubTasks();
        
        if (listener != null && todo != null) {
            listener.onTodoUpdated(todo.getId(), title, description, priority, selectedDueDate, subTasks);
        }
        
        dismiss();
    }
    
    private Priority getSelectedPriority() {
        int checkedId = binding.chipGroupPriority.getCheckedChipId();
        if (checkedId == R.id.chip_low) {
            return Priority.LOW;
        } else if (checkedId == R.id.chip_medium) {
            return Priority.MEDIUM;
        } else if (checkedId == R.id.chip_high) {
            return Priority.HIGH;
        } else if (checkedId == R.id.chip_urgent) {
            return Priority.URGENT;
        }
        return Priority.MEDIUM; // 默认中等优先级
    }
    
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                selectedCalendar.set(Calendar.YEAR, year);
                selectedCalendar.set(Calendar.MONTH, month);
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                // 选完日期后自动弹出时间选择器
                showTimePicker();
            },
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // 设置最小日期为今天
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }
    
    private void showTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(
            requireContext(),
            (view, hourOfDay, minute) -> {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedCalendar.set(Calendar.MINUTE, minute);
                selectedCalendar.set(Calendar.SECOND, 0);
                selectedDueDate = selectedCalendar.getTimeInMillis();
                updateDateTimeDisplay();
            },
            selectedCalendar.get(Calendar.HOUR_OF_DAY),
            selectedCalendar.get(Calendar.MINUTE),
            true
        );
        dialog.show();
    }
    
    /**
     * 更新日期时间显示
     */
    private void updateDateTimeDisplay() {
        if (selectedDueDate != null) {
            String dateTimeStr = dateTimeFormat.format(new Date(selectedDueDate));
            binding.tvSelectedDate.setText("📅 " + dateTimeStr);
            binding.tvSelectedDate.setVisibility(View.VISIBLE);
        } else {
            binding.tvSelectedDate.setVisibility(View.GONE);
        }
    }
    
    /**
     * 添加子任务输入框
     */
    private void addSubTaskInput() {
        addSubTaskView("", false);
    }
    
    /**
     * 添加子任务视图
     */
    private void addSubTaskView(String title, boolean isCompleted) {
        View subTaskView = getLayoutInflater().inflate(R.layout.item_subtask_input, binding.layoutSubtasksContainer, false);
        
        EditText etSubTaskTitle = subTaskView.findViewById(R.id.et_subtask_title);
        ImageButton btnRemove = subTaskView.findViewById(R.id.btn_remove_subtask);
        
        // 设置标题
        if (!title.isEmpty()) {
            etSubTaskTitle.setText(title);
        }
        
        // 删除按钮点击事件
        btnRemove.setOnClickListener(v -> binding.layoutSubtasksContainer.removeView(subTaskView));
        
        binding.layoutSubtasksContainer.addView(subTaskView);
    }
    
    /**
     * 收集所有子任务
     */
    private List<SubTask> collectSubTasks() {
        List<SubTask> subTasks = new ArrayList<>();
        
        for (int i = 0; i < binding.layoutSubtasksContainer.getChildCount(); i++) {
            View childView = binding.layoutSubtasksContainer.getChildAt(i);
            EditText etSubTaskTitle = childView.findViewById(R.id.et_subtask_title);
            
            String title = etSubTaskTitle.getText().toString().trim();
            if (!title.isEmpty()) {
                subTasks.add(new SubTask(title));
            }
        }
        
        return subTasks;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // 取消正在进行的AI解析任务
        if (aiParseTask != null && !aiParseTask.isDone()) {
            aiParseTask.cancel(true);
        }
        
        // 关闭AI解析器
        if (aiParser != null) {
            aiParser.shutdown();
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == AI_SPEECH_REQUEST_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0);
                binding.etAiInput.setText(spokenText);
            }
        }
    }
    
    /**
     * 切换AI模式
     */
    private void toggleAiMode() {
        isAiModeEnabled = !isAiModeEnabled;
        
        if (isAiModeEnabled) {
            binding.cardAiMode.setVisibility(View.VISIBLE);
            binding.btnToggleAiMode.setIconResource(android.R.drawable.ic_menu_close_clear_cancel);
            
            if (aiParser == null) {
                binding.btnAiAnalyze.setEnabled(false);
                binding.btnAiAnalyze.setText("⚠️ 请先配置API密钥");
            }
        } else {
            binding.cardAiMode.setVisibility(View.GONE);
            binding.btnToggleAiMode.setIconResource(android.R.drawable.ic_menu_help);
        }
    }
    
    /**
     * AI模式下的语音输入
     */
    private void startAiVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请描述你的任务");
        
        try {
            startActivityForResult(intent, AI_SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(getContext(), "语音识别不可用", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 从AI输入区域执行分析
     */
    private void performAIAnalysisFromAiInput() {
        if (aiParser == null) {
            Toast.makeText(getContext(), "请先在设置中配置DeepSeek API密钥", Toast.LENGTH_LONG).show();
            return;
        }
        
        String input = binding.etAiInput.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(getContext(), "请先输入任务描述", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示加载状态
        binding.btnAiAnalyze.setText("🤖 AI分析中...");
        binding.btnAiAnalyze.setEnabled(false);
        
        // 异步执行AI解析
        aiParseTask = aiParser.parseTaskAsync(input);
        
        // 在后台线程等待结果
        new Thread(() -> {
            try {
                ParsedTask result = aiParseTask.get();
                // 切换到主线程更新UI
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        currentParsedTask = result;
                        applyAIResultDirectly(result);
                        binding.btnAiAnalyze.setText("✨ 让AI帮我分析");
                        binding.btnAiAnalyze.setEnabled(true);
                        
                        // 关闭AI模式显示
                        binding.cardAiMode.setVisibility(View.GONE);
                        isAiModeEnabled = false;
                        binding.btnToggleAiMode.setIconResource(android.R.drawable.ic_menu_help);
                        
                        Toast.makeText(getContext(), "✅ AI分析完成，已自动填充表单", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "AI解析失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.btnAiAnalyze.setText("✨ 让AI帮我分析");
                        binding.btnAiAnalyze.setEnabled(true);
                    });
                }
            }
        }).start();
    }
    
    /**
     * 直接应用AI结果
     */
    private void applyAIResultDirectly(ParsedTask parsedTask) {
        if (parsedTask == null) return;
        
        // 应用标题
        if (!parsedTask.getTitle().isEmpty()) {
            binding.etTitle.setText(parsedTask.getTitle());
        }
        
        // 应用描述
        if (!parsedTask.getDescription().isEmpty()) {
            binding.etDescription.setText(parsedTask.getDescription());
        }
        
        // 应用优先级
        setPriorityChip(parsedTask.getPriority());
        
        // 应用截止日期
        if (parsedTask.getDueDate() != null) {
            selectedDueDate = parsedTask.getDueDate();
            selectedCalendar.setTimeInMillis(selectedDueDate);
            updateDateTimeDisplay();
        }
        
        // 应用子任务
        List<SubTask> subTasks = parsedTask.getSubTasks();
        if (subTasks != null && !subTasks.isEmpty()) {
            // 清除现有子任务
            binding.layoutSubtasksContainer.removeAllViews();
            
            // 添加AI解析的子任务
            for (SubTask subTask : subTasks) {
                addSubTaskView(subTask.getTitle(), subTask.isCompleted());
            }
        }
    }
}
