package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;

import java.util.HashMap;
import java.util.Map;

@Service(timeout = 5000)
public class ItemSerachServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map search(Map searchMap) {

        Map<String,Object> map = new HashMap<>();

        Query query = new SimpleQuery("*:*");
        /*添加查询条件*/
       if(searchMap.get("keywords")!=null && "".equals(searchMap.get("keywords"))){
           Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));/*使用复制域，因为关键字不一定使哪个域的
        is是匹配*/
           query.addCriteria(criteria);
       }


        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);

        map.put("rows",page.getContent());
        return map;
    }
}
