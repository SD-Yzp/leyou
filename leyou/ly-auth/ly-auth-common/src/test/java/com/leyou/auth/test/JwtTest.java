package com.leyou.auth.test;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "C:\\leyou_temp\\rsa\\rsa.pub";

    private static final String priKeyPath = "C:\\leyou_temp\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU4NTYyODkwMn0.U1ZI-bazrRw5XkZui8WIVPzf8HYg2S6v1snhVb3Mgsn51fDTfNlS0zBAWXdi5R0jjsHL9BGnNnD-kD4XUBj2Y3Qp04wfwBB6yFvGanJ6wvY8UURWzG1GzS4YrUpVpoFnR9G48ejdJ8TI1exnBye8dKoTRl6pLGdi3pNR1niEvOk";

        // 解析token
        UserInfo user = JwtUtils.getUserInfo(publicKey, token);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}