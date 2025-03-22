package org.example.ws_sever;

import org.example.ws_sever.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WsSeverApplicationTests {

    @Test
    void testGenerateJWT() {
        // 准备测试数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1234);
        claims.put("username", "testUser");
        
        // 生成JWT
        String token = JwtUtils.generateJwt(claims);
        
        // 断言
        assertNotNull(token);
        System.out.println("Generated Token: " + token);
    }

    @Test
    void testParseJWT() {
        // 准备测试数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1234);
        claims.put("username", "testUser");
        String token = JwtUtils.generateJwt(claims);

        // 解析JWT
        Integer userId = JwtUtils.getUserIdFromToken(token);

        // 断言
        assertNotNull(userId);
        assertEquals(1234, userId);
        System.out.println("Parsed userId: " + userId);
    }

    @Test
    void testValidateToken() {
        // 准备测试数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1234);
        String token = JwtUtils.generateJwt(claims);

        // 创建JwtUtils实例
        JwtUtils jwtUtils = new JwtUtils();

        // 验证有效token
        boolean isValid = jwtUtils.validateToken(token);
        assertTrue(isValid);

        // 验证无效token
        boolean isInvalidValid = jwtUtils.validateToken("invalid.token.here");
        assertFalse(isInvalidValid);
    }

    @Test
    void testExpiredToken() throws InterruptedException {
        // 这个测试需要修改JwtUtils中的expire时间为较短时间才能测试
        // 当前设置为12小时，如果需要测试过期，建议在JwtUtils中添加一个较短过期时间的方法
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1234);
        String token = JwtUtils.generateJwt(claims);
        
        // 验证token当前是有效的
        JwtUtils jwtUtils = new JwtUtils();
        assertTrue(jwtUtils.validateToken(token));
        
        System.out.println("Token is currently valid");
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Test
    void testRedis(){
        stringRedisTemplate.opsForValue().set("test","test");
        assertEquals("test",stringRedisTemplate.opsForValue().get("test"));

    }
}
