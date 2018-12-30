package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 5000)
public class ItemSerachServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map search(Map searchMap) {

        Map<String,Object> map = new HashMap<>();

     /*   Query query = new SimpleQuery("*:*");
        *//*添加查询条件*//*
       if(searchMap.get("keywords")!=null && "".equals(searchMap.get("keywords"))){
           Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));*//*使用复制域，因为关键字不一定使哪个域的
        is是匹配*//*
           query.addCriteria(criteria);
       }


        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);

        map.put("rows",page.getContent());*/

        /*高亮显示*/
        HighlightQuery query = new SimpleHighlightQuery();
        /*在哪列加高亮*/
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");/*前缀*/
        highlightOptions.setSimplePostfix("</em>");

        /*为查询对象设置高亮选项*/
        query.setHighlightOptions(highlightOptions);


        //关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
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
        /*  未经高亮处理的*/

        return map;
        }


}

