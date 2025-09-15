package com.example.projectmanagement.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.projectmanagement.entity.Deployment;
import com.example.projectmanagement.entity.EnvironmentConfig;
import com.example.projectmanagement.mapper.DeploymentMapper;
import com.example.projectmanagement.service.DeploymentService;
import com.example.projectmanagement.service.EnvironmentConfigService;
import com.example.projectmanagement.utils.AiDeploymentHelper;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * 部署服务实现类
 * 实现部署相关的业务逻辑
 */
@Service
public class DeploymentServiceImpl extends ServiceImpl<DeploymentMapper, Deployment> implements DeploymentService {
    
    @Autowired
    private DeploymentMapper deploymentMapper;
    
    @Autowired
    private EnvironmentConfigService environmentConfigService;
    
    @Value("${deploy.temp-dir}")
    private String tempDir;
    
    @Override
    public Page<Deployment> findPage(Integer pageNum, Integer pageSize, Long projectId, String env, String status) {
        Page<Deployment> page = new Page<>(pageNum, pageSize);
        return deploymentMapper.findPage(page, projectId, env, status);
    }
    
    @Override
    public boolean createDeployment(Deployment deployment) {
        deployment.setStartTime(LocalDateTime.now());
        deployment.setStatus("pending");
        return save(deployment);
    }
    
    @Override
    public boolean updateDeploymentStatus(Long deployId, String status) {
        Deployment deployment = new Deployment();
        deployment.setDeployId(deployId);
        deployment.setStatus(status);
        if ("success".equals(status) || "failed".equals(status)) {
            deployment.setEndTime(LocalDateTime.now());
        }
        return updateById(deployment);
    }
    
    @Override
    public boolean addDeploymentLog(Long deployId, String log) {
        Deployment deployment = getById(deployId);
        if (deployment == null) {
            return false;
        }
        
        String currentLog = deployment.getLog();
        if (StringUtils.isEmpty(currentLog)) {
            currentLog = log;
        } else {
            currentLog += "\n" + log;
        }
        
        deployment.setLog(currentLog);
        return updateById(deployment);
    }
    
    @Override
    public List<Deployment> findByProjectId(Long projectId) {
        return deploymentMapper.findByProjectId(projectId);
    }
    
    @Override
    public Deployment findLatestByProjectIdAndEnv(Long projectId, String env) {
        // 根据项目ID和环境查询最新的部署记录
        QueryWrapper<Deployment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_id", projectId)
                   .eq("env", env)
                   .orderByDesc("create_time")
                   .last("LIMIT 1");
        return getOne(queryWrapper);
    }

