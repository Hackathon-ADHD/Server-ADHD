package adhd.diary.auth.controller;

import adhd.diary.auth.dto.response.TokenResponse;
import adhd.diary.auth.exception.token.TokenNotFoundException;
import adhd.diary.auth.jwt.JwtService;
import adhd.diary.response.ApiResponse;
import adhd.diary.response.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthApiController {

    private final JwtService jwtService;

    public AuthApiController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/api/auth/token/refresh")
    @Operation(summary = "refreshToken을 이용해 token을 재발급", description = "사용자의 토큰 정보가 만료되었을 경우 재발급 받기 위해 사용하는 API")
    public ApiResponse<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader == null) {
                throw new IllegalArgumentException("Authorization header is missing");
            }

            String refreshToken = jwtService.extractRefreshTokenFromHeader(authorizationHeader);
            TokenResponse tokenResponse = jwtService.refreshTokens(refreshToken);
            jwtService.sendAccessAndRefreshToken(response, tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());

            return ApiResponse.success(ResponseCode.JWT_REFRESH_SUCCESS, tokenResponse);
        } catch (TokenNotFoundException e) {
            return ApiResponse.fail(ResponseCode.JWT_REFRESH_TOKEN_EXPIRED, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ResponseCode.UNEXPECTED_ERROR, e.getMessage());
        }
    }
}
