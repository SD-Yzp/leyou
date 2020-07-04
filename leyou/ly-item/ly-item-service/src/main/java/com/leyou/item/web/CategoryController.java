package com.leyou.item.web;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父节点id查询商品分类
     * @param pid
     * @return
     */
    @RequestMapping("list")
    public ResponseEntity<List<Category>> queryCategoryByParentId(@RequestParam(value="pid",defaultValue = "0")Long pid){
        List<Category> categoryList = categoryService.queryCategoryByParentId(pid);
        return ResponseEntity.status(HttpStatus.OK).body(categoryList);
    }

    /**
     * 根据id查询商品分类
     * @param ids
     * @return
     */
    @RequestMapping("list/ids")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("ids")List<Long> ids){
        return ResponseEntity.ok(categoryService.queryByIds(ids));
    }

}
