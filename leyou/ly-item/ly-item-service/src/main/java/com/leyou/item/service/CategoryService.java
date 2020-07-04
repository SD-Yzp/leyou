package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> queryCategoryByParentId(Long pid){
        Category category = new Category();
        category.setParentId(pid);
        //根据category对象中的非空属性来查询
        List<Category> categoryList = categoryMapper.select(category);
        if(CollectionUtils.isEmpty(categoryList)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return categoryList;
    }


    public List<Category> queryByIds(List<Long> ids){
        List<Category> categoryList = categoryMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(categoryList)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return categoryList;
    }
}
