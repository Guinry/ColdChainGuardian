package com.coldchain.guardian.infra.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.guardian.infra.persistence.entity.UserEntity;
import com.coldchain.guardian.infra.persistence.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据用户名查找用户
     */
    public UserEntity findByUsername(String username) {
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserEntity::getUsername, username);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 保存用户
     */
    public void save(UserEntity user) {
        if (user.getId() == null) {
            userMapper.insert(user);
        } else {
            userMapper.updateById(user);
        }
    }

    /**
     * 根据ID查找用户
     */
    public UserEntity findById(Long id) {
        return userMapper.selectById(id);
    }
}