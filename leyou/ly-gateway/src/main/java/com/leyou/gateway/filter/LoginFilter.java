package com.leyou.gateway.filter;

import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.properties.FilterProperties;
import com.leyou.gateway.properties.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
@Slf4j
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProp;

    @Autowired
    private FilterProperties filterProp;


    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 5;
    }

    @Override
    public boolean shouldFilter() {
        // 获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        // 获取request
        HttpServletRequest req = ctx.getRequest();
        // 获取路径
        String requestURI = req.getRequestURI();
        // 判断白名单
        return !isAllowPath(requestURI);
    }

    private boolean isAllowPath(String requestURI) {
        // 定义一个标记
        boolean flag = false;
        // 遍历允许访问的路径
        for (String path : this.filterProp.getAllowPaths()) {
            // 然后判断是否是符合
            if(requestURI.startsWith(path)){
                flag = true;
                break;
            }
        }
        return flag;
    }

    @Override
    public Object run() throws ZuulException {
        // 获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        // 获取request
        HttpServletRequest request = ctx.getRequest();
        // 获取token
        String token = CookieUtils.getCookieValue(request, jwtProp.getCookieName());
        // 校验
        try {
            // 校验通过什么都不做，即放行
            JwtUtils.getUserInfo(jwtProp.getPublicKey(), token);
        } catch (Exception e) {
            // 解析token出现异常，未登录，拦截
            ctx.setSendZuulResponse(false);
            //返回状态码
            ctx.setResponseStatusCode(403);
            log.error("非法访问，未登录，地址：{}", request.getRemoteHost(), e );
        }
        return null;
    }
}