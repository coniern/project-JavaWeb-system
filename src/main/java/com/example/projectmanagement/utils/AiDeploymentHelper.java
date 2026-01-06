package com.example.projectmanagement.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI部署辅助工具类
 * 用于分析部署失败的原因并提供解决方案建议
 */
public class AiDeploymentHelper {

    // 错误问题库 - 键：错误关键词，值：解决方案
    private static final Map<String, String> ERROR_SOLUTION_MAP = new HashMap<>();

    static {
        // 初始化错误问题库
        // 网络相关错误
        ERROR_SOLUTION_MAP.put("java.net.BindException", "端口被占用。解决方案：执行命令netstat -tlnp | grep 端口号 查看进程，再kill -9 进程ID，或者修改应用端口号后重新部署。");
        ERROR_SOLUTION_MAP.put("Connection refused", "连接被拒绝。解决方案：检查目标服务器是否启动，防火墙是否开放对应端口，网络连接是否正常。");
        ERROR_SOLUTION_MAP.put("java.net.UnknownHostException", "未知主机。解决方案：检查服务器IP地址或域名是否正确，DNS解析是否正常。");
        
        // 数据库相关错误
        ERROR_SOLUTION_MAP.put("Connection refused to database", "无法连接到数据库。解决方案：检查数据库IP是否正确，或服务是否启动，数据库用户名密码是否正确。");
        ERROR_SOLUTION_MAP.put("java.sql.SQLSyntaxErrorException", "SQL语法错误。解决方案：检查SQL语句是否正确，表名、字段名是否存在。");
        ERROR_SOLUTION_MAP.put("java.sql.SQLIntegrityConstraintViolationException", "数据库完整性约束违反。解决方案：检查数据是否符合约束条件，如外键约束、唯一约束等。");
        
        // 文件相关错误
        ERROR_SOLUTION_MAP.put("java.io.FileNotFoundException", "文件未找到。解决方案：检查文件路径是否正确，文件是否存在，权限是否足够。");
        ERROR_SOLUTION_MAP.put("java.io.IOException", "IO异常。解决方案：检查磁盘空间是否充足，文件权限是否正确，网络连接是否稳定。");
        
        // 服务相关错误
        ERROR_SOLUTION_MAP.put("Application failed to start", "应用启动失败。解决方案：检查应用配置文件是否正确，依赖是否完整，环境变量是否配置。");
        ERROR_SOLUTION_MAP.put("OutOfMemoryError", "内存溢出。解决方案：增加JVM内存分配，检查是否存在内存泄漏，优化代码。");
        ERROR_SOLUTION_MAP.put("StackOverflowError", "栈溢出。解决方案：检查是否存在无限递归调用，减少递归深度。");
        
        // SSH相关错误
        ERROR_SOLUTION_MAP.put("com.jcraft.jsch.JSchException", "SSH连接异常。解决方案：检查服务器IP、端口、用户名、密码或密钥是否正确，SSH服务是否正常运行。");
        
        // 其他常见错误
        ERROR_SOLUTION_MAP.put("Permission denied", "权限拒绝。解决方案：检查用户权限是否足够，文件或目录权限是否设置正确。");
        ERROR_SOLUTION_MAP.put("500 Internal Server Error", "服务器内部错误。解决方案：查看详细日志，定位具体错误原因。");
        ERROR_SOLUTION_MAP.put("404 Not Found", "资源未找到。解决方案：检查请求的URL路径是否正确，资源是否存在。");
    }

    /**
     * 分析部署日志，提供AI建议
     * @param deploymentLog 部署日志内容
     * @return AI建议解决方案
     */
    public static String analyzeDeploymentLog(String deploymentLog) {
        if (deploymentLog == null || deploymentLog.isEmpty()) {
            return "没有找到部署日志，无法提供AI分析建议。";
        }

        StringBuilder suggestions = new StringBuilder();
        suggestions.append("AI分析建议：\n");

        // 遍历错误问题库，查找匹配的错误关键词
        boolean foundError = false;
        for (Map.Entry<String, String> entry : ERROR_SOLUTION_MAP.entrySet()) {
            String errorKeyword = entry.getKey();
            String solution = entry.getValue();

            if (containsKeyword(deploymentLog, errorKeyword)) {
                suggestions.append("- [").append(errorKeyword).append("]：").append(solution).append("\n");
                foundError = true;
            }
        }

        // 如果没有找到预定义的错误关键词，尝试提取异常信息
        if (!foundError) {
            String exceptionInfo = extractExceptionInfo(deploymentLog);
            if (exceptionInfo != null && !exceptionInfo.isEmpty()) {
                suggestions.append("- 检测到异常：").append(exceptionInfo).append("\n");
                suggestions.append("  建议：检查相关服务配置，查看详细日志信息，或联系系统管理员。");
            } else {
                suggestions.append("未找到明显的错误模式，建议查看完整日志或联系技术支持。");
            }
        }

        return suggestions.toString();
    }

    /**
     * 检查日志中是否包含指定的关键词（支持正则表达式）
     */
    private static boolean containsKeyword(String log, String keyword) {
        try {
            Pattern pattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(log);
            return matcher.find();
        } catch (Exception e) {
            // 如果关键词不是有效的正则表达式，使用简单的字符串包含检查
            return log.toLowerCase().contains(keyword.toLowerCase());
        }
    }

    /**
     * 从日志中提取异常信息
     */
    private static String extractExceptionInfo(String log) {
        try {
            // 尝试匹配常见的异常格式
            Pattern pattern = Pattern.compile("(\\w+Exception|\\w+Error)(?:[:\\s]+(.*?))?(?:\\s+at\\s+|$)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(log);
            if (matcher.find()) {
                String exceptionType = matcher.group(1);
                String exceptionMessage = matcher.group(2);
                if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
                    return exceptionType + ": " + exceptionMessage.trim();
                }
                return exceptionType;
            }
        } catch (Exception e) {
            // 提取异常信息失败时不做处理
        }
        return null;
    }

    /**
     * 添加自定义的错误解决方案到问题库
     * @param errorKeyword 错误关键词
     * @param solution 解决方案
     */
    public static void addCustomSolution(String errorKeyword, String solution) {
        ERROR_SOLUTION_MAP.put(errorKeyword, solution);
    }

    /**
     * 获取当前问题库中的所有错误关键词和解决方案
     */
    public static Map<String, String> getAllSolutions() {
        return new HashMap<>(ERROR_SOLUTION_MAP);
    }
    
    /**
     * 获取常见错误解决方案
     */
    public static Map<String, String> getCommonErrors() {
        return getAllSolutions();
    }
}