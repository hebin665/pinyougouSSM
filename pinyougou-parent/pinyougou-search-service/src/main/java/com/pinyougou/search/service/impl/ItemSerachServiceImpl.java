package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSerachServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map search(Map searchMap) {

        Map<String,Object> map = new HashMap<>();

        //关键字空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));

     /*   Query query = new SimpleQuery("*:*");
        *//*添加查询条件*//*
       if(searchMap.get("keywords")!=null && "".equals(searchMap.get("keywords"))){
           Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));*//*使用复制域，因为关键字不一定使哪个域的
        is是匹配*//*
           query.addCriteria(criteria);
       }


        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);

        map.put("rows",page.getContent());*/

       //1.查询列表加高亮
        map.putAll(searchList(searchMap));
        //2分组查询商品分类列表
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);
        //3查询品牌和规格名称
        String categoryName=(String)searchMap.get("category");
        if(!"".equals(categoryName)){//如果有分类名称
            map.putAll(searchBrandAndSpecList(categoryName));
        }else{//如果没有分类名称，按照第一个查询
            if(categoryList.size()>0){
                /*为什么取的是第一个分类名称,默认为查一个分类*/
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }
        return map;
        }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID"+goodsIdList);
        Query query=new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();

    }


    /*列表查询*/
        private Map searchList(Map searchMap){
            Map<String,Object> map = new HashMap<>();
//********************************888高亮初始化*+*************************
            HighlightQuery query = new SimpleHighlightQuery();
            /*在哪列加高亮*/
            HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
            highlightOptions.setSimplePrefix("<em style='color:red'>");/*前缀*/
            highlightOptions.setSimplePostfix("</em>");

            /*为查询对象设置高亮选项*/
            query.setHighlightOptions(highlightOptions);
            //1.1关键字查询
            Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
            query.addCriteria(criteria);
//*********************开始过滤**************************8
            //1.2按分类筛选******************************88
            if (!"".equals(searchMap.get("category"))) {
                Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            //1.3按品牌筛选
            if(!"".equals(searchMap.get("brand"))){
                Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

            //1.4过滤规格
            if(searchMap.get("spec")!=null){
                Map<String,String> specMap= (Map) searchMap.get("spec");
                for(String key:specMap.keySet() ){/*使用键值对*/
                    Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key) );
                    FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                    query.addFilterQuery(filterQuery);
                }
            }
            //1.5价格过滤
            if(!"".equals(searchMap.get("price"))){
                String[] price = ((String) searchMap.get("price")).split("-");
                if(!price[0].equals("0")){/*最低价格不等于0*/
                    FilterQuery filterQuery = new SimpleFacetQuery();
                    Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                    filterQuery.addCriteria(filterCriteria);
                    query.addFilterQuery(filterQuery);
                }
                if(!price[1].equals("*")){/*最高价格budengyu※*/
                    FilterQuery filterQuery = new SimpleFacetQuery();
                    Criteria filterCriteria = new Criteria("item_price").lessThan(price[1]);
                    filterQuery.addCriteria(filterCriteria);
                    query.addFilterQuery(filterQuery);
                }
            }

            //1.6分页查询
            Integer pageNo= (Integer) searchMap.get("pageNo");//提取页码
            if(pageNo==null){
                pageNo=1;//默认第一页
            }
            Integer pageSize=(Integer) searchMap.get("pageSize");//每页记录数
            if(pageSize==null){
                pageSize=20;//默认20
            }
            query.setOffset((pageNo-1)*pageSize);//起始索引
            query.setRows(pageSize);//每页纪录数


            //1.7排序
            String sortValue = (String) searchMap.get("sort");//升序ASC  DESC
            String sortField = (String) searchMap.get("sortField");//排序字段
            if(sortValue!=null && !sortValue.equals("")){
                if(sortValue.equals("ASC")){
                    Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                    query.addSort(sort);
                }
                if(sortValue.equals("DESC")){
                    Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                    query.addSort(sort);
                }

            }



            //*****************************获取高亮结果值88888888888********************************
            /*高亮页对象*/
            HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

            /*h获取高亮入口集合(每条记录的高亮入口),高亮处理*/
            List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
            for (HighlightEntry<TbItem> entry : entryList) {
                /*获取高亮列表(高亮域的个数)*/
                List<HighlightEntry.Highlight> highlightList = entry.getHighlights();

            /*   for (HighlightEntry.Highlight h : highlightList) {
                List<String> sns = h.getSnipplets();*//*每个域可能又多个值*//*
                System.out.println(sns);
         */
                if(highlightList.size()>0 && highlightList.get(0).getSnipplets().size()>0){
                    TbItem item = entry.getEntity();
                    item.setTitle( highlightList.get(0).getSnipplets().get(0));

                }
            }
            map.put("rows",page.getContent());
            map.put("totalPages",page.getTotalPages());//总页数
            map.put("total",page.getTotalElements());//总纪录数
            return map;

        }



    /**
     *
     * @param searchMap
     * @return 分组查询，分类列表
     */
    private List<String> searchCategoryList(Map searchMap){

        ArrayList<String> list = new ArrayList();

        Query query = new SimpleQuery("*:*");
        //关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
       //获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取分组结果对象
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //获取分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //获取分组入口集合
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
        //遍历
        for (GroupEntry<TbItem> entry : entryList) {
            list.add(entry.getGroupValue());//见分组结果添加到返回值
        }
        return list;
        

    }

    /**
     * 查询品牌和规格列表
     * @param searchMap
     * @return
     */
    @Autowired
    private RedisTemplate redisTemplate;

    private Map searchBrandAndSpecList(String category){
        Map map = new HashMap();
        //1根据商品分类名的得到模板iD
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);

        if(templateId!=null){
            //2根据模板iD获取品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList",brandList);

            //3根据模板id获取规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList",specList);

        }
        return map;
    }




}

