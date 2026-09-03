package com.knowledge.knowledgeagentplatform.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.knowledge.knowledgeagentplatform.common.exception.BusinessException;
import com.knowledge.knowledgeagentplatform.common.utils.BeanCopyUtil;
import com.knowledge.knowledgeagentplatform.controller.param.AddUserParam;
import com.knowledge.knowledgeagentplatform.controller.param.UpdateUserParam;
import com.knowledge.knowledgeagentplatform.entity.AppUser;
import com.knowledge.knowledgeagentplatform.mapper.AppUserMapper;
import com.knowledge.knowledgeagentplatform.service.AppUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author whw
 * @since 2026-09-01
 */
@Service
public class AppUserServiceImpl extends ServiceImpl<AppUserMapper, AppUser> implements AppUserService {


    private final AppUserMapper appUserMapper;

    private final PasswordEncoder passwordEncoder;

    public AppUserServiceImpl(AppUserMapper appUserMapper, PasswordEncoder passwordEncoder) {
        this.appUserMapper = appUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addUser(AddUserParam addUserParam) {
        AppUser appUser = new AppUser();
        BeanUtils.copyProperties(addUserParam, appUser);
        String encode = passwordEncoder.encode(addUserParam.userPassword());
        appUser.setUserPassword(encode);
        appUserMapper.insert(appUser);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUser(UpdateUserParam updateUserParam) {
        AppUser appUser = appUserMapper.selectById(updateUserParam.id());
        if (appUser == null) {
            throw new BusinessException("当前id为 ：" + updateUserParam.id() + "不存在,请核实后重新上传");
        }
        //名字感觉没必要进行校验
        BeanCopyUtil.copyProperty(updateUserParam, appUser);
        appUserMapper.updateById(appUser);
    }

}
