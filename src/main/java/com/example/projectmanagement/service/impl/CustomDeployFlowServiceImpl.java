package com.example.projectmanagement.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.projectmanagement.entity.CustomDeployFlow;
import com.example.projectmanagement.entity.Deployment;
import com.example.projectmanagement.entity.FlowExecution;
import com.example.projectmanagement.mapper.CustomDeployFlowMapper;
import com.example.projectmanagement.mapper.FlowExecutionMapper;
import com.example.projectmanagement.service.CustomDeployFlowService;
import com.example.projectmanagement.service.DeploymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 自定义部署流程服务实现类
 * 实现自定义部署流程相关的业务逻辑
 */
@Service
public class CustomDeployFlowServiceImpl extends ServiceImpl<CustomDeployFlowMapper, CustomDeployFlow> implements CustomDeployFlowService {

    @Autowired
    private CustomDeployFlowMapper customDeployFlowMapper;

    @Autowired
    private FlowExecutionMapper flowExecutionMapper;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Page<CustomDeployFlow> findPage(Integer pageNum, Integer pageSize, Long projectId, String flowName) {
        Page<CustomDeployFlow> page = new Page<>(pageNum, pageSize);
        return customDeployFlowMapper.findPage(page, projectId, flowName);
    }

    @Override
    public List<CustomDeployFlow> findByProjectId(Long projectId) {
        return customDeployFlowMapper.findByProjectId(projectId);
    }

    @Override
    public CustomDeployFlow findDefaultFlowByProjectId(Long projectId) {
        return customDeployFlowMapper.findDefaultFlowByProjectId(projectId);
    }

    @Transactional
    @Override
    public boolean setDefaultFlow(Long projectId, Long flowId) {
        // 先将所有该项目的流程设置为非默认
        List<CustomDeployFlow> flows = customDeployFlowMapper.findByProjectId(projectId);
        for (CustomDeployFlow flow : flows) {
            flow.setIsDefault(false);
            updateById(flow);
        }
        
        // 再将指定流程设置为默认
        CustomDeployFlow targetFlow = getById(flowId);
        if (targetFlow != null && targetFlow.getProjectId().equals(projectId)) {
            targetFlow.setIsDefault(true);
            return updateById(targetFlow);
        }
        return false;
    }

    @Override
    public Long executeCustomFlow(Long flowId, Long projectId, String env, String version, Long deployerId) {
        // 创建流程执行记录
        FlowExecution execution = new FlowExecution();
        execution.setFlowId(flowId);
        execution.setProjectId(projectId);
        execution.setEnv(env);
        execution.setVersion(version);
        execution.setStatus("running");
        execution.setCurrentStepIndex(0);
        execution.setStartTime(LocalDateTime.now());
        execution.setDeployerId(deployerId);
        
        if (flowExecutionMapper.insert(execution) == 0) {
            return null;
        }
        
        // 异步执行流程
        new Thread(() -> {
            try {
                executeFlowSteps(execution);
            } catch (Exception e) {
                execution.setStatus("failed");
                execution.setExecutionLog(execution.getExecutionLog() + "\n流程执行异常：" + e.getMessage());
                execution.setEndTime(LocalDateTime.now());
                flowExecutionMapper.updateById(execution);
            }
        }).start();
        
        return execution.getExecutionId();
    }

    @Override
    public boolean resumeFlowExecution(Long flowExecutionId) {
        FlowExecution execution = flowExecutionMapper.selectById(flowExecutionId);
        if (execution != null && "paused".equals(execution.getStatus())) {
            execution.setStatus("running");
            flowExecutionMapper.updateById(execution);
            
            // 继续执行流程
            new Thread(() -> {
                try {
                    executeFlowSteps(execution);
                } catch (Exception e) {
                    execution.setStatus("failed");
                    execution.setExecutionLog(execution.getExecutionLog() + "\n流程执行异常：" + e.getMessage());
                    execution.setEndTime(LocalDateTime.now());
                    flowExecutionMapper.updateById(execution);
                }
            }).start();
            
            return true;
        }
        return false;
    }

    @Override
    public boolean pauseFlowExecution(Long flowExecutionId) {
        FlowExecution execution = flowExecutionMapper.selectById(flowExecutionId);
        if (execution != null && "running".equals(execution.getStatus())) {
            execution.setStatus("paused");
            return flowExecutionMapper.updateById(execution) > 0;
        }
        return false;
    }

