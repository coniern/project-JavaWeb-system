<%--
  Created by IntelliJ IDEA.
  User: 张雅
  Date: 2025/9/9
  Time: 15:49
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
    <title>注册 - Javaweb项目</title>
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
                        danger: '#ef4444'
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
            .form-input-focus {
                @apply focus:border-primary focus:ring-2 focus:ring-primary/20 focus:outline-none;
            }
            .form-float-label input:focus ~ label,
            .form-float-label input:not(:placeholder-shown) ~ label {
                @apply transform -translate-y-6 scale-75 text-primary;
            }
            .error-input {
                @apply border-danger focus:border-danger focus:ring-danger/20;
            }
            .error-text {
                @apply text-danger text-sm mt-1 flex items-center;
            }
            .shake {
                animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both;
            }
        }

        @keyframes shake {
            10%, 90% { transform: translateX(-1px); }
            20%, 80% { transform: translateX(2px); }
            30%, 50%, 70% { transform: translateX(-3px); }
            40%, 60% { transform: translateX(3px); }
        }
    </style>
</head>
<body class="font-inter bg-gray-50 min-h-screen">
<!-- 导航栏 -->
<header class="bg-white shadow-sm">
    <div class="container mx-auto px-4 py-4 flex justify-between items-center">
        <div class="flex items-center space-x-2">
            <div class="w-10 h-10 bg-primary rounded-lg flex items-center justify-center">
                <i class="fa fa-code text-white text-xl"></i>
            </div>
            <h1 class="text-xl font-bold text-gray-800">Javaweb项目</h1>
        </div>

        <div class="flex items-center space-x-4">
            <a href="<%=basePath%>" class="text-gray-600 hover:text-primary transition-colors">
                <i class="fa fa-home mr-1"></i>首页
            </a>
            <a href="login.jsp" class="text-gray-600 hover:text-primary transition-colors">
                <i class="fa fa-sign-in mr-1"></i>登录
            </a>
        </div>
    </div>
</header>


