<%--
  Created by IntelliJ IDEA.
  User: 张雅
  Date: 2025/9/9
  Time: 13:16
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
  String path = request.getContextPath();
  String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>首页 - 第一个Javaweb项目</title>
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
      .animate-fade-in {
        animation: fadeIn 0.8s ease-out forwards;
      }
      .animate-slide-up {
        animation: slideUp 0.6s ease-out forwards;
      }
      .animate-pulse-slow {
        animation: pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite;
      }
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
<body class="font-inter bg-gray-50 min-h-screen flex flex-col">
<!-- 导航栏 -->
<header class="bg-white shadow-sm sticky top-0 z-10 transition-all duration-300">
  <div class="container mx-auto px-4 py-4 flex justify-between items-center">
    <div class="flex items-center space-x-2">
      <div class="w-10 h-10 bg-primary rounded-lg flex items-center justify-center">
        <i class="fa fa-code text-white text-xl"></i>
      </div>
      <h1 class="text-xl font-bold text-gray-800">Javaweb项目</h1>
    </div>

    <nav class="hidden md:flex items-center space-x-8">
      <a href="#" class="text-primary font-medium hover:text-primary/80 transition-colors">首页</a>
      <a href="#" class="text-gray-600 hover:text-primary transition-colors">功能</a>
      <a href="#" class="text-gray-600 hover:text-primary transition-colors">关于</a>
      <a href="#" class="text-gray-600 hover:text-primary transition-colors">帮助</a>
    </nav>

    <div class="flex items-center space-x-4">
      <a href="login.jsp" class="hidden md:block px-5 py-2 rounded-lg border border-primary text-primary hover:bg-primary/5 transition-colors">登录</a>
      <a href="register.jsp" class="px-5 py-2 rounded-lg bg-primary text-white hover:bg-primary/90 shadow-sm hover:shadow transition-all">注册</a>
      <button class="md:hidden text-gray-600">
        <i class="fa fa-bars text-xl"></i>
      </button>
    </div>
  </div>
</header>

<!-- 主要内容区 -->
<main class="flex-grow">
  <!-- 英雄区域 -->
  <section class="bg-gradient-to-br from-secondary to-white py-16 md:py-24">
    <div class="container mx-auto px-4">
      <div class="max-w-3xl mx-auto text-center">
        <h2 class="text-[clamp(2rem,5vw,3.5rem)] font-bold text-gray-800 leading-tight mb-6 animate-fade-in">
          第一个<span class="text-primary">Javaweb</span>项目
        </h2>
        <p class="text-gray-600 text-lg md:text-xl mb-10 animate-slide-up" style="animation-delay: 0.2s">
          探索Javaweb开发的无限可能，从这个基础项目开始你的编程之旅
        </p>
        <div class="flex flex-col sm:flex-row justify-center gap-4 animate-slide-up" style="animation-delay: 0.4s">
          <a href="#" class="px-8 py-3 bg-primary text-white rounded-lg font-medium shadow-md hover:shadow-lg hover:bg-primary/90 transition-all">
            开始探索 <i class="fa fa-arrow-right ml-2"></i>
          </a>
          <a href="#" class="px-8 py-3 bg-white text-gray-700 rounded-lg font-medium border border-gray-200 hover:bg-gray-50 transition-all">
            查看文档 <i class="fa fa-book ml-2"></i>
          </a>
        </div>
      </div>
    </div>
  </section>

  <!-- 功能卡片区域 -->
  <section class="py-16 bg-white">
    <div class="container mx-auto px-4">
      <div class="text-center mb-16">
        <h3 class="text-2xl md:text-3xl font-bold text-gray-800 mb-4">项目功能</h3>
        <p class="text-gray-600 max-w-2xl mx-auto">这个Javaweb项目包含了基础的功能模块，为你的学习和开发提供良好的起点</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <!-- 功能卡片1 -->
        <div class="bg-gray-50 rounded-xl p-6 shadow-sm hover:shadow-md transition-all border border-gray-100">
          <div class="w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center mb-4">
            <i class="fa fa-user text-primary text-xl"></i>
          </div>
          <h4 class="text-xl font-semibold text-gray-800 mb-2">用户管理</h4>
          <p class="text-gray-600">实现用户注册、登录和信息管理等基础功能</p>
        </div>

        <!-- 功能卡片2 -->
        <div class="bg-gray-50 rounded-xl p-6 shadow-sm hover:shadow-md transition-all border border-gray-100">
          <div class="w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center mb-4">
            <i class="fa fa-database text-primary text-xl"></i>
          </div>
          <h4 class="text-xl font-semibold text-gray-800 mb-2">数据存储</h4>
          <p class="text-gray-600">与数据库交互，实现数据的增删改查操作</p>
        </div>

        <!-- 功能卡片3 -->
        <div class="bg-gray-50 rounded-xl p-6 shadow-sm hover:shadow-md transition-all border border-gray-100">
          <div class="w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center mb-4">
            <i class="fa fa-cogs text-primary text-xl"></i>
          </div>
          <h4 class="text-xl font-semibold text-gray-800 mb-2">业务逻辑</h4>
          <p class="text-gray-600">核心业务逻辑处理，实现项目的主要功能</p>
        </div>
      </div>
    </div>
  </section>
</main>

<!-- 页脚 -->
<footer class="bg-gray-800 text-white py-12">
  <div class="container mx-auto px-4">
    <div class="flex flex-col md:flex-row justify-between items-center">
      <div class="mb-6 md:mb-0">
        <div class="flex items-center space-x-2 mb-2">
          <div class="w-8 h-8 bg-primary rounded flex items-center justify-center">
            <i class="fa fa-code text-white"></i>
          </div>
          <span class="font-bold">Javaweb项目</span>
        </div>
        <p class="text-gray-400 text-sm">© 2023 第一个Javaweb项目. 保留所有权利</p>
      </div>

      <div class="flex space-x-6">
        <a href="#" class="text-gray-400 hover:text-white transition-colors">
          <i class="fa fa-github text-xl"></i>
        </a>
        <a href="#" class="text-gray-400 hover:text-white transition-colors">
          <i class="fa fa-twitter text-xl"></i>
        </a>
        <a href="#" class="text-gray-400 hover:text-white transition-colors">
          <i class="fa fa-linkedin text-xl"></i>
        </a>
      </div>
    </div>
  </div>
</footer>

<script>
  // 页面滚动时导航栏效果
  window.addEventListener('scroll', function() {
    const header = document.querySelector('header');
    if (window.scrollY > 10) {
      header.classList.add('py-2');
      header.classList.remove('py-4');
      header.classList.add('shadow');
    } else {
      header.classList.add('py-4');
      header.classList.remove('py-2');
      header.classList.remove('shadow');
    }
  });
</script>
</body>
</html>

