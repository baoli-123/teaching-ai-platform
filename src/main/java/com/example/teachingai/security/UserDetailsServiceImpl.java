package com.example.teachingai.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.teachingai.entity.AppUser;
import com.example.teachingai.mapper.AppUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AppUserMapper appUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserMapper.selectOne(
                Wrappers.<AppUser>lambdaQuery().eq(AppUser::getUsername, username)
        );
        if (user == null) {
            throw new UsernameNotFoundException("user not found");
        }
        return new UserPrincipal(user);
    }
}