<!-- 主要内容区 -->
<main class="container mx-auto px-4 py-10 md:py-16">
    <div class="max-w-2xl mx-auto">
        <!-- 页面标题 -->
        <div class="text-center mb-10">
            <h2 class="text-2xl md:text-3xl font-bold text-gray-800 mb-2">创建新账号</h2>
            <p class="text-gray-600">填写以下信息完成注册，加入我们的平台</p>
        </div>

        <!-- 后端错误信息显示 -->
        <% if (request.getAttribute("errorMsg") != null) { %>
        <div class="bg-red-50 border border-red-200 text-danger rounded-lg p-4 mb-6 animate-fade-in">
            <i class="fa fa-exclamation-circle mr-2"></i>
            <%= request.getAttribute("errorMsg") %>
        </div>
        <% } %>

        <!-- 注册表单 -->
        <form action="Register" method="post" class="bg-white rounded-2xl shadow-lg p-6 md:p-8 space-y-6 animate-fade-in" onsubmit="return validateForm()">
            <!-- 用户名输入 -->
            <div class="form-float-label relative">
                <input type="text" name="name" id="name"
                       class="w-full py-3 px-4 border border-gray-300 rounded-lg transition-all duration-200 form-input-focus <%= request.getAttribute("nameError") != null ? "error-input" : "" %>"
                       placeholder=" " value="<%= request.getAttribute("name") != null ? request.getAttribute("name") : "" %>">
                <label for="name" class="absolute left-4 top-3 text-gray-500 transition-all duration-200 pointer-events-none">
                    用户名
                </label>
                <% if (request.getAttribute("nameError") != null) { %>
                <p class="error-text"><i class="fa fa-times-circle mr-1"></i><%= request.getAttribute("nameError") %></p>
                <% } %>
            </div>

            <!-- 密码输入 -->
            <div class="form-float-label relative">
                <input type="password" name="pwd" id="pwd"
                       class="w-full py-3 px-4 border border-gray-300 rounded-lg transition-all duration-200 form-input-focus <%= request.getAttribute("pwdError") != null ? "error-input" : "" %>"
                       placeholder=" ">
                <label for="pwd" class="absolute left-4 top-3 text-gray-500 transition-all duration-200 pointer-events-none">
                    密码
                </label>
                <button type="button" id="togglePwd" class="absolute inset-y-0 right-0 pr-4 flex items-center text-gray-400 hover:text-gray-600 transition-colors">
                    <i class="fa fa-eye-slash"></i>
                </button>
                <% if (request.getAttribute("pwdError") != null) { %>
                <p class="error-text"><i class="fa fa-times-circle mr-1"></i><%= request.getAttribute("pwdError") %></p>
                <% } %>
            </div>

            <!-- 性别选择 -->
            <div>
                <label class="block text-gray-700 font-medium mb-3">选择性别</label>
                <div class="flex space-x-6">
                    <label class="inline-flex items-center cursor-pointer">
                        <input type="radio" name="sex" value="男" <%= request.getAttribute("sex") != null && request.getAttribute("sex").equals("男") ? "checked" : "checked" %>
                               class="form-radio h-5 w-5 text-primary focus:ring-primary border-gray-300">
                        <span class="ml-2 text-gray-700">男</span>
                    </label>
                    <label class="inline-flex items-center cursor-pointer">
                        <input type="radio" name="sex" value="女" <%= request.getAttribute("sex") != null && request.getAttribute("sex").equals("女") ? "checked" : "" %>
                               class="form-radio h-5 w-5 text-primary focus:ring-primary border-gray-300">
                        <span class="ml-2 text-gray-700">女</span>
                    </label>
                </div>
            </div>

            <!-- 家乡选择 -->
            <div>
                <label for="home" class="block text-gray-700 font-medium mb-2">选择家乡</label>
                <div class="relative">
                    <select name="home" id="home"
                            class="w-full py-3 px-4 border border-gray-300 rounded-lg transition-all duration-200 form-input-focus appearance-none bg-white">
                        <option value="上海" <%= request.getAttribute("home") != null && request.getAttribute("home").equals("上海") ? "selected" : "" %>>上海</option>
                        <option value="北京" <%= request.getAttribute("home") == null || request.getAttribute("home").equals("北京") ? "selected" : "" %>>北京</option>
                        <option value="纽约" <%= request.getAttribute("home") != null && request.getAttribute("home").equals("纽约") ? "selected" : "" %>>纽约</option>
                        <option value="广州" <%= request.getAttribute("home") != null && request.getAttribute("home").equals("广州") ? "selected" : "" %>>广州</option>
                        <option value="深圳" <%= request.getAttribute("home") != null && request.getAttribute("home").equals("深圳") ? "selected" : "" %>>深圳</option>
                    </select>
                    <div class="absolute inset-y-0 right-0 pr-4 flex items-center pointer-events-none text-gray-500">
                        <i class="fa fa-chevron-down"></i>
                    </div>
                </div>
            </div>

            <!-- 个人信息 -->
            <div>
                <label for="info" class="block text-gray-700 font-medium mb-2">个人信息</label>
                <textarea name="info" id="info" rows="4" cols="30"
                          class="w-full py-3 px-4 border border-gray-300 rounded-lg transition-all duration-200 form-input-focus resize-none"
                          placeholder="请简单介绍一下自己"><%= request.getAttribute("info") != null ? request.getAttribute("info") : "" %></textarea>
                <p class="text-xs text-gray-500 mt-1">请简要介绍自己，帮助他人更好地了解你</p>
            </div>

            <!-- 注册协议 -->
            <div class="flex items-start" id="termsContainer">
                <div class="flex items-center h-5">
                    <input id="terms" name="terms" type="checkbox"
                           class="h-4 w-4 text-primary focus:ring-primary border-gray-300 rounded">
                </div>
                <div class="ml-3 text-sm">
                    <label for="terms" class="text-gray-600">我已阅读并同意<a href="#" class="text-primary hover:underline">服务条款</a>和<a href="#" class="text-primary hover:underline">隐私政策</a></label>
                </div>
            </div>
            <!-- 协议验证错误提示 - 默认隐藏，仅在提交时未勾选才显示 -->
            <p id="termsError" class="error-text hidden"><i class="fa fa-times-circle mr-1"></i>请阅读并同意服务条款和隐私政策</p>

            <!-- 按钮区域 -->
            <div class="flex flex-col sm:flex-row gap-3 pt-2">
                <input type="reset" value="重置" onclick="resetForm()"
                       class="flex-1 py-3 px-4 bg-gray-100 hover:bg-gray-200 text-gray-800 font-medium rounded-lg transition-all duration-200">
                <input type="submit" value="注册"
                       class="flex-1 py-3 px-4 bg-primary hover:bg-primary/90 text-white font-medium rounded-lg transition-all duration-200 shadow-md hover:shadow-lg transform hover:-translate-y-0.5">
            </div>
        </form>

        <!-- 已有账号提示 -->
        <div class="text-center mt-6">
            <p class="text-gray-600">已有账号?
                <a href="login.jsp" class="text-primary font-medium hover:text-primary/80 transition-colors ml-1">
                    立即登录
                </a>
            </p>
        </div>
    </div>
