package com.example.projectmanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.projectmanagement.entity.Task;
import com.example.projectmanagement.mapper.TaskMapper;
import com.example.projectmanagement.service.TaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    @Override
    public List<Task> findTasks(Long projectId, String status, String keyword) {
        return lambdaQuery()
                .eq(projectId != null, Task::getProjectId, projectId)
                .eq(status != null && !status.isBlank(), Task::getStatus, status)
                .and(keyword != null && !keyword.isBlank(), wrapper ->
                        wrapper.like(Task::getTitle, keyword).or().like(Task::getDescription, keyword))
                .orderByDesc(Task::getUpdateTime)
                .orderByDesc(Task::getCreateTime)
                .list();
    }

    @Override
    public boolean createTask(Task task) {
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        if (task.getPriority() == null) {
            task.setPriority(2);
        }
        if (task.getStatus() == null || task.getStatus().isBlank()) {
            task.setStatus("todo");
        }
        return save(task);
    }

    @Override
    public boolean updateTask(Task task) {
        task.setUpdateTime(LocalDateTime.now());
        return updateById(task);
    }

    @Override
    public Map<String, Object> buildStats(Long projectId) {
        List<Task> tasks = findTasks(projectId, null, null);
        long total = tasks.size();
        long todo = tasks.stream().filter(task -> "todo".equals(task.getStatus())).count();
        long inProgress = tasks.stream().filter(task -> "in_progress".equals(task.getStatus())).count();
        long done = tasks.stream().filter(task -> "done".equals(task.getStatus())).count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", total);
        stats.put("todo", todo);
        stats.put("inProgress", inProgress);
        stats.put("done", done);
        return stats;
    }
}
