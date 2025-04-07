package org.example.ws_sever.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AiClient {
//    @Value("${ai.api.key}")
    private String apiKey = "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";

    // OpenRouter API 的 URL
    private static final String OPENAI_API_URL = "https://openrouter.ai/api/v1/chat/completions";

    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7897));//开发调试时设置代理

    private final OkHttpClient client = new OkHttpClient.Builder()
            .proxy(proxy)//设置代理
            .build();  // 创建 OkHttpClient 对象

    public String getChatGPTResponse(GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults, GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult) throws IOException {
        // 构建提示词
        ObjectMapper objectMapper = new ObjectMapper();

        List<JsonNode> messages = new ArrayList<>();
        messages.add(objectMapper.createObjectNode().put("role", "system").put("content", "你是一个交通管理者"));
        messages.add(objectMapper.createObjectNode().put("role", "user").put("content",
                "下面是当前道路的车辆位置信息"+geoResults.toString()));
        // 添加用户消息
        messages.add(objectMapper.createObjectNode().put("role", "user").put("content", "请为"+geoResult.toString()+
                "的驾驶者给出驾驶建议以避免事故发生(要求一段40字以内的话 内容应包括：事故位置在该车的哪个方位，如何避让)"));
        // 构建请求体



        JsonNode requestBody = objectMapper.createObjectNode()
                .put("model", "meta-llama/llama-4-maverick:free")
                .set("messages", objectMapper.valueToTree(messages));
        // 构建请求
        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        System.out.println("Request: " + requestBody.toString());
        // 同步请求
        try (Response response = client.newCall(request).execute()) {
           // System.out.println("Response: " + response.body().string());
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return parseResponse(response.body().string());
        }
    }



    private String parseResponse(String responseBody) {

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode responseNode = null;
        try {
            responseNode = objectMapper.readTree(responseBody);  // 解析 JSON 字符串为 JsonNode
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response body", e); // 错误处理
        }

        // 检查 'choices' 字段是否存在并且包含至少一个元素
        JsonNode choicesNode = responseNode.path("choices");
        if (choicesNode.isArray() && choicesNode.size() > 0) {
            // 获取第一个 choice 对象中的 message 内容
            JsonNode messageNode = choicesNode.get(0).path("message");
            String content = messageNode.path("content").asText();  // 获取 content 字段的值
            return content;
        } else {
            throw new RuntimeException("Invalid response format: 'choices' is empty or missing");
        }
    }
}
