package com.borodulin.cloudrepository.service;

import com.borodulin.cloudrepository.dao.UserCloudRepository;
import com.borodulin.cloudrepository.model.UserCloud;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static org.springframework.security.core.userdetails.User.builder;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserCloudRepository dao;
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserCloud myUser= dao.findByLogin(userName);
        if (myUser == null) {
            throw new UsernameNotFoundException("Неизвестный пользователь: " + userName);
        }
        UserDetails user = builder()
                .username(myUser.getLogin())
                .password(myUser.getPassword())
                .roles(myUser.getRole())
                .build();
        return user;
    }
}
