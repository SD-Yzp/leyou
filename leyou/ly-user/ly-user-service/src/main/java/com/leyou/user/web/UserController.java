package com.leyou.user.web;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 校验数据
     * @param data
     * @param type
     * @return
     */
    @RequestMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data")String data,@PathVariable("type")Integer type){
        return ResponseEntity.ok(userService.checkData(data,type));
    }

    /**
     * 发送短信
     * @param phone
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone) {
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 注册功能
     * @param user
     * @param code
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code")String code){
        Boolean boo = userService.register(user,code);
        if(boo == null || !boo){
            // 注册失败
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("query")
    public ResponseEntity<User> queryUser(
            @RequestParam("username")String username,
            @RequestParam("password")String password
    ){
        User user = userService.queryUser(username,password);
        if(user == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(user);
    }

}