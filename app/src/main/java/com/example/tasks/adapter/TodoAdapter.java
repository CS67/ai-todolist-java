package com.example.tasks.adapter;

import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasks.R;
import com.example.tasks.data.Priority;
import com.example.tasks.data.SubTask;
import com.example.tasks.data.Todo;
import com.example.tasks.databinding.ItemTodoBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Todo RecyclerView适配器
 */
public class TodoAdapter extends ListAdapter<Todo, TodoAdapter.TodoViewHolder> {
    
    private OnTodoClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault());
    
    // 保存展开状态的Map，key为todoId
    private final java.util.HashMap<String, Boolean> expandedStates = new java.util.HashMap<>();
    
    public TodoAdapter() {
        super(new TodoDiffCallback());
    }
    
    public void setOnTodoClickListener(OnTodoClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTodoBinding binding = ItemTodoBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new TodoViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        holder.bind(getItem(position));
    }
    
    public class TodoViewHolder extends RecyclerView.ViewHolder {
        private final ItemTodoBinding binding;
        
        public TodoViewHolder(ItemTodoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        public void bind(Todo todo) {
            // 获取该todo的展开状态
            boolean isSubTasksExpanded = expandedStates.getOrDefault(todo.getId(), false);
            
            // 设置优先级条纹颜色
            int priorityColor = binding.getRoot().getContext().getColor(todo.getPriority().getColorRes());
            binding.priorityStripe.setBackgroundColor(priorityColor);
            
            // 设置基本信息
            binding.tvTitle.setText(todo.getTitle());
            binding.checkboxCompleted.setChecked(todo.isCompleted());
            
            // 设置完成状态的视觉效果
            if (todo.isCompleted()) {
                binding.tvTitle.setPaintFlags(binding.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvTitle.setAlpha(0.6f);
            } else {
                binding.tvTitle.setPaintFlags(binding.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                binding.tvTitle.setAlpha(1.0f);
            }
            
            // 设置描述
            if (todo.getDescription().isEmpty()) {
                binding.tvDescription.setVisibility(View.GONE);
            } else {
                binding.tvDescription.setVisibility(View.VISIBLE);
                binding.tvDescription.setText(todo.getDescription());
            }
            
            // 设置优先级Chip
            binding.tvPriority.setText(todo.getPriority().getDisplayName());
            binding.tvPriority.setChipBackgroundColorResource(todo.getPriority().getColorRes());
            
            // 设置截止时间
            if (todo.getDueDate() != null) {
                binding.layoutDueDate.setVisibility(View.VISIBLE);
                String dueDateStr = dateTimeFormat.format(new Date(todo.getDueDate()));
                binding.tvDueDate.setText(dueDateStr);
                
                // 设置过期颜色
                if (todo.isOverdue()) {
                    int urgentColor = binding.getRoot().getContext().getColor(Priority.URGENT.getColorRes());
                    binding.tvDueDate.setTextColor(urgentColor);
                } else if (todo.isDueSoon()) {
                    int highColor = binding.getRoot().getContext().getColor(Priority.HIGH.getColorRes());
                    binding.tvDueDate.setTextColor(highColor);
                }
            } else {
                binding.layoutDueDate.setVisibility(View.GONE);
            }
            
            // 设置截止时间相对时间（如今天、明天、13天后等）
            if (todo.getDueDate() != null) {
                binding.tvCreatedAt.setVisibility(View.VISIBLE);
                binding.tvCreatedAt.setText(getDueDateRelativeString(todo.getDueDate()));
            } else {
                binding.tvCreatedAt.setVisibility(View.GONE);
            }
            
            // 设置子任务进度
            if (todo.getSubTasks().isEmpty()) {
                binding.layoutSubtasks.setVisibility(View.GONE);
            } else {
                binding.layoutSubtasks.setVisibility(View.VISIBLE);
                // 总是更新子任务进度
                updateSubTaskProgress(todo);
                
                // 设置展开/折叠按钮图标
                binding.btnToggleSubtasks.setIconResource(
                    isSubTasksExpanded ? R.drawable.ic_expand_less_24 : R.drawable.ic_expand_more_24
                );
                
                // 显示/隐藏子任务列表
                binding.layoutSubtaskList.setVisibility(isSubTasksExpanded ? View.VISIBLE : View.GONE);
                
                // 如果展开了，重新渲染子任务列表以反映最新状态
                if (isSubTasksExpanded) {
                    populateSubTasks(todo);
                }
            }
            
            // 设置点击事件
            binding.checkboxCompleted.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTodoToggle(todo.getId());
                }
            });
            
            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTodoEdit(todo.getId());
                }
            });
            
            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTodoDelete(todo.getId());
                }
            });
            
            // 展开/折叠子任务按钮
            binding.btnToggleSubtasks.setOnClickListener(v -> {
                boolean newState = !isSubTasksExpanded;
                expandedStates.put(todo.getId(), newState);
                
                binding.btnToggleSubtasks.setIconResource(
                    newState ? R.drawable.ic_expand_less_24 : R.drawable.ic_expand_more_24
                );
                binding.layoutSubtaskList.setVisibility(newState ? View.VISIBLE : View.GONE);
                
                if (newState) {
                    populateSubTasks(todo);
                }
            });
        }
        
        /**
         * 获取相对时间字符串（用于创建时间）
         */
        private String getRelativeTimeString(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;
            
            if (diff < 24 * 60 * 60 * 1000) { // 24小时内
                return "今天";
            } else if (diff < 2 * 24 * 60 * 60 * 1000) { // 48小时内
                return "昨天";
            } else if (diff < 7 * 24 * 60 * 60 * 1000) { // 一周内
                return String.format("%d天前", diff / (24 * 60 * 60 * 1000));
            } else {
                return new SimpleDateFormat("MM月dd日", Locale.getDefault()).format(new Date(timestamp));
            }
        }
        
        /**
         * 获取截止时间的相对时间字符串
         */
        private String getDueDateRelativeString(long dueDate) {
            long now = System.currentTimeMillis();
            long diff = dueDate - now;
            
            // 计算天数差异（考虑日期边界）
            Calendar nowCal = Calendar.getInstance();
            Calendar dueCal = Calendar.getInstance();
            dueCal.setTimeInMillis(dueDate);
            
            // 重置时间到当天0点进行日期比较
            nowCal.set(Calendar.HOUR_OF_DAY, 0);
            nowCal.set(Calendar.MINUTE, 0);
            nowCal.set(Calendar.SECOND, 0);
            nowCal.set(Calendar.MILLISECOND, 0);
            
            Calendar dueCalDay = Calendar.getInstance();
            dueCalDay.setTimeInMillis(dueDate);
            dueCalDay.set(Calendar.HOUR_OF_DAY, 0);
            dueCalDay.set(Calendar.MINUTE, 0);
            dueCalDay.set(Calendar.SECOND, 0);
            dueCalDay.set(Calendar.MILLISECOND, 0);
            
            long daysDiff = (dueCalDay.getTimeInMillis() - nowCal.getTimeInMillis()) / (24 * 60 * 60 * 1000);
            
            if (daysDiff == 0) {
                return "今天";
            } else if (daysDiff == 1) {
                return "明天";
            } else if (daysDiff == -1) {
                return "昨天";
            } else if (daysDiff > 1 && daysDiff <= 30) {
                return String.format("%d天后", daysDiff);
            } else if (daysDiff < -1 && daysDiff >= -30) {
                return String.format("%d天前", -daysDiff);
            } else if (daysDiff > 30) {
                return new SimpleDateFormat("MM月dd日", Locale.getDefault()).format(new Date(dueDate));
            } else {
                return new SimpleDateFormat("MM月dd日", Locale.getDefault()).format(new Date(dueDate));
            }
        }
        
        /**
         * 更新子任务进度显示
         */
        private void updateSubTaskProgress(Todo todo) {
            int completed = todo.getCompletedSubTasksCount();
            int total = todo.getSubTasks().size();
            binding.tvSubtaskProgress.setText("子任务: " + completed + "/" + total + " 已完成");
            binding.progressSubtasks.setProgress((int) (todo.getSubTaskProgress() * 100));
        }
        
        /**
         * 填充子任务列表
         */
        private void populateSubTasks(Todo todo) {
            binding.layoutSubtaskList.removeAllViews();
            
            for (int i = 0; i < todo.getSubTasks().size(); i++) {
                SubTask subTask = todo.getSubTasks().get(i);
                View subTaskView = LayoutInflater.from(binding.getRoot().getContext())
                    .inflate(R.layout.item_subtask, binding.layoutSubtaskList, false);
                
                CheckBox checkBox = subTaskView.findViewById(R.id.checkbox_subtask);
                TextView title = subTaskView.findViewById(R.id.tv_subtask_title);
                ImageButton btnAddSubTask = subTaskView.findViewById(R.id.btn_add_subtask_here);
                
                checkBox.setChecked(subTask.isCompleted());
                title.setText(subTask.getTitle());
                
                // 设置完成状态的视觉效果
                if (subTask.isCompleted()) {
                    title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    title.setAlpha(0.6f);
                } else {
                    title.setPaintFlags(title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    title.setAlpha(1.0f);
                }
                
                // 子任务完成状态切换
                checkBox.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onSubTaskToggle(todo.getId(), subTask.getId());
                    }
                });
                
                // 显示最后一个子任务的添加按钮
                if (i == todo.getSubTasks().size() - 1) {
                    btnAddSubTask.setVisibility(View.VISIBLE);
                    btnAddSubTask.setOnClickListener(v -> {
                        showAddSubTaskDialog(todo.getId());
                    });
                }
                
                binding.layoutSubtaskList.addView(subTaskView);
            }
            
            // 如果没有子任务，显示添加按钮
            if (todo.getSubTasks().isEmpty()) {
                View addSubTaskView = LayoutInflater.from(binding.getRoot().getContext())
                    .inflate(R.layout.item_subtask, binding.layoutSubtaskList, false);
                
                CheckBox checkBox = addSubTaskView.findViewById(R.id.checkbox_subtask);
                TextView title = addSubTaskView.findViewById(R.id.tv_subtask_title);
                ImageButton btnAddSubTask = addSubTaskView.findViewById(R.id.btn_add_subtask_here);
                
                checkBox.setVisibility(View.GONE);
                title.setText("点击添加第一个子任务");
                title.setTextColor(title.getContext().getColor(android.R.color.darker_gray));
                btnAddSubTask.setVisibility(View.VISIBLE);
                
                btnAddSubTask.setOnClickListener(v -> {
                    showAddSubTaskDialog(todo.getId());
                });
                
                binding.layoutSubtaskList.addView(addSubTaskView);
            }
        }
        
        /**
         * 显示添加子任务对话框
         */
        private void showAddSubTaskDialog(String todoId) {
            // 简单的输入对话框
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(binding.getRoot().getContext());
            final EditText input = new EditText(binding.getRoot().getContext());
            input.setHint("输入子任务标题");
            builder.setTitle("添加子任务")
                   .setView(input)
                   .setPositiveButton("添加", (dialog, which) -> {
                       String title = input.getText().toString().trim();
                       if (!title.isEmpty() && listener != null) {
                           listener.onAddSubTask(todoId, title);
                       }
                   })
                   .setNegativeButton("取消", null)
                   .show();
        }
    }
    
    private static class TodoDiffCallback extends DiffUtil.ItemCallback<Todo> {
        @Override
        public boolean areItemsTheSame(@NonNull Todo oldItem, @NonNull Todo newItem) {
            return oldItem.getId().equals(newItem.getId());
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull Todo oldItem, @NonNull Todo newItem) {
            // 比较所有关键字段
            if (!oldItem.getTitle().equals(newItem.getTitle())) return false;
            if (!oldItem.getDescription().equals(newItem.getDescription())) return false;
            if (oldItem.isCompleted() != newItem.isCompleted()) return false;
            if (oldItem.getPriority() != newItem.getPriority()) return false;
            
            // 比较截止日期
            if (oldItem.getDueDate() == null && newItem.getDueDate() != null) return false;
            if (oldItem.getDueDate() != null && !oldItem.getDueDate().equals(newItem.getDueDate())) return false;
            
            // 比较子任务数量和状态
            if (oldItem.getSubTasks().size() != newItem.getSubTasks().size()) return false;
            
            // 比较每个子任务
            for (int i = 0; i < oldItem.getSubTasks().size(); i++) {
                SubTask oldSubTask = oldItem.getSubTasks().get(i);
                SubTask newSubTask = newItem.getSubTasks().get(i);
                
                if (!oldSubTask.getId().equals(newSubTask.getId())) return false;
                if (!oldSubTask.getTitle().equals(newSubTask.getTitle())) return false;
                if (oldSubTask.isCompleted() != newSubTask.isCompleted()) return false;
            }
            
            return true;
        }
    }
}