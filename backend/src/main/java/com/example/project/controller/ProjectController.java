package com.example.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.project.common.Result;
import com.example.project.entity.Project;
import com.example.project.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 项目控制器
 */
@RestController
@RequestMapping("/api/project")
@CrossOrigin
public class ProjectController {

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * 获取项目列表
     */
    @GetMapping("/list")
    public Result<List<Project>> list(@RequestParam(required = false) String status) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<Project>()
                .eq(Project::getDeleted, 0)
                .orderByDesc(Project::getUpdateTime)
                .orderByDesc(Project::getCreateTime);
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Project::getStatus, status);
        }
        
        List<Project> projects = projectMapper.selectList(wrapper);
        return Result.success(projects);
    }

    /**
     * 获取项目详情
     */
    @GetMapping("/{id}")
    public Result<Project> detail(@PathVariable Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null || Integer.valueOf(1).equals(project.getDeleted())) {
            return Result.error(404, "项目不存在");
        }
        return Result.success(project);
    }

    /**
     * 创建项目
     */
    @PostMapping("/create")
    public Result<String> create(@RequestBody Project project) {
        Result<String> validation = validateProject(project, false);
        if (validation != null) {
            return validation;
        }
        if (project.getProgress() == null) {
            project.setProgress(0);
        }
        if (project.getOwnerId() == null) {
            project.setOwnerId(1L);
        }
        project.setDeleted(0);
        projectMapper.insert(project);
        return Result.success("创建成功");
    }

    /**
     * 更新项目
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Project project) {
        Result<String> validation = validateProject(project, true);
        if (validation != null) {
            return validation;
        }
        projectMapper.updateById(project);
        return Result.success("更新成功");
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null || Integer.valueOf(1).equals(project.getDeleted())) {
            return Result.error(404, "项目不存在");
        }
        projectMapper.deleteById(id);
        return Result.success("删除成功");
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/stats")
    public Result<Object> stats() {
        List<Project> all = projectMapper.selectList(new LambdaQueryWrapper<Project>().eq(Project::getDeleted, 0));
        
        long total = all.size();
        long planning = all.stream().filter(p -> "规划中".equals(p.getStatus())).count();
        long inProgress = all.stream().filter(p -> "进行中".equals(p.getStatus())).count();
        long completed = all.stream().filter(p -> "已完成".equals(p.getStatus())).count();
        
        return Result.success(Map.of(
            "total", total,
            "planning", planning,
            "inProgress", inProgress,
            "completed", completed
        ));
    }

    private Result<String> validateProject(Project project, boolean requireId) {
        if (project == null) {
            return Result.error(400, "请求参数不能为空");
        }
        if (requireId && project.getId() == null) {
            return Result.error(400, "项目ID不能为空");
        }
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            return Result.error(400, "项目名称不能为空");
        }
        if (project.getStatus() == null || project.getStatus().trim().isEmpty()) {
            return Result.error(400, "项目状态不能为空");
        }
        if (!List.of("规划中", "进行中", "已完成").contains(project.getStatus())) {
            return Result.error(400, "项目状态不合法");
        }
        if (project.getProgress() != null && (project.getProgress() < 0 || project.getProgress() > 100)) {
            return Result.error(400, "项目进度必须在 0 到 100 之间");
        }
        if (requireId) {
            Project existingProject = projectMapper.selectById(project.getId());
            if (existingProject == null || Integer.valueOf(1).equals(existingProject.getDeleted())) {
                return Result.error(404, "项目不存在");
            }
            if (project.getOwnerId() == null) {
                project.setOwnerId(existingProject.getOwnerId());
            }
            if (project.getProgress() == null) {
                project.setProgress(existingProject.getProgress());
            }
            project.setDeleted(existingProject.getDeleted());
        }
        return null;
    }
}
