package com.example.projectmanagement.filter;

import com.example.projectmanagement.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 拦截请求并验证JWT令牌
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
        // 获取请求头中的JWT令牌
        String jwt = parseJwt(request);
        
        // 如果存在JWT令牌并且当前没有认证信息
        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 从JWT令牌中提取用户名
            String username = jwtUtils.getUsernameFromJwtToken(jwt);
            
            if (username != null) {
                // 加载用户详情
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 验证JWT令牌是否有效
                if (jwtUtils.validateToken(jwt, userDetails)) {
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    
                    // 设置认证详情
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置安全上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从请求头中解析JWT令牌
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        
        return null;
    }
}
