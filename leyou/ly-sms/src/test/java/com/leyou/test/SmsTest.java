package com.leyou.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsTest {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void testSend() {
        Map<String, String> msg = new HashMap<>();
        msg.put("phone","17854200056");
        msg.put("code", "1234");
        amqpTemplate.convertAndSend("ly.sms.exchange", "sms.verify.code", msg);

        log.info("发送");

        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}