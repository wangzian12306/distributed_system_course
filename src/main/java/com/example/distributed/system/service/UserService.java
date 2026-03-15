package com.example.distributed.system.service;

import com.example.distributed.system.dto.LoginRequest;
import com.example.distributed.system.dto.RegisterRequest;
import com.example.distributed.system.dto.UserResponse;

public interface UserService {
    UserResponse register(RegisterRequest request);
    UserResponse login(LoginRequest request);
}
