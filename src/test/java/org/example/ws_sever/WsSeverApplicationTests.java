package org.example.ws_sever;

import org.example.ws_sever.utils.AiClient;
import org.example.ws_sever.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
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

//    @Autowired
//    private AiClient aiClient;
   @Test
    void testAIClient() throws IOException {
        // 创建 AiClient 实例
        AiClient aiClient = new AiClient();
        assertNotNull(aiClient, "AI客户端不应为空");
        
        // 准备测试数据：添加地理位置信息
        Point point = new Point(122.08, 37.5312);
        stringRedisTemplate.opsForGeo().add("vehicle:locations", point, "vehicle-15");
        
        // 搜索附近位置
        Circle searchArea = new Circle(new Point(122.08, 37.53),
                new Distance(1, Metrics.KILOMETERS));
        
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeCoordinates()
                .includeDistance()
                .sortAscending();
        
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                stringRedisTemplate.opsForGeo().radius("vehicle:locations", searchArea, args);
        
        // 确保有结果
        assertNotNull(results);
        assertTrue(results.getContent().size() > 0);
        
        // 获取第一个结果
        GeoResult<RedisGeoCommands.GeoLocation<String>> firstResult = results.getContent().get(0);
        
        // 调用 AI 客户端获取驾驶建议
        try {
            String aiResponse = aiClient.getChatGPTResponse(results, firstResult);
            
            // 验证响应
            assertNotNull(aiResponse, "AI响应不应为空");
            assertFalse(aiResponse.isEmpty(), "AI响应不应为空字符串");
            
            System.out.println("AI驾驶建议:");
            System.out.println(aiResponse);
        } catch (Exception e) {
            System.out.println("AI请求异常: " + e.getMessage());
            e.printStackTrace();
            fail("AI请求失败: " + e.getMessage());
        } finally {
            // 清理测试数据
            stringRedisTemplate.opsForGeo().remove("vehicle:locations", "vehicle-15");
        }
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Test
   // @DisplayName("应成功执行地理位置操作")
    void testRedisGeoOperations() {
        // 添加位置
        Point point = new Point(122.08, 37.5312);
        stringRedisTemplate.opsForGeo().add("vehicle:locations", point, "vehicle-15");
    
        // 搜索附近
        Circle searchArea = new Circle(new Point(122.08, 37.53),
                new Distance(1, Metrics.KILOMETERS));
        
        // 修改这里：指定要返回坐标信息
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeCoordinates()  // 包含坐标信息
                .includeDistance()     // 包含距离信息
                .sortAscending();      // 按距离升序排序
    
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                stringRedisTemplate.opsForGeo().radius("vehicle:locations", searchArea, args);
    
        assertNotNull(results);
        assertTrue(results.getContent().size() > 0);
        
        // 打印结果
        System.out.println(results);
        
        // 验证坐标点不为null
        RedisGeoCommands.GeoLocation<String> location = results.getContent().get(0).getContent();
        assertNotNull(location.getPoint());
        System.out.println("坐标点: " + location.getPoint());
    
        // 清理
        stringRedisTemplate.opsForGeo().remove("vehicle:locations", "vehicle-15");
    }
}
