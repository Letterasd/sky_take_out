package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 动态条件查询sql
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据id字段更新
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumById(ShoppingCart shoppingCart);

    /**
     * 插入shoppingCart表数据
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) \n" +
            "VALUES (#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 删除全部userId下的购物车数据
     */
    @Delete("delete from shopping_cart where user_id=#{userId}")
    void deleByUserId(Long userId);

    /**
     * 根据购物车商品主键删除数据
     * @param id
     */
    @Delete("delete from shopping_cart where id=#{id}")
    void deleteByCartId(Long id);

    /**
     * 批量插入数据
     * @param cartList
     */
    void insertBatch(List<ShoppingCart> cartList);
}
