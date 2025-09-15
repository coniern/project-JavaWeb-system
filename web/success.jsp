<%--
  Created by IntelliJ IDEA.
  User: 张雅
  Date: 2025/9/9
  Time: 15:51
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>操作成功</title>
    <!-- 引入Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <!-- 引入Font Awesome -->
    <link href="https://cdn.jsdelivr.net/npm/font-awesome@4.7.0/css/font-awesome.min.css" rel="stylesheet">

    <script>
        // 配置Tailwind自定义颜色
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        success: '#10b981',
                        successLight: '#dcfce7',
                    },
                    fontFamily: {
                        inter: ['Inter', 'system-ui', 'sans-serif'],
                    },
                }
            }
        }
    </script>

    <style type="text/tailwindcss">
        @layer utilities {
            .content-auto {
                content-visibility: auto;
            }
            .animate-bounce-slow {
                animation: bounce 2s infinite;
            }
            .animate-fade-in {
                animation: fadeIn 0.6s ease-out forwards;
            }
            .animate-slide-up {
                animation: slideUp 0.6s ease-out forwards;
            }
        }

        @keyframes bounce {
            0%, 100% { transform: translateY(0); }
            50% { transform: translateY(-10px); }
        }

        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }

        @keyframes slideUp {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
    </style>
</head>
<body class="font-inter bg-gray-50 min-h-screen flex items-center justify-center p-4">
<div class="w-full max-w-md">
    <!-- 成功提示卡片 -->
    <div class="bg-white rounded-2xl shadow-lg overflow-hidden transform transition-all duration-300 hover:shadow-xl animate-fade-in">
        <!-- 顶部状态指示区 -->
        <div class="bg-successLight p-6 text-center relative overflow-hidden">
            <div class="absolute inset-0 bg-gradient-to-r from-green-50 to-green-100 opacity-70"></div>
            <div class="relative z-10">
                <div class="w-16 h-16 bg-success/10 rounded-full flex items-center justify-center mx-auto mb-4 animate-bounce-slow">
                    <i class="fa fa-check text-success text-2xl"></i>
                </div>
                <h2 class="text-[clamp(1.5rem,5vw,2rem)] font-bold text-gray-800">操作成功</h2>
            </div>
        </div>

        <!-- 内容区 -->
        <div class="p-6 animate-slide-up" style="animation-delay: 0.2s">
            <p class="text-gray-600 text-center mb-8 text-lg">${xiaoxi}</p>

            <!-- 操作按钮 -->
            <div class="space-y-4">
                <a href="FindUserList"
                   class="block w-full py-3 px-4 bg-success hover:bg-success/90 text-white font-medium rounded-lg transition-all duration-200 text-center shadow-md hover:shadow-lg">
                    <i class="fa fa-users mr-2"></i>查看所有用户
                </a>

                <a href="index.jsp"
                   class="block w-full py-3 px-4 bg-gray-100 hover:bg-gray-200 text-gray-800 font-medium rounded-lg transition-all duration-200 text-center">
                    <i class="fa fa-home mr-2"></i>返回首页
                </a>

                <a href="javascript:history.back()"
                   class="block w-full py-3 px-4 border border-gray-200 hover:bg-gray-50 text-gray-700 font-medium rounded-lg transition-all duration-200 text-center">
                    <i class="fa fa-arrow-left mr-2"></i>返回上一页
                </a>
            </div>
        </div>
    </div>

    <!-- 页脚信息 -->
    <p class="text-center text-gray-400 text-sm mt-6 animate-slide-up" style="animation-delay: 0.4s">
        操作已成功完成 | 如有疑问，请联系管理员
    </p>
</div>
</body>
</html>

