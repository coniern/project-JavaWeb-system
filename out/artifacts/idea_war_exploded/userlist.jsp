<%--
  Created by IntelliJ IDEA.
  User: 张雅
  Date: 2025/9/9
  Time: 15:51
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <base href="<%=basePath%>">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>所有用户 - Javaweb项目</title>
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
                        primary: '#165DFF',
                        secondary: '#E8F3FF',
                        success: '#10b981',
                        danger: '#ef4444',
                        neutral: '#64748b'
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
            .table-shadow {
                box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
            }
            .transition-bg {
                transition: background-color 0.2s ease-in-out;
            }
            .scale-hover {
                transition: transform 0.2s ease-in-out;
            }
            .scale-hover:hover {
                transform: scale(1.02);
            }
        }
    </style>
</head>
<body class="font-inter bg-gray-50 min-h-screen">
<!-- 导航栏 -->
<header class="bg-white shadow-sm sticky top-0 z-50 transition-all duration-300">
    <div class="container mx-auto px-4 py-4 flex justify-between items-center">
        <div class="flex items-center space-x-3">
            <div class="w-10 h-10 bg-primary rounded-lg flex items-center justify-center">
                <i class="fa fa-users text-white text-xl"></i>
            </div>
            <h1 class="text-xl font-bold text-gray-800">用户管理系统</h1>
        </div>

        <nav class="hidden md:flex items-center space-x-8">
            <a href="<%=basePath%>" class="text-gray-600 hover:text-primary transition-colors">
                <i class="fa fa-home mr-1"></i>首页
            </a>
            <a href="#" class="text-primary font-medium border-b-2 border-primary pb-1">
                <i class="fa fa-list mr-1"></i>用户列表
            </a>
            <a href="register.jsp" class="text-gray-600 hover:text-primary transition-colors">
                <i class="fa fa-user-plus mr-1"></i>新增用户
            </a>
        </nav>

        <button class="md:hidden text-gray-600 focus:outline-none">
            <i class="fa fa-bars text-xl"></i>
        </button>
    </div>
</header>

