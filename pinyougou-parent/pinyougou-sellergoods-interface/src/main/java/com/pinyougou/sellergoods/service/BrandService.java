package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;

/**
 * 品牌服务层接口
 * @author Administrator
 *
 */

public interface BrandService {

    /**
     * 返回全部列表
     * @return
     */
    public List<TbBrand> findAll();

    /**
     * 返回分页列表
     * @param pageNum
     * @param pagSize
     * @return
     */
    public PageResult findPage(int pageNum,int pagSize);


    /**
     * 增加
     * @param brand
     */
    public void add(TbBrand brand);

    /**
     * 修改
     * @param brand
     */
    public void update(TbBrand brand);

    /**
     * 根据id获取实体
     * @param id
     * @return
     */
    public TbBrand findOne(Long id);

    /**
     * 批量删除
     * @param ids
     */
    public void delete(Long[] ids);

    /**
     * 返回分页查询列表
     * @param pageNum 当前页
     * @param pagSize 每页显示条数
     * @return
     */
    public PageResult findPage(TbBrand brandint,int pageNum,int pagSize);


}
