package com.example.tasks.ai;

import com.example.tasks.data.Priority;
import com.example.tasks.data.SubTask;
import com.example.tasks.data.Todo;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * AI任务解析器 - 使用DeepSeek API解析自然语言输入
 * 注意：这个版本简化了AI功能，主要提供基础解析
 */
public class AITaskParser {
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String MODEL = "deepseek-chat";
    
    private final String apiKey;
    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executor;
    
    public AITaskParser(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 解析自然语言输入为Todo对象（异步）
     */
    public Future<ParsedTask> parseTaskAsync(String input) {
        return executor.submit(() -> {
            try {
                String prompt = createPrompt(input);
                String response = callDeepSeekAPI(prompt);
                return parseResponse(response);
            } catch (Exception e) {
                // 如果AI解析失败，返回基础解析结果
                return createBasicParsedTask(input);
            }
        });
    }
    
    /**
     * 同步解析方法（用于测试）
     */
    public ParsedTask parseTask(String input) {
        try {
            return parseTaskAsync(input).get();
        } catch (Exception e) {
            return createBasicParsedTask(input);
        }
    }
    
    /**
     * 创建基础的解析结果（当AI不可用时）
     */
    private ParsedTask createBasicParsedTask(String input) {
        // 简单的关键词解析
        String title = input.length() > 50 ? input.substring(0, 50) + "..." : input;
        Priority priority = extractPriority(input);
        
        return new ParsedTask(
            title,
            "", // 描述为空
            priority,
            null, // 无截止日期
            "基础解析：未使用AI",
            new ArrayList<>() // 无子任务
        );
    }
    
    /**
     * 从文本中提取优先级
     */
    private Priority extractPriority(String input) {
        String lowerInput = input.toLowerCase();
        if (lowerInput.contains("紧急") || lowerInput.contains("urgent")) {
            return Priority.URGENT;
        } else if (lowerInput.contains("重要") || lowerInput.contains("高") || lowerInput.contains("high")) {
            return Priority.HIGH;
        } else if (lowerInput.contains("低") || lowerInput.contains("low")) {
            return Priority.LOW;
        }
        return Priority.MEDIUM;
    }
    
    /**
     * 创建给AI的提示词
     */
    private String createPrompt(String input) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String currentTime = dateFormat.format(new Date());
        
        return "你是一个任务解析助手，将自然语言转换为结构化任务数据。\n\n" +
                "当前时间：" + currentTime + "\n" +
                "用户输入：" + input + "\n\n" +
                "返回JSON格式：\n" +
                "{\n" +
                "  \"title\": \"任务标题\",\n" +
                "  \"description\": \"任务描述\",\n" +
                "  \"priority\": \"LOW/MEDIUM/HIGH/URGENT\",\n" +
                "  \"dueDate\": \"YYYY-MM-DD HH:mm格式或null\",\n" +
                "  \"subTasks\": [\"子任务1\", \"子任务2\"],\n" +
                "  \"reasoning\": \"分析过程\"\n" +
                "}\n\n" +
                "优先级规则：URGENT(紧急)、HIGH(重要有时限)、MEDIUM(日常)、LOW(可延后)\n" +
                "时间规则：今天=当前日期，明天=+1天，只有时间默认今天\n" +
                "子任务规则：识别\"包括A、B、C\"、\"先做X再做Y\"等表达\n\n" +
                "只返回JSON，无其他文字。";
    }
    
    /**
     * 调用DeepSeek API
     */
    private String callDeepSeekAPI(String prompt) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.1);
        requestBody.put("max_tokens", 500);
        
        String jsonBody = gson.toJson(requestBody);
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));
        
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API调用失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            Map<String, Object> apiResponse = gson.fromJson(responseBody, Map.class);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
            @SuppressWarnings("unchecked")
            Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
            
            return (String) messageObj.get("content");
        }
    }
    
    /**
     * 解析AI返回的响应
     */
    private ParsedTask parseResponse(String response) {
        try {
            int jsonStart = response.indexOf("{");
            int jsonEnd = response.lastIndexOf("}") + 1;
            
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                throw new RuntimeException("无效的JSON响应");
            }
            
            String jsonStr = response.substring(jsonStart, jsonEnd);
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = gson.fromJson(jsonStr, Map.class);
            
            return new ParsedTask(
                (String) parsed.getOrDefault("title", "新任务"),
                (String) parsed.getOrDefault("description", ""),
                parsePriority((String) parsed.get("priority")),
                parseDueDate(parsed.get("dueDate")),
                (String) parsed.getOrDefault("reasoning", ""),
                parseSubTasks(parsed.get("subTasks"))
            );
        } catch (Exception e) {
            throw new RuntimeException("解析响应失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析优先级
     */
    private Priority parsePriority(String priorityStr) {
        if (priorityStr == null) return Priority.MEDIUM;
        
        try {
            return Priority.valueOf(priorityStr.toUpperCase());
        } catch (Exception e) {
            return Priority.MEDIUM;
        }
    }
    
    /**
     * 解析子任务
     */
    @SuppressWarnings("unchecked")
    private List<SubTask> parseSubTasks(Object subTasksObj) {
        List<SubTask> result = new ArrayList<>();
        
        if (subTasksObj instanceof List) {
            List<Object> subTasksList = (List<Object>) subTasksObj;
            for (Object item : subTasksList) {
                if (item instanceof String) {
                    String taskTitle = ((String) item).trim();
                    if (!taskTitle.isEmpty()) {
                        result.add(new SubTask(taskTitle));
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * 解析截止时间
     */
    private Long parseDueDate(Object dueDateObj) {
        if (dueDateObj == null) return null;
        
        if (dueDateObj instanceof String) {
            String trimmed = ((String) dueDateObj).trim().replaceAll("^\"|\"$", "");
            if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) {
                return null;
            }
            
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date date = dateFormat.parse(trimmed);
                return date != null ? date.getTime() : null;
            } catch (Exception e) {
                return null;
            }
        } else if (dueDateObj instanceof Number) {
            long timestamp = ((Number) dueDateObj).longValue();
            // 如果时间戳小于1000000000000L，可能是秒级时间戳，转换为毫秒
            return timestamp < 1_000_000_000_000L ? timestamp * 1000L : timestamp;
        }
        
        return null;
    }
    
    /**
     * 转换为Todo对象
     */
    public Todo parsedTaskToTodo(ParsedTask parsedTask) {
        return new Todo(
            parsedTask.getTitle(),
            parsedTask.getDescription(),
            parsedTask.getPriority(),
            parsedTask.getDueDate(),
            parsedTask.getSubTasks()
        );
    }
    
    /**
     * 关闭资源
     */
    public void shutdown() {
        executor.shutdown();
    }
}