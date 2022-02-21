package com.wjk.shopmall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.wjk.common.constant.AuthServerConstant;
import com.wjk.common.exception.BizCodeEnume;
import com.wjk.common.utils.R;
import com.wjk.common.vo.MemberRespVo;
import com.wjk.shopmall.auth.feign.MemberFeignService;
import com.wjk.shopmall.auth.feign.ThirdPartFeignService;
import com.wjk.shopmall.auth.vo.UserLoginVo;
import com.wjk.shopmall.auth.vo.UserRegistVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     *   发送一个请求直接跳转到一个页面
     *   SpringMVC viewcontroler ：将请求和页面映射过来
     * */

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone){
        // TODO 1.接口防刷

        String redisCode = redisTemplate.opsForValue().get("AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone");
        if(!StringUtils.isEmpty(redisCode)){
            long l = Long.parseLong(redisCode.split("_")[1]);
            if(System.currentTimeMillis() - l < 60000){
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMessage());
            }
        }

        // 2.验证码再次校验 redis key-phone value code
        int temp = (int) ((Math.random() * 9+1) * 100000);
        String code = String.valueOf(temp);
        String codeRedis = code + "_" + System.currentTimeMillis();
        // redis缓存验证码，防止同一个phone在60s内再次发送验证码
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone,codeRedis,10, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone,code);
        return R.ok();
    }

    // TODO 重定向携带数据，利用session原理。将数据放到session中。
    //  只要跳转到下一个页面去取出这个数据以后，session里面的数据就会删掉
    //  分布式下的Sesson出现的问题
    // RedirectAttributes redirectAttributes 模拟重定向携带数据
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes){
        if(result.hasErrors()){
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors",errors);
            // Request method 'POST' not supported
            // 用户注册 /regist【post】 --》 转发/reg.html（路径映射默认都是Get方式访问）

            // 校验出错，转发到注册页
            return "redirect:http://auth.shopmall.com/reg.html";
        }
        // 1.校验验证码
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if(!StringUtils.isEmpty(s)){
            if(code.equals(s.split("_")[0])){
                // 删除验证码; 令牌机制
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                // 验证码通过  调用远程服务实现注册服务
                R r = memberFeignService.regist(vo);
                if(r.getCode() == 0){
                    // 注册成功回到登录页
                    return "redirect:http://auth.shopmall.com/login.html";
                }else{
                    Map<String,String> errors = new HashMap<>();
                    errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.shopmall.com/reg.html";
                }
            }else{
                Map<String,String> errors = new HashMap<>();
                errors.put("code","验证码错误");
                redirectAttributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.shopmall.com/reg.html";
            }
        }else{
            Map<String,String> errors = new HashMap<>();
            errors.put("code","请发送验证码");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.shopmall.com/reg.html";
        }
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute == null){
            // 没登录
            return "login";
        }else{
            return "redirect:http://shopmall.com";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session){
        // 远程调用Member服务 进行登录
        R login = memberFeignService.login(vo);
        if(login.getCode() == 0){
            MemberRespVo data = login.getData("data", new TypeReference<MemberRespVo>() {
            });
            // 成功放到session中
            session.setAttribute(AuthServerConstant.LOGIN_USER,data);
            return "redirect:http://shopmall.com";
        }else{
            Map<String,String> errors = new HashMap<>();
            errors.put("msg",login.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.shopmall.com/login.html";
        }
    }
}
