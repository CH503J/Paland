package org.paland.admin.module.demo;

import jakarta.validation.Valid;
import org.paland.common.exception.BusinessException;
import org.paland.common.result.Result;
import org.paland.common.result.ResultCode;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {

    @GetMapping("/test/success")
    public Result<String> success() {
        return Result.success("hello paland");
    }

    @GetMapping("/test/fail")
    public Result<Void> fail(@RequestParam(defaultValue = "0") String userId) {
        throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户ID=" + userId + " 不存在");
    }

    @GetMapping("/test/error")
    public Result<Void> error() {
        // 故意触发空指针，验证兜底异常处理是否生效
        String s = null;
        s.length();
        return Result.success(null);
    }

    @PostMapping("/test/valid")
    public Result<String> valid(@Valid @RequestBody TestRequest request) {
        return Result.success("校验通过, username=" + request.getUsername());
    }
}