    /**
     * 执行流程步骤
     */
    private void executeFlowSteps(FlowExecution execution) throws Exception {
        CustomDeployFlow flow = customDeployFlowMapper.selectById(execution.getFlowId());
        if (flow == null) {
            execution.setStatus("failed");
            execution.setExecutionLog("流程不存在");
            flowExecutionMapper.updateById(execution);
            return;
        }
        
        JsonNode flowConfig = flow.getFlowConfig();
        JsonNode steps = flowConfig.get("steps");
        
        if (steps == null || !steps.isArray()) {
            execution.setStatus("failed");
            execution.setExecutionLog("流程配置无效");
            flowExecutionMapper.updateById(execution);
            return;
        }
        
        int totalSteps = steps.size();
        
        // 从当前步骤开始执行
        for (int i = execution.getCurrentStepIndex(); i < totalSteps; i++) {
            // 检查执行状态，如果被暂停则退出循环
            FlowExecution currentExecution = flowExecutionMapper.selectById(execution.getExecutionId());
            if (currentExecution != null && "paused".equals(currentExecution.getStatus())) {
                execution.setStatus("paused");
                execution.setCurrentStepIndex(i);
                flowExecutionMapper.updateById(execution);
                return;
            }
            
            JsonNode step = steps.get(i);
            String stepType = step.get("type").asText();
            
            // 记录步骤开始
            addExecutionLog(execution, "开始执行步骤 " + (i + 1) + ": " + stepType);
            
            try {
                // 执行具体步骤
                executeStep(execution, step);
                addExecutionLog(execution, "步骤 " + (i + 1) + " 执行成功");
                
                // 更新当前步骤索引
                execution.setCurrentStepIndex(i + 1);
                flowExecutionMapper.updateById(execution);
                
            } catch (Exception e) {
                addExecutionLog(execution, "步骤 " + (i + 1) + " 执行失败: " + e.getMessage());
                execution.setStatus("failed");
                execution.setCurrentStepIndex(i);
                execution.setEndTime(LocalDateTime.now());
                flowExecutionMapper.updateById(execution);
                return;
            }
        }
        
        // 所有步骤执行完成
        execution.setStatus("completed");
        execution.setEndTime(LocalDateTime.now());
        addExecutionLog(execution, "流程执行完成");
        flowExecutionMapper.updateById(execution);
    }

    /**
     * 执行单个流程步骤
     */
    private void executeStep(FlowExecution execution, JsonNode step) throws Exception {
        String stepType = step.get("type").asText();
        
        switch (stepType) {
            case "upload":
                // 实现文件上传步骤
                executeUploadStep(execution, step);
                break;
            case "shell":
                // 实现Shell脚本执行步骤
                executeShellStep(execution, step);
                break;
            case "notify":
                // 实现通知发送步骤
                executeNotifyStep(execution, step);
                break;
            case "condition":
                // 实现条件判断步骤
                executeConditionStep(execution, step);
                break;
            case "manual_confirm":
                // 实现人工确认步骤
                executeManualConfirmStep(execution, step);
                break;
            default:
                throw new UnsupportedOperationException("不支持的步骤类型: " + stepType);
        }
    }

    /**
     * 执行文件上传步骤
     */
    private void executeUploadStep(FlowExecution execution, JsonNode step) throws Exception {
        // 这里实现文件上传的逻辑
        // 由于我们没有实际的文件，这里仅作为示例
        String path = step.get("path").asText();
        addExecutionLog(execution, "上传文件到路径: " + path);
    }

    /**
     * 执行Shell脚本步骤
     */
    private void executeShellStep(FlowExecution execution, JsonNode step) throws Exception {
        // 这里实现Shell脚本执行的逻辑
        String cmd = step.get("cmd").asText();
        addExecutionLog(execution, "执行Shell命令: " + cmd);
        // 实际项目中，这里应该通过SSH或其他方式执行命令
    }

    /**
     * 执行通知发送步骤
     */
    private void executeNotifyStep(FlowExecution execution, JsonNode step) throws Exception {
        // 这里实现通知发送的逻辑
        String message = step.get("message").asText();
        String type = step.has("notifyType") ? step.get("notifyType").asText() : "email";
        addExecutionLog(execution, "发送" + type + "通知: " + message);
        // 实际项目中，这里应该调用通知服务
    }

    /**
     * 执行条件判断步骤
     */
    private void executeConditionStep(FlowExecution execution, JsonNode step) throws Exception {
        // 这里实现条件判断的逻辑
        String condition = step.get("condition").asText();
        addExecutionLog(execution, "执行条件判断: " + condition);
        
        // 示例：检查上一次部署是否成功
        boolean conditionMet = false;
        if ("last_deploy_success".equals(condition)) {
            Deployment lastDeployment = deploymentService.findLatestByProjectIdAndEnv(execution.getProjectId(), execution.getEnv());
            conditionMet = lastDeployment != null && "success".equals(lastDeployment.getStatus());
        }
        
        addExecutionLog(execution, "条件判断结果: " + (conditionMet ? "满足" : "不满足"));
        
        // 如果条件不满足，抛出异常中断流程
        if (!conditionMet && step.has("failOnConditionNotMet") && step.get("failOnConditionNotMet").asBoolean()) {
            throw new Exception("条件不满足，流程中断");
        }
    }

    /**
     * 执行人工确认步骤
     */
    private void executeManualConfirmStep(FlowExecution execution, JsonNode step) throws Exception {
        // 设置流程为暂停状态，等待用户确认
        execution.setStatus("paused");
        String message = step.has("message") ? step.get("message").asText() : "请确认是否继续执行流程";
        addExecutionLog(execution, "等待人工确认: " + message);
        flowExecutionMapper.updateById(execution);
        
        // 使用CountDownLatch等待用户确认
        CountDownLatch latch = new CountDownLatch(1);
        // 这里应该有一个机制让用户确认后释放latch
        // 为了简化示例，我们假设用户立即确认
        Thread.sleep(2000); // 模拟等待时间
        addExecutionLog(execution, "已获得人工确认，继续执行流程");
    }

    /**
     * 添加执行日志
     */
    private void addExecutionLog(FlowExecution execution, String log) {
        String currentLog = execution.getExecutionLog();
        if (currentLog == null) {
            currentLog = log;
        } else {
            currentLog += "\n" + log;
        }
        execution.setExecutionLog(currentLog);
        flowExecutionMapper.updateById(execution);
    }
}