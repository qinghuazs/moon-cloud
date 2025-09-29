package com.moon.cloud.response.example;

import com.moon.cloud.response.adapter.ResponseAdapter;
import com.moon.cloud.response.enums.ResponseCode;
import com.moon.cloud.response.handler.BusinessException;
import com.moon.cloud.response.web.MoonCloudResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.*;

/**
 * 示例控制器 - 展示如何使用统一响应类
 *
 * 这个类展示了如何将旧的响应类替换为新的 MoonCloudResponse
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/example")
public class ExampleController {

    // ========== 基础用法示例 ==========

    /**
     * 示例1：简单成功响应
     * 旧代码：return Result.success();
     * 新代码：return MoonCloudResponse.success();
     */
    @GetMapping("/success")
    public MoonCloudResponse<Void> successExample() {
        // 执行业务逻辑
        log.info("执行成功响应示例");
        return MoonCloudResponse.success();
    }

    /**
     * 示例2：带数据的成功响应
     * 旧代码：return Result.success(user);
     * 新代码：return MoonCloudResponse.success(user);
     */
    @GetMapping("/user/{id}")
    public MoonCloudResponse<User> getUserExample(@PathVariable Long id) {
        // 模拟查询用户
        User user = findUserById(id);
        return MoonCloudResponse.success(user);
    }

    /**
     * 示例3：自定义消息的成功响应
     * 旧代码：return Result.success("创建成功", user);
     * 新代码：return MoonCloudResponse.success("创建成功", user);
     */
    @PostMapping("/user")
    public MoonCloudResponse<User> createUserExample(@Valid @RequestBody UserDTO dto) {
        User user = createUser(dto);
        return MoonCloudResponse.success("用户创建成功", user);
    }

    /**
     * 示例4：错误响应（使用异常）
     * 旧代码：return Result.error(ResultCode.USER_NOT_FOUND);
     * 新代码：throw new BusinessException(ResponseCode.USER_NOT_FOUND);
     */
    @GetMapping("/user/check/{id}")
    public MoonCloudResponse<User> checkUserExample(@PathVariable Long id) {
        User user = findUserById(id);
        if (user == null) {
            // 抛出异常，由全局异常处理器自动处理
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }
        return MoonCloudResponse.success(user);
    }

    // ========== 分页响应示例 ==========

    /**
     * 示例5：分页响应
     * 旧代码：return PageResult.success(page);
     * 新代码：使用 MoonCloudResponse.page()
     */
    @GetMapping("/users/page")
    public MoonCloudResponse<List<User>> pageUsersExample(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {

        // 模拟分页数据
        List<User> users = getUserPageData(current, size);
        long total = 100; // 模拟总数

        // 创建分页响应
        return MoonCloudResponse.page(users, current, size, total);
    }

    // ========== 异常处理示例 ==========

    /**
     * 示例6：业务异常处理
     * 展示各种抛出异常的方式
     */
    @PutMapping("/user/{id}/password")
    public MoonCloudResponse<Void> updatePasswordExample(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {

        User user = findUserById(id);

        // 方式1：直接抛出异常
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        // 方式2：条件性抛出异常
        BusinessException.throwIf(
            user.isLocked(),
            ResponseCode.ACCOUNT_LOCKED,
            "账户已被锁定，无法修改密码"
        );

        // 方式3：自定义消息
        if (!oldPassword.equals(user.getPassword())) {
            throw new BusinessException(
                ResponseCode.OLD_PASSWORD_ERROR,
                "原密码不正确"
            );
        }

        // 更新密码
        user.setPassword(newPassword);
        return MoonCloudResponse.success("密码修改成功");
    }

    /**
     * 示例7：参数校验异常（自动处理）
     * 当参数校验失败时，全局异常处理器会自动返回标准错误响应
     */
    @PostMapping("/validate")
    public MoonCloudResponse<UserDTO> validateExample(@Valid @RequestBody UserDTO dto) {
        // 如果参数校验失败，会自动返回：
        // {
        //   "code": 8000,
        //   "message": "参数校验失败: username - 用户名不能为空",
        //   "extra": {"username": "用户名不能为空"}
        // }
        return MoonCloudResponse.success("参数校验通过", dto);
    }

    // ========== 高级用法示例 ==========

    /**
     * 示例8：链式调用
     * 添加追踪ID和扩展信息
     */
    @GetMapping("/advanced/{id}")
    public MoonCloudResponse<User> advancedExample(@PathVariable Long id) {
        User user = findUserById(id);

        return MoonCloudResponse.success(user)
                .withTraceId(UUID.randomUUID().toString())
                .withExtra(Map.of(
                    "requestTime", System.currentTimeMillis(),
                    "version", "1.0.0"
                ));
    }

    /**
     * 示例9：兼容旧代码
     * 使用适配器转换旧的Result对象
     */
    @GetMapping("/compatible/{id}")
    public MoonCloudResponse<User> compatibleExample(@PathVariable Long id) {
        // 假设这是旧服务返回的Result对象
        OldResult<User> oldResult = oldService.getUser(id);

        // 使用适配器转换
        return ResponseAdapter.convert(oldResult);
    }

    // ========== 替换示例对比 ==========

    /**
     * 完整的替换示例：展示旧代码如何改写
     */
    @GetMapping("/migration-example")
    public MoonCloudResponse<String> migrationExample() {
        /* ===== 旧代码 =====

        // 1. 成功响应
        return Result.success();
        return Result.success(data);
        return Result.success("消息", data);

        // 2. 错误响应
        return Result.error();
        return Result.error("错误消息");
        return Result.error(ResultCode.USER_NOT_FOUND);

        // 3. 分页响应
        return PageResult.success(page);

        // 4. 判断
        if (!result.isSuccess()) { ... }

        ===== 新代码 ===== */

        // 1. 成功响应
        // return MoonCloudResponse.success();
        // return MoonCloudResponse.success(data);
        // return MoonCloudResponse.success("消息", data);

        // 2. 错误响应（推荐使用异常）
        // throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        // 或者直接返回
        // return MoonCloudResponse.error(ResponseCode.USER_NOT_FOUND);

        // 3. 分页响应
        // return ResponseAdapter.fromPage(page);

        // 4. 判断
        // if (response.isError()) { ... }

        return MoonCloudResponse.success("迁移示例展示完成");
    }

    // ========== 辅助方法和类 ==========

    private User findUserById(Long id) {
        // 模拟查询
        if (id == 1L) {
            User user = new User();
            user.setId(id);
            user.setUsername("admin");
            user.setEmail("admin@example.com");
            user.setPassword("password");
            user.setLocked(false);
            return user;
        }
        return null;
    }

    private User createUser(UserDTO dto) {
        User user = new User();
        user.setId(System.currentTimeMillis());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword("encrypted");
        user.setLocked(false);
        return user;
    }

    private List<User> getUserPageData(int current, int size) {
        List<User> users = new ArrayList<>();
        int start = (current - 1) * size;
        for (int i = 0; i < size; i++) {
            User user = new User();
            user.setId((long) (start + i + 1));
            user.setUsername("user" + user.getId());
            user.setEmail("user" + user.getId() + "@example.com");
            user.setPassword("password");
            user.setLocked(false);
            users.add(user);
        }
        return users;
    }

    // 模拟旧服务
    private final OldService oldService = new OldService();

    @Data
    public static class User {
        private Long id;
        private String username;
        private String email;
        private String password;
        private boolean locked;
    }

    @Data
    public static class UserDTO {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
        private String username;

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
        private String password;
    }

    // 模拟旧的Result类
    @Data
    public static class OldResult<T> {
        private Integer code;
        private String message;
        private T data;

        public static <T> OldResult<T> success(T data) {
            OldResult<T> result = new OldResult<>();
            result.code = 200;
            result.message = "success";
            result.data = data;
            return result;
        }
    }

    // 模拟旧服务
    public static class OldService {
        public OldResult<User> getUser(Long id) {
            User user = new User();
            user.setId(id);
            user.setUsername("oldUser");
            user.setEmail("old@example.com");
            return OldResult.success(user);
        }
    }
}