</main>

<!-- 页脚 -->
<footer class="bg-gray-800 text-white py-8 mt-16">
    <div class="container mx-auto px-4">
        <div class="text-center">
            <p class="text-gray-400 text-sm">© 2023 Javaweb项目. 保留所有权利</p>
        </div>
    </div>
</footer>

<script>
    // 密码显示/隐藏切换
    const togglePwd = document.getElementById('togglePwd');
    const pwdInput = document.getElementById('pwd');

    togglePwd.addEventListener('click', function() {
        // 切换密码输入类型
        const type = pwdInput.getAttribute('type') === 'password' ? 'text' : 'password';
        pwdInput.setAttribute('type', type);

        // 切换图标
        this.querySelector('i').classList.toggle('fa-eye');
        this.querySelector('i').classList.toggle('fa-eye-slash');
    });

    // 表单验证函数 - 仅在提交时检查是否同意条款
    function validateForm() {
        let isValid = true;
        const termsCheckbox = document.getElementById('terms');
        const termsError = document.getElementById('termsError');
        const termsContainer = document.getElementById('termsContainer');

        // 验证服务条款是否同意（仅在提交时检查）
        if (!termsCheckbox.checked) {
            termsError.classList.remove('hidden');
            termsContainer.classList.add('shake'); // 添加抖动动画
            isValid = false;

            // 移除抖动类，以便下次可以再次触发动画
            setTimeout(() => {
                termsContainer.classList.remove('shake');
            }, 500);
        } else {
            termsError.classList.add('hidden');
        }

        return isValid;
    }

    // 重置表单时清除错误提示
    function resetForm() {
        document.getElementById('termsError').classList.add('hidden');
    }

    // 勾选条款时自动隐藏错误提示
    document.getElementById('terms').addEventListener('change', function() {
        if (this.checked) {
            document.getElementById('termsError').classList.add('hidden');
        }
    });

    // 表单元素淡入动画
    document.addEventListener('DOMContentLoaded', function() {
        const formElements = document.querySelectorAll('form > div');
        formElements.forEach((el, index) => {
            el.style.opacity = '0';
            el.style.transform = 'translateY(10px)';
            el.style.transition = 'opacity 0.3s ease, transform 0.3s ease';

            setTimeout(() => {
                el.style.opacity = '1';
                el.style.transform = 'translateY(0)';
            }, 100 + (index * 100));
        });
    });
</script>
</body>
</html>
