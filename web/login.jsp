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
    <title>登录 - Javaweb项目</title>
    <!-- 引入Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <!-- 引入Font Awesome -->
    <link href="https://cdn.jsdelivr.net/npm/font-awesome@4.7.0/css/font-awesome.min.css" rel="stylesheet">

    <script>
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
            .animate-float {
                animation: float 6s ease-in-out infinite;
            }
            .animate-fade-in {
                animation: fadeIn 0.6s ease-out forwards;
            }
            .form-input-focus {
                @apply focus:border-primary focus:ring-2 focus:ring-primary/20 focus:outline-none;
            }
            .error-input {
                @apply border-danger focus:border-danger focus:ring-danger/20;
            }
        }

        @keyframes float {
            0% { transform: translateY(0px); }
            50% { transform: translateY(-15px); }
            100% { transform: translateY(0px); }
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
        }
    </style>
</head>
<body class="font-inter bg-gray-50 min-h-screen flex items-center justify-center p-4">
<div class="w-full max-w-5xl">
    <div class="bg-white rounded-2xl shadow-xl overflow-hidden flex flex-col md:flex-row">
        <!-- 左侧装饰区域 -->
        <div class="bg-primary md:w-1/2 p-8 md:p-12 text-white relative overflow-hidden">
            <div class="absolute inset-0 opacity-10">
                <div class="absolute w-64 h-64 rounded-full bg-white top-1/4 -left-32"></div>
                <div class="absolute w-96 h-96 rounded-full bg-white bottom-1/4 -right-48"></div>
            </div>

            <div class="relative z-10 h-full flex flex-col justify-center">
                <div class="mb-10 animate-float">
                    <div class="w-16 h-16 bg-white/20 rounded-lg flex items-center justify-center mb-4">
                        <i class="fa fa-code text-white text-2xl"></i>
                    </div>
                    <h2 class="text-3xl font-bold">Javaweb项目</h2>
                    <p class="mt-2 opacity-90">探索编程的无限可能</p>
                </div>

                <div class="space-y-6">
                    <div class="flex items-start space-x-4 animate-fade-in" style="animation-delay: 0.2s">
                        <div class="mt-1 bg-white/20 p-2 rounded">
                            <i class="fa fa-shield text-white"></i>
                        </div>
                        <div>
                            <h3 class="text-xl font-semibold">安全保障</h3>
                            <p class="opacity-80 mt-1">多重验证机制，保障您的账号安全</p>
                        </div>
                    </div>

                    <div class="flex items-start space-x-4 animate-fade-in" style="animation-delay: 0.4s">
                        <div class="mt-1 bg-white/20 p-2 rounded">
                            <i class="fa fa-bolt text-white"></i>
                        </div>
                        <div>
                            <h3 class="text-xl font-semibold">高效体验</h3>
                            <p class="opacity-80 mt-1">优化的性能，带来流畅的操作感受</p>
                        </div>
                    </div>

                    <div class="flex items-start space-x-4 animate-fade-in" style="animation-delay: 0.6s">
                        <div class="mt-1 bg-white/20 p-2 rounded">
                            <i class="fa fa-lock text-white"></i>
                        </div>
                        <div>
                            <h3 class="text-xl font-semibold">数据加密</h3>
                            <p class="opacity-80 mt-1">所有数据传输均经过加密处理</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 右侧登录表单区域 -->
        <div class="md:w-1/2 p-8 md:p-12 animate-fade-in">
            <div class="max-w-md mx-auto w-full">
                <h2 class="text-2xl md:text-3xl font-bold text-gray-800 mb-6">欢迎回来</h2>
                <p class="text-gray-600 mb-8">请输入您的账号信息登录系统</p>

                <!-- 后端错误提示（如果有） -->
                <%
                    String error = request.getParameter("error");
                    if (error != null && error.equals("1")) {
                %>
                <div class="mb-5 p-3 bg-red-50 border border-red-200 text-danger rounded-lg text-sm flex items-center">
                    <i class="fa fa-exclamation-circle mr-2"></i>
                    <span>用户名或密码错误，请重新输入</span>
                </div>
                <% } %>

                <form action="Login" method="post" class="space-y-5" onsubmit="return validateForm()">
                    <!-- 用户名输入 -->
                    <div>
                        <label for="name" class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
                        <div class="relative">
                            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <i class="fa fa-user text-gray-400"></i>
                            </div>
                            <input type="text" id="name" name="name"
                                   class="w-full pl-10 py-3 border border-gray-300 rounded-lg transition-all duration-200 form-input-focus"
                                   placeholder="请输入用户名">
                        </div>
                        <p id="nameError" class="mt-1 text-danger text-sm hidden"><i class="fa fa-times-circle mr-1"></i>用户名不能为空</p>
                    </div>

                    <!-- 密码输入 -->
                    <div>
                        <label for="pwd" class="block text-sm font-medium text-gray-700 mb-1">密码</label>
                        <div class="relative">
                            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <i class="fa fa-lock text-gray-400"></i>
                            </div>
                            <input type="password" id="pwd" name="pwd"
                                   class="w-full pl-10 py-3 border border-gray-300 rounded-lg transition-all duration-200 form-input-focus"
                                   placeholder="请输入密码">
                            <button type="button" id="togglePwd" class="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600 transition-colors">
                                <i class="fa fa-eye-slash"></i>
                            </button>
                        </div>
                        <p id="pwdError" class="mt-1 text-danger text-sm hidden"><i class="fa fa-times-circle mr-1"></i>密码不能为空</p>
                    </div>

                    <!-- 记住密码和忘记密码 -->
                    <div class="flex items-center justify-between">
                        <div class="flex items-center">
                            <input id="remember" name="remember" type="checkbox"
                                   class="h-4 w-4 text-primary focus:ring-primary border-gray-300 rounded">
                            <label for="remember" class="ml-2 block text-sm text-gray-700">
                                记住我
                            </label>
                        </div>
                        <a href="#" class="text-sm text-primary hover:text-primary/80 transition-colors">
                            忘记密码?
                        </a>
                    </div>

                    <!-- 登录按钮 -->
                    <div>
                        <button type="submit" name="denglu"
                                class="w-full py-3 px-4 bg-primary hover:bg-primary/90 text-white font-medium rounded-lg transition-all duration-200 shadow-md hover:shadow-lg transform hover:-translate-y-0.5">
                            登录
                        </button>
                    </div>

                    <!-- 重置按钮 -->
                    <div>
                        <button type="reset" onclick="resetForm()"
                                class="w-full py-3 px-4 bg-gray-100 hover:bg-gray-200 text-gray-800 font-medium rounded-lg transition-all duration-200">
                            重置
                        </button>
                    </div>
                </form>

                <!-- 注册链接 -->
                <div class="mt-8 text-center">
                    <p class="text-gray-600">还没有账号?
                    <form action="register.jsp" class="inline">
                        <button type="submit" class="text-primary font-medium hover:text-primary/80 transition-colors ml-1">
                            新用户注册
                        </button>
                    </form>
                    </p>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    // 密码显示/隐藏切换
    const togglePwd = document.getElementById('togglePwd');
    const pwdInput = document.getElementById('pwd');

    togglePwd.addEventListener('click', function() {
        const type = pwdInput.getAttribute('type') === 'password' ? 'text' : 'password';
        pwdInput.setAttribute('type', type);
        this.querySelector('i').classList.toggle('fa-eye');
        this.querySelector('i').classList.toggle('fa-eye-slash');
    });

    // 表单输入动画效果
    const inputs = document.querySelectorAll('input');
    inputs.forEach(input => {
        input.addEventListener('focus', function() {
            this.parentElement.classList.add('scale-[1.01]');
        });
        input.addEventListener('blur', function() {
            this.parentElement.classList.remove('scale-[1.01]');
            // 失去焦点时验证
            if (this.id === 'name') validateName();
            if (this.id === 'pwd') validatePwd();
        });
    });

    // 验证用户名
    function validateName() {
        const name = document.getElementById('name').value.trim();
        const errorEl = document.getElementById('nameError');
        if (name === '') {
            errorEl.classList.remove('hidden');
            document.getElementById('name').classList.add('error-input');
            return false;
        } else {
            errorEl.classList.add('hidden');
            document.getElementById('name').classList.remove('error-input');
            return true;
        }
    }

    // 验证密码
    function validatePwd() {
        const pwd = document.getElementById('pwd').value.trim();
        const errorEl = document.getElementById('pwdError');
        if (pwd === '') {
            errorEl.classList.remove('hidden');
            document.getElementById('pwd').classList.add('error-input');
            return false;
        } else {
            errorEl.classList.add('hidden');
            document.getElementById('pwd').classList.remove('error-input');
            return true;
        }
    }

    // 表单提交验证
    function validateForm() {
        const isNameValid = validateName();
        const isPwdValid = validatePwd();
        return isNameValid && isPwdValid; // 都验证通过才提交
    }

    // 重置表单时清除错误状态
    function resetForm() {
        document.getElementById('nameError').classList.add('hidden');
        document.getElementById('pwdError').classList.add('hidden');
        document.getElementById('name').classList.remove('error-input');
        document.getElementById('pwd').classList.remove('error-input');
    }
</script>
</body>
</html>
