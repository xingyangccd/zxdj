package com.example.zxds.common.config.xss;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * XSS 防护过滤器
 * <p>
 * 作用：拦截所有请求，对请求参数、请求头等内容做 XSS 过滤，
 * 防止用户提交恶意的脚本代码（如 &lt;script&gt; 标签）造成安全问题。
 * <p>
 * 实现思路：
 * 1. @WebFilter(urlPatterns = "/*") 表示拦截所有路径的请求；
 * 2. 把原始的 HttpServletRequest 包装成自定义的 XssHttpServletRequestWrapper，
 *    在包装类里对参数做转义或清洗处理；
 * 3. 把包装后的 request 交给后续的过滤器链和 Controller 使用，
 *    这样 Controller 拿到的就是已经过滤过 XSS 的内容。
 * <p>
 * 注意：使用 @WebFilter 需要在启动类上添加 @ServletComponentScan 才能生效。
 *
 */
@WebFilter(urlPatterns = "/*")
public class XssFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(request);
        filterChain.doFilter(wrapper, servletResponse);
    }
}