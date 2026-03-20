package com.lichuan.tea.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
}
