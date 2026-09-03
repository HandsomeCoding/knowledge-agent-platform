package com.knowledge.knowledgeagentplatform.service;

import com.knowledge.knowledgeagentplatform.controller.param.AddUserParam;
import com.knowledge.knowledgeagentplatform.controller.param.UpdateUserParam;
import com.knowledge.knowledgeagentplatform.entity.AppUser;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author whw
 * @since 2026-09-01
 */
public interface AppUserService extends IService<AppUser> {

    void addUser(AddUserParam addUserParam);

    void updateUser(UpdateUserParam updateUserParam);

}
