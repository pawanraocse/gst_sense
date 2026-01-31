package com.learning.authservice.auth.service;

import com.learning.authservice.auth.dto.AuthRequestDto;
import com.learning.authservice.auth.dto.AuthResponseDto;
import com.learning.authservice.dto.SignupRequestDto;
import com.learning.authservice.dto.SignupResponseDto;
import com.learning.authservice.dto.UserInfoDto;

public interface AuthService {
    UserInfoDto getCurrentUser();

    AuthResponseDto login(AuthRequestDto request);

    SignupResponseDto signup(SignupRequestDto request);

    void logout();

    void deleteAccount();
}
