package com.lichuan.tea.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lichuan.tea.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
