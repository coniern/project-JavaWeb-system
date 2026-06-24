package com.example.projectmanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.projectmanagement.entity.Task;

import java.util.List;
import java.util.Map;

public interface TaskService extends IService<Task> {

    List<Task> findTasks(Long projectId, String status, String keyword);

    boolean createTask(Task task);

    boolean updateTask(Task task);

    Map<String, Object> buildStats(Long projectId);
}
