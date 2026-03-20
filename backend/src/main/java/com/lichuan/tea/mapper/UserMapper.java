package com.lichuan.tea.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lichuan.tea.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
