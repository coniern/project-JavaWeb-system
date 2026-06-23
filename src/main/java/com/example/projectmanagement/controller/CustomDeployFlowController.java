package com.example.projectmanagement.controller;

/*
 * 自定义部署流程模块保留为后续扩展，当前最小可运行版本不注册该控制器。
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmanagement.entity.CustomDeployFlow;
import com.example.projectmanagement.entity.FlowExecution;
import com.example.projectmanagement.service.CustomDeployFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 自定义部署流程控制器
 * 提供自定义部署流程编排相关的API接口
 */
public class CustomDeployFlowController {

    @Autowired
    private CustomDeployFlowService customDeployFlowService;

    /**
     * 分页查询自定义部署流程
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param projectId 项目ID
     * @param flowName 流程名称
     * @return 流程分页列表
     */
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                      @RequestParam(defaultValue = "10") Integer pageSize,
                      @RequestParam(required = false) Long projectId,
                      @RequestParam(required = false) String flowName) {
        try {
            Page<CustomDeployFlow> page = customDeployFlowService.findPage(pageNum, pageSize, projectId, flowName);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("查询流程列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据项目ID查询部署流程
     * @param projectId 项目ID
     * @return 流程列表
     */
    @GetMapping("/project/{projectId}")
    public Result findByProjectId(@PathVariable Long projectId) {
        try {
            List<CustomDeployFlow> flows = customDeployFlowService.findByProjectId(projectId);
            return Result.success(flows);
        } catch (Exception e) {
            return Result.error("查询项目流程失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目的默认部署流程
     * @param projectId 项目ID
     * @return 默认流程
     */
    @GetMapping("/default/{projectId}")
    public Result findDefaultFlow(@PathVariable Long projectId) {
        try {
            CustomDeployFlow defaultFlow = customDeployFlowService.findDefaultFlowByProjectId(projectId);
            return Result.success(defaultFlow);
        } catch (Exception e) {
            return Result.error("获取默认流程失败: " + e.getMessage());
        }
    }

    /**
     * 设置项目的默认部署流程
     * @param projectId 项目ID
     * @param flowId 流程ID
     * @return 设置结果
     */
    @PostMapping("/set-default")
    public Result setDefaultFlow(@RequestParam Long projectId, @RequestParam Long flowId) {
        try {
            boolean result = customDeployFlowService.setDefaultFlow(projectId, flowId);
            if (result) {
                return Result.success("设置成功");
            } else {
                return Result.error("设置失败");
            }
        } catch (Exception e) {
            return Result.error("设置默认流程失败: " + e.getMessage());
        }
    }

    /**
     * 创建自定义部署流程
     * @param flow 流程信息
     * @return 创建结果
     */
    @PostMapping
    public Result createFlow(@RequestBody CustomDeployFlow flow) {
        try {
            boolean result = customDeployFlowService.save(flow);
            if (result) {
                return Result.success(flow);
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            return Result.error("创建流程失败: " + e.getMessage());
        }
    }

    /**
     * 更新自定义部署流程
     * @param flow 流程信息
     * @return 更新结果
     */
    @PutMapping
    public Result updateFlow(@RequestBody CustomDeployFlow flow) {
        try {
            boolean result = customDeployFlowService.updateById(flow);
            if (result) {
                return Result.success(flow);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新流程失败: " + e.getMessage());
        }
    }

    /**
     * 删除自定义部署流程
     * @param flowId 流程ID
     * @return 删除结果
     */
    @DeleteMapping("/{flowId}")
    public Result deleteFlow(@PathVariable Long flowId) {
        try {
            boolean result = customDeployFlowService.removeById(flowId);
            if (result) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除流程失败: " + e.getMessage());
        }
    }

    /**
     * 执行自定义部署流程
     * @param params 执行参数
     * @return 执行结果
     */
    @PostMapping("/execute")
    public Result executeFlow(@RequestBody Map<String, Object> params) {
        try {
            Long flowId = ((Number) params.get("flowId")).longValue();
            Long projectId = ((Number) params.get("projectId")).longValue();
            String env = (String) params.get("env");
            String version = (String) params.get("version");
            Long deployerId = ((Number) params.get("deployerId")).longValue();
            
            Long executionId = customDeployFlowService.executeCustomFlow(flowId, projectId, env, version, deployerId);
            if (executionId != null) {
                return Result.success(executionId);
            } else {
                return Result.error("执行失败");
            }
        } catch (Exception e) {
            return Result.error("执行流程失败: " + e.getMessage());
        }
    }

    /**
     * 继续执行暂停的流程
     * @param flowExecutionId 流程执行ID
     * @return 继续执行结果
     */
    @PostMapping("/resume/{flowExecutionId}")
    public Result resumeFlow(@PathVariable Long flowExecutionId) {
        try {
            boolean result = customDeployFlowService.resumeFlowExecution(flowExecutionId);
            if (result) {
                return Result.success("继续执行成功");
            } else {
                return Result.error("继续执行失败");
            }
        } catch (Exception e) {
            return Result.error("继续执行流程失败: " + e.getMessage());
        }
    }

    /**
     * 暂停正在执行的流程
     * @param flowExecutionId 流程执行ID
     * @return 暂停结果
     */
    @PostMapping("/pause/{flowExecutionId}")
    public Result pauseFlow(@PathVariable Long flowExecutionId) {
        try {
            boolean result = customDeployFlowService.pauseFlowExecution(flowExecutionId);
            if (result) {
                return Result.success("暂停成功");
            } else {
                return Result.error("暂停失败");
            }
        } catch (Exception e) {
            return Result.error("暂停流程失败: " + e.getMessage());
        }
    }

    /**
     * 自定义结果类，用于统一API响应格式
     */
    public static class Result {
        private int code;
        private String message;
        private Object data;

        public Result(int code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        public static Result success(Object data) {
            return new Result(200, "success", data);
        }

        public static Result error(String message) {
            return new Result(500, message, null);
        }

        // getter and setter
        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}
