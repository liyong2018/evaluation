package com.evaluate.security;

import com.evaluate.entity.User;
import com.evaluate.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 基于 Header 的简单用户认证过滤器
 */
@Component
public class UserHeaderFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. 获取 Header 中的用户名
        String username = request.getHeader("X-Current-User");

        // 2. 如果存在用户名且当前 Context 未认证
        if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 3. 加载用户信息
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 4. 构建认证对象
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. 设置到 Context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // 用户不存在或加载失败，忽略，继续后续流程
                logger.warn("Failed to load user by header username: " + username, e);
            }
        }

        chain.doFilter(request, response);
    }
}
