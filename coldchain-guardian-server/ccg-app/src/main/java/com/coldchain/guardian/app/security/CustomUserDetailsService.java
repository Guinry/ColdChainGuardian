package com.coldchain.guardian.app.security;

import com.coldchain.guardian.infra.persistence.entity.UserEntity;
import com.coldchain.guardian.infra.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username);

        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // 将用户角色转换为Spring Security所需的格式
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (userEntity.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole()));
        }

        // 创建UserDetails对象
        return new User(
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getStatus() == 1, // 启用状态
                true, // 账户未过期
                true, // 凭证未过期
                true, // 账户未锁定
                authorities
        );
    }
}