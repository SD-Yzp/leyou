package com.leyou.item.web;

import com.leyou.common.dto.CartDto;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

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
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key
    ) {
        return ResponseEntity.ok(goodsService.querySpuByPage(page, rows, saleable, key));
    }

    /**
     * 新增商品
     * @param spu
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu) {
        goodsService.saveGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改商品
     * @param spu
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu) {
        goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据spuId查询spu_detail信息
     * @param spuId
     * @return
     */
    @GetMapping("/spu/detail/{id}")
    public ResponseEntity<SpuDetail> queryDetailById(@PathVariable("id")Long spuId) {
        return ResponseEntity.ok(goodsService.queryDetailById(spuId));
    }

    /**
     * 根据spuID查询sku信息
     * @param spuId
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuById(@RequestParam("id") Long spuId) {
        return ResponseEntity.ok(goodsService.querySkuById(spuId));
    }

    /**
     * 根据ids查询sku信息
     * @param ids
     * @return
     */
    @GetMapping("sku/list/ids")
    public ResponseEntity<List<Sku>> querySkuByIds(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(goodsService.querySkuByIds(ids));
    }

    /**
     * 根据spuID删除商品信息
     * @param id
     * @return
     */
    @DeleteMapping("goods")
    public ResponseEntity<Void> deleteGoodsById(@RequestParam("id") Long id) {
        goodsService.deleteGoodsById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("goods/spu/out/{id}")
    public ResponseEntity<Void> updateSaleableById(@PathVariable("id") Long id) {
        goodsService.updateSaleableById(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据id查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }

    @PostMapping("stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDto> carts) {
        goodsService.decreaseStock(carts);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