<!-- 主要内容区 -->
<main class="container mx-auto px-4 py-8 md:py-12">
    <!-- 页面标题和状态提示 -->
    <div class="mb-8">
        <div class="flex flex-col md:flex-row md:items-center md:justify-between">
            <div>
                <h2 class="text-[clamp(1.5rem,3vw,2.5rem)] font-bold text-gray-800 mb-2">所有用户</h2>
                <p class="text-gray-600">管理系统中的所有用户信息</p>
            </div>

            <div class="mt-4 md:mt-0">
                <a href="register.jsp" class="inline-flex items-center px-5 py-2.5 bg-primary hover:bg-primary/90 text-white font-medium rounded-lg transition-all duration-200 shadow-sm hover:shadow scale-hover">
                    <i class="fa fa-plus-circle mr-2"></i>添加新用户
                </a>
            </div>
        </div>

        <!-- 状态提示 -->
        <c:if test="${not empty xiaoxi}">
            <div class="mt-4 p-3 bg-secondary text-primary rounded-lg inline-flex items-center">
                <i class="fa fa-info-circle mr-2"></i>
                <span>${xiaoxi}</span>
            </div>
        </c:if>
    </div>

    <!-- 用户列表卡片 -->
    <div class="bg-white rounded-xl table-shadow overflow-hidden">
        <!-- 表格区域 - 在小屏幕上可横向滚动 -->
        <div class="overflow-x-auto">
            <table class="w-full text-left">
                <thead class="bg-gray-50">
                <tr>
                    <th class="px-6 py-4 text-sm font-semibold text-gray-700 uppercase tracking-wider">ID</th>
                    <th class="px-6 py-4 text-sm font-semibold text-gray-700 uppercase tracking-wider">姓名</th>
                    <th class="px-6 py-4 text-sm font-semibold text-gray-700 uppercase tracking-wider">性别</th>
                    <th class="px-6 py-4 text-sm font-semibold text-gray-700 uppercase tracking-wider">密码</th>
                    <th class="px-6 py-4 text-sm font-semibold text-gray-700 uppercase tracking-wider">家乡</th>
                    <th class="px-6 py-4 text-sm font-semibold text-gray-700 uppercase tracking-wider">备注</th>
                    <th class="px-6 py-4 text-sm font-semibold text-gray-700 uppercase tracking-wider">操作</th>
                </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                <c:forEach var="U" items="${userAll}">
                    <form action="UpdateUser" method="post">
                        <tr class="hover:bg-gray-50 transition-bg">
                            <td class="px-6 py-4">
                                <input type="text" value="${U.id}" name="id"
                                       class="w-16 px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all">
                            </td>
                            <td class="px-6 py-4">
                                <input type="text" value="${U.name}" name="name"
                                       class="w-24 px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all">
                            </td>
                            <td class="px-6 py-4">
                                <input type="text" value="${U.sex}" name="sex"
                                       class="w-16 px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all">
                            </td>
                            <td class="px-6 py-4">
                                <input type="text" value="${U.pwd}" name="pwd"
                                       class="w-24 px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all">
                            </td>
                            <td class="px-6 py-4">
                                <input type="text" value="${U.home}" name="home"
                                       class="w-24 px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all">
                            </td>
                            <td class="px-6 py-4">
                                <input type="text" value="${U.info}" name="info"
                                       class="w-40 px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all">
                            </td>
                            <td class="px-6 py-4">
                                <div class="flex space-x-3">
                                    <a href="DeleteUser?id=${U.id}"
                                       class="inline-flex items-center px-3 py-1.5 text-danger hover:text-white hover:bg-danger/90 border border-danger rounded-md text-sm font-medium transition-all"
                                       onclick="return confirm('确定要删除ID为 ${U.id} 的用户吗？此操作不可恢复。')">
                                        <i class="fa fa-trash-o mr-1"></i>删除
                                    </a>
                                    <button type="submit"
                                            class="inline-flex items-center px-3 py-1.5 bg-success text-white hover:bg-success/90 rounded-md text-sm font-medium transition-all">
                                        <i class="fa fa-pencil mr-1"></i>更新
                                    </button>
                                </div>
                            </td>
                        </tr>
                    </form>
                </c:forEach>
                </tbody>
            </table>
        </div>

        <!-- 空状态显示 -->
        <c:if test="${empty userAll}">
            <div class="py-20 text-center">
                <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                    <i class="fa fa-user-o text-gray-400 text-2xl"></i>
                </div>
                <h3 class="text-lg font-medium text-gray-900 mb-2">暂无用户数据</h3>
                <p class="text-gray-500 max-w-md mx-auto mb-6">系统中还没有任何用户信息，点击下方按钮添加第一个用户吧</p>
                <a href="register.jsp" class="inline-flex items-center px-5 py-2.5 bg-primary hover:bg-primary/90 text-white font-medium rounded-lg transition-all duration-200 shadow-sm hover:shadow">
                    <i class="fa fa-plus mr-2"></i>添加用户
                </a>
            </div>
        </c:if>
    </div>
</main>

<!-- 页脚 -->
<footer class="bg-gray-800 text-white py-8 mt-16">
    <div class="container mx-auto px-4">
        <div class="flex flex-col md:flex-row justify-between items-center">
            <div class="mb-4 md:mb-0">
                <p class="text-gray-400 text-sm">© 2023 Javaweb用户管理系统</p>
            </div>
            <div class="flex space-x-6">
                <a href="#" class="text-gray-400 hover:text-white transition-colors">
                    <i class="fa fa-question-circle"></i> 帮助中心
                </a>
                <a href="#" class="text-gray-400 hover:text-white transition-colors">
                    <i class="fa fa-file-text-o"></i> 使用说明
                </a>
            </div>
        </div>
    </div>
</footer>

<script>
    // 导航栏滚动效果
    window.addEventListener('scroll', function() {
        const header = document.querySelector('header');
        if (window.scrollY > 10) {
            header.classList.add('py-2', 'shadow');
            header.classList.remove('py-4', 'shadow-sm');
        } else {
            header.classList.add('py-4', 'shadow-sm');
            header.classList.remove('py-2', 'shadow');
        }
    });

    // 为表格行添加淡入动画
    document.addEventListener('DOMContentLoaded', function() {
        const rows = document.querySelectorAll('tbody tr');
        rows.forEach((row, index) => {
            row.style.opacity = '0';
            row.style.transform = 'translateY(10px)';
            row.style.transition = 'opacity 0.3s ease, transform 0.3s ease';

            setTimeout(() => {
                row.style.opacity = '1';
                row.style.transform = 'translateY(0)';
            }, 100 + (index * 50));
        });
    });
</script>
</body>
</html>