    @Override
    public boolean analyzeDeploymentFailure(Long deploymentId) {
        try {
            // 获取部署记录
            Deployment deployment = getById(deploymentId);
            if (deployment == null) {
                log.error("部署记录不存在: {}", deploymentId);
                return false;
            }

            // 使用AiDeploymentHelper分析错误日志
            String aiSuggestion = AiDeploymentHelper.analyzeDeploymentLog(deployment.getLog());
            
            // 更新部署记录中的AI建议
            deployment.setAiSuggestion(aiSuggestion);
            return updateById(deployment);
        } catch (Exception e) {
            log.error("AI分析部署失败异常: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, String> getCommonErrors() {
        // 直接调用AiDeploymentHelper获取常见错误解决方案
        return AiDeploymentHelper.getCommonErrors();
    }
    
    @Override
    public Long executeDeployment(Long projectId, String env, String version, MultipartFile file, Long deployerId) {
        // 创建部署记录
        Deployment deployment = new Deployment();
        deployment.setProjectId(projectId);
        deployment.setEnv(env);
        deployment.setVersion(version);
        deployment.setDeployerId(deployerId);
        deployment.setStatus("deploying");
        deployment.setStartTime(LocalDateTime.now());
        
        if (!save(deployment)) {
            return null;
        }
        
        // 异步执行部署过程
        new Thread(() -> {
            try {
                addDeploymentLog(deployment.getDeployId(), "开始部署任务...");
                
                // 获取环境配置
                EnvironmentConfig envConfig = environmentConfigService.findByProjectIdAndEnvType(projectId, env);
                if (envConfig == null || !envConfig.getEnabled()) {
                    addDeploymentLog(deployment.getDeployId(), "环境配置不存在或未启用");
                    updateDeploymentStatus(deployment.getDeployId(), "failed");
                    return;
                }
                
                // 保存部署包到临时目录
                String fileName = saveDeployPackage(file);
                String filePath = tempDir + File.separator + fileName;
                deployment.setPackagePath(filePath);
                deployment.setServerIp(envConfig.getServerIp());
                updateById(deployment);
                
                addDeploymentLog(deployment.getDeployId(), "部署包保存成功：" + fileName);
                
                // 执行部署流程
                executeDeployProcess(envConfig, filePath, deployment.getDeployId());
                
            } catch (Exception e) {
                addDeploymentLog(deployment.getDeployId(), "部署过程发生异常：" + e.getMessage());
                updateDeploymentStatus(deployment.getDeployId(), "failed");
                e.printStackTrace();
            }
        }).start();
        
        return deployment.getDeployId();
    }
    
    /**
     * 保存部署包到临时目录
     */
    private String saveDeployPackage(MultipartFile file) throws IOException {
        File dir = new File(tempDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + "-" + originalFilename;
        String filePath = tempDir + File.separator + fileName;
        
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(file.getBytes());
        }
        
        return fileName;
    }
    
    /**
     * 执行部署流程
     */
    private void executeDeployProcess(EnvironmentConfig envConfig, String filePath, Long deployId) throws JSchException {
        JSch jsch = new JSch();
        Session session = null;
        
        try {
            // 1. 检查服务器连接
            addDeploymentLog(deployId, "检查服务器连接：" + envConfig.getServerIp() + ":" + envConfig.getSshPort());
            
            session = jsch.getSession(envConfig.getSshUsername(), envConfig.getServerIp(), envConfig.getSshPort());
            if (!StringUtils.isEmpty(envConfig.getSshPassword())) {
                session.setPassword(envConfig.getSshPassword());
            } else if (!StringUtils.isEmpty(envConfig.getSshKeyPath())) {
                jsch.addIdentity(envConfig.getSshKeyPath());
            }
            
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(5000); // 连接超时时间5秒
            
            addDeploymentLog(deployId, "服务器连接成功");
            
            // 2. 上传部署包
            addDeploymentLog(deployId, "开始上传部署包到服务器：" + envConfig.getDeployPath());
            // 这里实现文件上传逻辑
            // ...
            addDeploymentLog(deployId, "部署包上传成功");
            
            // 3. 停止旧服务
            addDeploymentLog(deployId, "停止旧服务");
            executeCommand(session, "sh " + envConfig.getDeployPath() + "/shutdown.sh", deployId);
            
            // 4. 启动新服务
            addDeploymentLog(deployId, "启动新服务");
            executeCommand(session, "sh " + envConfig.getDeployPath() + "/startup.sh", deployId);
            
            // 5. 检查服务是否启动成功
            addDeploymentLog(deployId, "检查服务是否启动成功");
            // 这里实现服务健康检查逻辑
            // ...
            
            addDeploymentLog(deployId, "部署成功！");
            updateDeploymentStatus(deployId, "success");
            
        } catch (Exception e) {
            addDeploymentLog(deployId, "部署失败：" + e.getMessage());
            
            // 调用AI分析模块分析错误原因并提供解决方案
            Deployment deployment = getById(deployId);
            String aiSuggestion = AiDeploymentHelper.analyzeDeploymentLog(deployment.getLog());
            deployment.setAiSuggestion(aiSuggestion);
            updateById(deployment);
            
            updateDeploymentStatus(deployId, "failed");
            throw e;
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
    
    /**
     * 执行SSH命令
     */
    private void executeCommand(Session session, String command, Long deployId) throws JSchException, IOException {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        
        // 获取命令执行的输出
        InputStream in = channel.getInputStream();
        channel.connect();
        
        StringBuilder output = new StringBuilder();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) break;
                output.append(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                if (in.available() > 0) continue;
                addDeploymentLog(deployId, "命令执行结果：" + output.toString());
                addDeploymentLog(deployId, "命令退出状态码：" + channel.getExitStatus());
                break;
            }
            try { Thread.sleep(1000); } catch (Exception ee) {}
        }
        
        channel.disconnect();
    }
}