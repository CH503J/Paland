package org.paland;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TestRequest {

    @NotBlank(message = "不能为空")
    @Size(min = 2, max = 10, message = "长度必须在2到10之间")
    private String username;
}