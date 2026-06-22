package com.example.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.project.common.Result;
import com.example.project.entity.Task;
import com.example.project.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/api/task")
@CrossOrigin
public class TaskController {

    @Autowired
    private TaskMapper taskMapper;

    /**
     * 获取任务列表
     */
    @GetMapping("/list")
    public Result<List<Task>> list(@RequestParam(required = false) Long projectId,
                                   @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<Task>()
                .eq(Task::getDeleted, 0)
                .orderByDesc(Task::getUpdateTime)
                .orderByDesc(Task::getCreateTime);
        
        if (projectId != null) {
            wrapper.eq(Task::getProjectId, projectId);
        }
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Task::getStatus, status);
        }
        
        List<Task> tasks = taskMapper.selectList(wrapper);
        return Result.success(tasks);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public Result<Task> detail(@PathVariable Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null || Integer.valueOf(1).equals(task.getDeleted())) {
            return Result.error(404, "任务不存在");
        }
        return Result.success(task);
    }

    /**
     * 创建任务
     */
    @PostMapping("/create")
    public Result<String> create(@RequestBody Task task) {
        Result<String> validation = validateTask(task, false);
        if (validation != null) {
            return validation;
        }
        task.setDeleted(0);
        taskMapper.insert(task);
        return Result.success("创建成功");
    }

    /**
     * 更新任务
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Task task) {
        Result<String> validation = validateTask(task, true);
        if (validation != null) {
            return validation;
        }
        taskMapper.updateById(task);
        return Result.success("更新成功");
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null || Integer.valueOf(1).equals(task.getDeleted())) {
            return Result.error(404, "任务不存在");
        }
        taskMapper.deleteById(id);
        return Result.success("删除成功");
    }

    /**
     * 获取任务统计
     */
    @GetMapping("/stats/{projectId}")
    public Result<Object> stats(@PathVariable Long projectId) {
        List<Task> tasks = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getProjectId, projectId)
                .eq(Task::getDeleted, 0));
        
        long total = tasks.size();
        long todo = tasks.stream().filter(t -> "待开始".equals(t.getStatus())).count();
        long inProgress = tasks.stream().filter(t -> "进行中".equals(t.getStatus())).count();
        long done = tasks.stream().filter(t -> "已完成".equals(t.getStatus())).count();
        
        return Result.success(Map.of(
            "total", total,
            "todo", todo,
            "inProgress", inProgress,
            "done", done
        ));
    }

    private Result<String> validateTask(Task task, boolean requireId) {
        if (task == null) {
            return Result.error(400, "请求参数不能为空");
        }
        if (requireId && task.getId() == null) {
            return Result.error(400, "任务ID不能为空");
        }
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            return Result.error(400, "任务标题不能为空");
        }
        if (task.getProjectId() == null) {
            return Result.error(400, "所属项目不能为空");
        }
        if (task.getAssigneeId() == null) {
            return Result.error(400, "执行人不能为空");
        }
        if (task.getStatus() == null || task.getStatus().trim().isEmpty()) {
            return Result.error(400, "任务状态不能为空");
        }
        if (!List.of("待开始", "进行中", "已完成").contains(task.getStatus())) {
            return Result.error(400, "任务状态不合法");
        }
        if (task.getPriority() == null || !List.of(1, 2, 3).contains(task.getPriority())) {
            return Result.error(400, "任务优先级不合法");
        }
        if (requireId) {
            Task existingTask = taskMapper.selectById(task.getId());
            if (existingTask == null || Integer.valueOf(1).equals(existingTask.getDeleted())) {
                return Result.error(404, "任务不存在");
            }
            task.setDeleted(existingTask.getDeleted());
        }
        return null;
    }
}
