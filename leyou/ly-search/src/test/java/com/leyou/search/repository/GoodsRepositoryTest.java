package com.leyou.search.repository;


import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {


    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private SearchService searchService;

    @Autowired
    private GoodsClient goodsClient;

    @Test
    public void testCreateIndex(){
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }

    @Test
    public void loadData(){
        int page = 1;
        int rows = 100;
        int size = 0;
        do{
            //查询spu信息
            PageResult<Spu> result = goodsClient.querySpuByPage(page, rows, true, null);

            List<Spu> spuList = result.getItems();
            if(CollectionUtils.isEmpty(spuList)){
                break;
            }

            // 构建成goods
            List<Goods> goodsList = spuList.stream().map(searchService::buildGoods).collect(Collectors.toList());

            // 存入索引库
            goodsRepository.saveAll(goodsList);

            // 翻页
            page++;
            size = spuList.size();
        }while (size==100);
    }

    @Test
    public void test(){
        int size = 100;
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id"},null));
        // 添加查询条件
        queryBuilder.withQuery(QueryBuilders.matchAllQuery());
        // 分页  注意从0开始
        queryBuilder.withPageable(PageRequest.of(1,size));


        PageImpl<Goods> result = (PageImpl<Goods>) goodsRepository.search(queryBuilder.build());

        long total = result.getTotalElements();
        System.out.println("size = " + size);
        System.out.println("total = " + total);
        System.out.println("resultTotalPages = " + result.getTotalPages());
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) total / (double) size);
        System.out.println("totalPages = " + totalPages);
    }




}
