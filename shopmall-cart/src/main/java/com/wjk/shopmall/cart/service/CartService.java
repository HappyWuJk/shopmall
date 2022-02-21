package com.wjk.shopmall.cart.service;

import com.wjk.shopmall.cart.vo.Cart;
import com.wjk.shopmall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    // 获取整个购物车
    Cart getCart() throws ExecutionException, InterruptedException;

    // 清空购物车数据
    void clearCart(String cartKey);

    // 勾选购物项
    void checkItem(Long skuId, Integer check);

    // 修改购物项数量
    void changeItemCount(Long skuId, Integer num);

    // 删除购物项
    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();

}
