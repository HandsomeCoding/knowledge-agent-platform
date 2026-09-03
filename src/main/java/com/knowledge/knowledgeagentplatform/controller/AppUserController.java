package com.knowledge.knowledgeagentplatform.controller;

import com.knowledge.knowledgeagentplatform.common.exception.Result;
import com.knowledge.knowledgeagentplatform.controller.param.AddUserParam;
import com.knowledge.knowledgeagentplatform.controller.param.UpdateUserParam;
import com.knowledge.knowledgeagentplatform.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author whw
 * @since 2026-09-01
 */
@RestController
@RequestMapping("/knowledgeagentplatform/appUser")
public class AppUserController {


    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }


    @PostMapping("/updateUser")
    public Result<String> updateUser(@RequestBody @Valid UpdateUserParam updateUserParam) {
        appUserService.updateUser(updateUserParam);
        return Result.success("用户注册成功");
    }

}
