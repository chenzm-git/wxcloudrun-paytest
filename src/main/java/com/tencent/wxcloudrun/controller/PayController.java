package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.service.impl.PayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenzm
 * @version V1.0
 * @since 2023-05-05$
 **/
@RestController
@Slf4j
public class PayController {

    @Autowired
    private PayService pay;

    @PostMapping(value = "/api/pay/pay")
    public String pay(@RequestBody String code){
        log.info("pay 输入 : " + code);
        String output = pay.pay(code);
        log.info("pay 输出 : " + output);
        return output;
    }

    @PostMapping(value = "/api/pay/notify")
    public String notify(@RequestBody String input){
        log.info("notify 输入 : " + input);
        return "ok";
    }

}
