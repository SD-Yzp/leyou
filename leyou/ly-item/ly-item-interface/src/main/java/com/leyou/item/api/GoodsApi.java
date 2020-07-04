package com.leyou.item.api;

import com.leyou.common.dto.CartDto;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {

    /**
     * 商品查询
     *
     * @param page
     * @param rows
     * @param saleable
     * @param key
     * @return
     */
    @GetMapping(value = "/spu/page")
    PageResult<Spu> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key
    );

    /**
     * 根据spuId查询spu_detail信息
     * @param spuId
     * @return
     */
    @GetMapping("/spu/detail/{id}")
    SpuDetail queryDetailById(@PathVariable("id")Long spuId);

    /**
     * 根据spuID查询sku信息
     * @param spuId
     * @return
     */
    @GetMapping("sku/list")
    List<Sku> querySkuById(@RequestParam("id") Long spuId);

    /**
     * 根据spuId查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    Spu querySpuById(@PathVariable("id") Long id);

    /**
     * 根据id批量查询sku
     * @param ids
     * @return
     */
    @GetMapping("sku/list/ids")
    List<Sku> querySkuByIds(@RequestParam("ids") List<Long> ids);

    /**
     * 减少库存
     * @param carts
     * @return
     */
    @PostMapping("stock/decrease")
    Void decreaseStock(@RequestBody List<CartDto> carts);
}
