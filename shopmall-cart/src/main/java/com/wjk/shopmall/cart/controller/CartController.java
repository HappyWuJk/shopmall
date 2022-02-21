package com.wjk.shopmall.cart.controller;

import com.wjk.shopmall.cart.interceptor.CartInterceptor;
import com.wjk.shopmall.cart.service.CartService;
import com.wjk.shopmall.cart.vo.Cart;
import com.wjk.shopmall.cart.vo.CartItem;
import com.wjk.shopmall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @ResponseBody
    @GetMapping("/currentUserCartItems")
    public List<CartItem> currentUserCartItems(){
        return cartService.getUserCartItems();
    }


    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.shopmall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num){
        cartService.changeItemCount(skuId,num);
        return "redirect:http://cart.shopmall.com/cart.html";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("check") Integer check){
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.shopmall.com/cart.html";
    }

    /**
     *   浏览器有一个cookie：user-key：标识用户身份，一个月后过期
     *   如果第一次使用jd的购物车功能，都会给一个临时的用户身份
     *   浏览器以后保存，每次访问都会带上这个cookie
     *
     *   登录：session有
     *   没登录：按照cookie里面带user-key来做
     *   第一次：如果没有临时用户，帮忙创建一个临时用户
     * */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    // 添加商品到购物车
    /**
     *    redirectAttributes.addFlashAttribute(); 将数据放在session里面可以在页面取出，但是只能取一次
     *    redirectAttributes.addAttribute("skuId",skuId); 将数据放在url后面
     * */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId,num);
        redirectAttributes.addAttribute("skuId",skuId);
        return "redirect:http://cart.shopmall.com/addToCartSuccess.html";
    }

    // 跳转到成功页
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccesPage(@RequestParam("skuId") Long skuId,Model model){
        // 重定向到成功页面。再次查询购物车数据即可
        CartItem item = cartService.getCartItem(skuId);
        model.addAttribute("item",item);
        return "success";
    }
}
