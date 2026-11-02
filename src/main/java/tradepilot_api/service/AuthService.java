package tradepilot_api.service;

import org.springframework.stereotype.Service;
import tradepilot_api.dto.AuthResponse;
import tradepilot_api.dto.LoginRequest;
import tradepilot_api.dto.LoginResponse;
import tradepilot_api.dto.RegisterRequest;

@Service
public interface AuthService {
    AuthResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}
