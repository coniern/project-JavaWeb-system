package com.example.projectmanagement.controller;

import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.Task;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.service.ProjectService;
import com.example.projectmanagement.service.TaskService;
import com.example.projectmanagement.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController extends BaseController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final UserService userService;

    public TaskController(TaskService taskService, ProjectService projectService, UserService userService) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<List<Map<String, Object>>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        List<Map<String, Object>> tasks = taskService.findTasks(projectId, status, keyword).stream()
                .map(this::toView)
                .toList();
        return success(tasks);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        Task task = taskService.getById(id);
        if (task == null) {
            return error(404, "任务不存在");
        }
        return success(toView(task));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Map<String, Object>> create(@RequestBody Task task) {
        String validationMessage = validateTask(task, false);
        if (validationMessage != null) {
            return error(validationMessage);
        }
        boolean created = taskService.createTask(task);
        if (!created) {
            return error("创建任务失败");
        }
        return success(toView(taskService.getById(task.getId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        String validationMessage = validateTask(task, true);
        if (validationMessage != null) {
            return error(validationMessage);
        }
        boolean updated = taskService.updateTask(task);
        if (!updated) {
            return error("更新任务失败");
        }
        return success(toView(taskService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (taskService.getById(id) == null) {
            return error(404, "任务不存在");
        }
        boolean deleted = taskService.removeById(id);
        if (!deleted) {
            return error("删除任务失败");
        }
        return success();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Map<String, Object>> stats(@RequestParam(required = false) Long projectId) {
        return success(taskService.buildStats(projectId));
    }

    private String validateTask(Task task, boolean requireExisting) {
        if (task == null) {
            return "任务参数不能为空";
        }
        if (requireExisting) {
            if (task.getId() == null || taskService.getById(task.getId()) == null) {
                return "任务不存在";
            }
        }
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            return "任务标题不能为空";
        }
        if (task.getProjectId() == null || projectService.getById(task.getProjectId()) == null) {
            return "所属项目不能为空";
        }
        if (task.getAssigneeId() == null || userService.getById(task.getAssigneeId()) == null) {
            return "执行人不能为空";
        }
        if (!List.of("todo", "in_progress", "done").contains(task.getStatus())) {
            return "任务状态不合法";
        }
        if (task.getPriority() == null || !List.of(1, 2, 3).contains(task.getPriority())) {
            return "任务优先级不合法";
        }
        return null;
    }

    private Map<String, Object> toView(Task task) {
        Project project = projectService.getById(task.getProjectId());
        User assignee = userService.getById(task.getAssigneeId());

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", task.getId());
        view.put("projectId", task.getProjectId());
        view.put("projectName", project == null ? "未关联项目" : project.getName());
        view.put("title", task.getTitle());
        view.put("description", task.getDescription() == null ? "" : task.getDescription());
        view.put("statusCode", task.getStatus());
        view.put("status", mapStatus(task.getStatus()));
        view.put("priority", task.getPriority());
        view.put("priorityLabel", mapPriority(task.getPriority()));
        view.put("assigneeId", task.getAssigneeId());
        view.put("assigneeName", assignee == null ? "未分配" : displayName(assignee));
        view.put("dueDate", task.getDueDate() == null ? "" : task.getDueDate());
        view.put("updatedAt", task.getUpdateTime() == null ? "" : task.getUpdateTime().toLocalDate().toString());
        return view;
    }

    private String mapStatus(String status) {
        return switch (status) {
            case "todo" -> "待开始";
            case "in_progress" -> "进行中";
            case "done" -> "已完成";
            default -> status;
        };
    }

    private String mapPriority(Integer priority) {
        return switch (priority) {
            case 1 -> "高";
            case 2 -> "中";
            case 3 -> "低";
            default -> "中";
        };
    }

    private String displayName(User user) {
        return user.getNickname() == null || user.getNickname().isBlank() ? user.getUsername() : user.getNickname();
    }
}
