package org.paland.admin;

import org.junit.jupiter.api.Test;
import org.paland.common.result.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void success() throws Exception {
        mockMvc.perform(get("/test/success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("hello paland"));
    }

    @Test
    void fail() throws Exception {
        mockMvc.perform(get("/test/fail").param("userId", "123"))
                .andExpect(status().isOk())  // 业务异常通常返回200，code是错误码
                .andExpect(jsonPath("$.code").value(ResultCode.USER_NOT_FOUND.getCode()));
    }

    @Test
    void error() throws Exception {
        mockMvc.perform(get("/test/error"))
                .andExpect(status().isInternalServerError());  // 预期抛出500
    }

    @Test
    void valid_success() throws Exception {
        String json = """
                {
                    "username": "testuser",
                    "age": 25
                }
                """;

        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void valid_fail() throws Exception {
        String json = """
                {
                    "username": "",
                    "age": -1
                }
                """;

        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());  // 参数校验失败通常返回400
    }
}