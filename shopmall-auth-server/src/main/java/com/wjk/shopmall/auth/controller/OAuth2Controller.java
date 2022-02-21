package com.wjk.shopmall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wjk.common.utils.HttpUtils;
import com.wjk.common.utils.R;
import com.wjk.common.vo.MemberRespVo;
import com.wjk.shopmall.auth.feign.MemberFeignService;
import com.wjk.shopmall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 *  处理社交登录请求
 * */
@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    /**
     *   社交登陆成功回调
     * */
    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session, HttpServletResponse servletResponse) throws Exception {
        // 1.根据code换取accessToken
        Map<String,String> map = new HashMap<>();
        map.put("client_id","4177176202");
        map.put("client_secret","cc270709d3c498a305e406579273ea13");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://auth.shopmall.com/oauth2.0/weibo/success");
        map.put("code",code);
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), map, new HashMap<>());

        // 处理
        if(response.getStatusLine().getStatusCode() == 200){
            // 获取到了 accessToken
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            // 知道当前是哪个社交用户
            // 1）当前用户如果是第一次进网站，自动注册进来（为当前社交用户生成一个会员信息账号，以后这个社交帐号就对应指定的会员）
            // 登陆或者注册这个社交用户
            R oauthlogin = memberFeignService.oauthlogin(socialUser);
            if (oauthlogin.getCode() == 0){
                MemberRespVo data = oauthlogin.getData("data", new TypeReference<MemberRespVo>() {
                });
                System.out.println("登陆成功，用户信息"+data);
                log.info("登陆成功，用户：{}",data.toString());
                // 第一次使用session，命令浏览器保存JSESSION这个Cookie
                // 以后访问网站就会带上这个Cookie
                // 子域之间，shopmall.com  auth.shopmall.com  order.shopmall.com
                // 扩大Session作用域（指定域名为父域名）
                // new Cookie("JSESSIONID","dddd").setDomain("");
                // servletResponse.addCookie();
                // TODO 默认发的令牌 key为session，作用域为当前域（解决子域session共享问题）
                // TODO 使用JSON的序列化方式来序列化对象数据到redis中（默认JDK序列化）
                session.setAttribute("loginUser",data);
                // 2.登陆成功就跳回首页
                return "redirect:http://shopmall.com";
            }else{
                return "redirect:http://auth.shopmall.com/login.html";
            }

        }else{
            return "redirect:http://auth.shopmall.com/login.html";
        }
    }
}
