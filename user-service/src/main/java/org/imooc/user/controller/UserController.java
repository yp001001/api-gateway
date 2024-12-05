package org.imooc.user.controller;

import org.imooc.common.utils.JSONUtil;
import org.imooc.gateway.client.core.ApiInvoker;
import org.imooc.gateway.client.core.ApiProtocol;
import org.imooc.gateway.client.core.ApiService;
import org.imooc.user.dto.UserInfo;
import org.imooc.user.model.User;
import org.imooc.user.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@RestController
@ApiService(serviceId = "backend-user-server", protocol = ApiProtocol.HTTP, patternPath = "/user/**")
public class UserController {
    private static final String SECRETKEY = "faewifheafewhefsfjkds";//一般不会直接写代码里，可以用一些安全机制来保护
    private static final String COOKIE_NAME = "user-jwt";
    private final UserService userService;

    @ApiInvoker(path = "/login")
    @PostMapping("/login")
    public UserInfo login(@RequestParam("phoneNumber") String phoneNumer,
                          @RequestParam("code") String code,
                          HttpServletResponse response) {
        User user = userService.login(phoneNumer, code);
        String jwt = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, SECRETKEY).compact();
        response.addCookie(new Cookie(COOKIE_NAME, jwt));
        return UserInfo.builder()
            .id(user.getId())
            .nickname(user.getNickname()+"-> 灰度")
            .phoneNumber(user.getPhoneNumber() + "-> 灰度").build();
    }


    @ApiInvoker(path = "/login/limit", limit = true)
    @GetMapping("/login/limit")
    public UserInfo loginLimitTest(@RequestParam("phoneNumber") String phoneNumer,
                          @RequestParam("code") String code,
                          HttpServletResponse response) {
        User user = userService.login(phoneNumer, code);
        String jwt = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, SECRETKEY).compact();
        response.addCookie(new Cookie(COOKIE_NAME, jwt));
        return UserInfo.builder()
                .id(user.getId())
                .nickname(user.getNickname()+"-> 灰度")
                .phoneNumber(user.getPhoneNumber() + "-> 灰度").build();
    }


    @ApiInvoker(path = "/{phoneNumber}/loginlike/{code}")
    @PostMapping("/{phoneNumber}/loginlike/{code}")
    public UserInfo loginLike(@PathVariable("phoneNumber") String phoneNumer,
                          @PathVariable("code") String code,
                          HttpServletResponse response) {
        User user = userService.login(phoneNumer, code);
        String jwt = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, SECRETKEY).compact();
        response.addCookie(new Cookie(COOKIE_NAME, jwt));
        return UserInfo.builder()
                .id(user.getId())
                .nickname(user.getNickname()+"-> 灰度")
                .phoneNumber(user.getPhoneNumber() + "-> 灰度").build();
    }

    @GetMapping("/private/user-info")
    public UserInfo getUserInfo(@RequestHeader("userId") String userId) {
        log.info("userId :{}", userId);
        User user = userService.getUser(Long.parseLong(userId));
        return UserInfo.builder()
            .id(user.getId())
            .nickname(user.getNickname())
            .phoneNumber(user.getPhoneNumber()).build();
    }


    @ApiInvoker(path = "/login/post")
    @PostMapping("/login/post")
    public String login(@RequestBody UserInfo userInfo) {
        User user = userService.login(userInfo.getPhoneNumber(), userInfo.getNickname());
        return JSONUtil.toJSONString(user);
    }
}
