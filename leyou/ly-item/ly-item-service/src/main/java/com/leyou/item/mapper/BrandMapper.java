package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand> {

    @Insert("insert into tb_category_brand (category_id, brand_id) values (#{cid},#{bid})")
    int insertCategoryBrand(@Param("cid")Long cid,@Param("bid")Long bid);

    @Select("select b.* from tb_brand b inner join tb_category_brand cb on b.id = cb.`brand_id` where cb.`category_id` = #{cid}")
    List<Brand> queryBrandByCid(@Param("cid")Long cid);
}
