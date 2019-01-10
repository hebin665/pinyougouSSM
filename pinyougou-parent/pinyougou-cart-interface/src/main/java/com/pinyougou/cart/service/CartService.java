package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

public interface CartService {

    /* * 购物车服务接口
     * @author Administrator
     *
     */


    public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);
}
