package servlet;

import entity.User;
import dao.UserDao;
import dao.UserDaoImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        super.init();
        userDao = new UserDaoImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("/register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String name = trimParam(request.getParameter("name"));
        String pwd = trimParam(request.getParameter("pwd"));
        String sex = trimParam(request.getParameter("sex"));
        String home = trimParam(request.getParameter("home"));
        String info = trimParam(request.getParameter("info"));
        // 获取服务条款勾选状态（勾选时值为"on"，未勾选为null）
        String terms = request.getParameter("terms");

        // 1. 服务条款兜底校验（后端必须做，防止前端校验被绕过）
        if (terms == null || !"on".equals(terms)) {
            request.setAttribute("errorMsg", "请阅读并同意服务条款和隐私政策");
            forwardToRegister(request, response);
            return;
        }

        // 2. 其他校验（用户名、密码非空等，保持原有逻辑）
        if (isNullOrEmpty(name)) {
            request.setAttribute("errorMsg", "用户名不能为空！");
            forwardToRegister(request, response);
            return;
        }
        if (isNullOrEmpty(pwd)) {
            request.setAttribute("errorMsg", "密码不能为空！");
            forwardToRegister(request, response);
            return;
        }
        // ... 其他原有校验逻辑（如用户名长度、密码强度等）

        try {
            // 3. 用户名唯一性校验、组装用户信息、执行注册（保持原有逻辑）
            if (userDao.login(name,pwd)) {
                request.setAttribute("errorMsg", "用户名已存在，请更换其他用户名！");
                forwardToRegister(request, response);
                return;
            }

            User user = new User();
            user.setName(name);
            user.setPwd(pwd);
            user.setSex(sex);
            user.setHome(home);
            user.setInfo(info);

            if (userDao.register(user)) {
                request.setAttribute("successMsg", "注册成功！请使用新账号登录");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMsg", "注册失败，请稍后重试！");
                forwardToRegister(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMsg", "系统异常，请联系管理员！");
            forwardToRegister(request, response);
        }
    }

    // 工具方法（保持原有逻辑）
    private String trimParam(String param) {
        return param == null ? null : param.trim();
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private void forwardToRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("name", request.getParameter("name"));
        request.setAttribute("sex", request.getParameter("sex"));
        request.setAttribute("home", request.getParameter("home"));
        request.setAttribute("info", request.getParameter("info"));
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }
}