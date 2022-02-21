package com.wjk.shopmall.auth.feign;

import com.wjk.common.utils.R;
import com.wjk.shopmall.auth.vo.SocialUser;
import com.wjk.shopmall.auth.vo.UserLoginVo;
import com.wjk.shopmall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("shopmall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    public R regist(@RequestBody UserRegistVo vo);

    @PostMapping("/member/member/login")
    public R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    public R oauthlogin(@RequestBody SocialUser socialUser) throws Exception;
}
