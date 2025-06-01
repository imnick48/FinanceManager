package com.finManager.FinanceManager.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.finManager.FinanceManager.Repository.UserRepository;
import com.finManager.FinanceManager.models.User;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String googleId = oAuth2User.getAttribute("sub");
        String name = oAuth2User.getAttribute("name");
        String email = oAuth2User.getAttribute("email");
        String pictureUrl = oAuth2User.getAttribute("picture");
        
        Optional<User> existingUser = userRepository.findByGoogleId(googleId);
        User user;
        
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setName(name);
            user.setEmail(email);
            user.setPictureUrl(pictureUrl);
            user.setUpdatedAt(LocalDateTime.now());
        } else {
            user = new User();
            user.setGoogleId(googleId);
            user.setName(name);
            user.setEmail(email);
            user.setPictureUrl(pictureUrl);
        }
        
        userRepository.save(user);
        
        return oAuth2User;
    }
}