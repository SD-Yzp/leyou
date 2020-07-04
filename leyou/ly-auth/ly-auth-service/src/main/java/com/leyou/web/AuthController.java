package com.leyou.web;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.properties.JwtProperties;
import com.leyou.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties prop;

    /**
     * 登录授权
     *
     * @param username
     * @param password
     * @return
     */
    @PostMapping("accredit")
    public ResponseEntity<Void> authentication(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response) {
        // 登录校验
        String token = authService.authentication(username, password);
        if (StringUtils.isBlank(token)) {
            log.info("用户授权失败");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // 将token写入cookie,并指定httpOnly为true，防止通过JS获取和修改
//        CookieUtils.setCookie(request, response, prop.getCookieName(),
//                token, prop.getCookieMaxAge(), null, true);
        CookieUtils.setCookie(request, response, prop.getCookieName(), token, prop.getCookieMaxAge(), true);
        return ResponseEntity.ok().build();
    }

    /**
     * 验证用户登录状态
     * @param token
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyUser(@CookieValue("LY_TOKEN")String token, HttpServletRequest request, HttpServletResponse response){
        try {
            // 从token中解析token信息
            UserInfo userInfo = JwtUtils.getUserInfo(this.prop.getPublicKey(), token);
            // 解析成功要重新刷生成token
            token = JwtUtils.generateToken(userInfo, this.prop.getPrivateKey(), this.prop.getExpire());
            // 更新cookie中的token
            CookieUtils.setCookie(request, response, this.prop.getCookieName(), token, this.prop.getCookieMaxAge());

            log.info("[购物车测试]");


            // 解析成功返回用户信息
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        // 出现异常则，响应500
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}