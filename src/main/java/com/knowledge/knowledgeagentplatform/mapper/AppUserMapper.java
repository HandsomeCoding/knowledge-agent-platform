package com.knowledge.knowledgeagentplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.knowledgeagentplatform.controller.param.UpdateUserParam;
import com.knowledge.knowledgeagentplatform.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author whw
 * @since 2026-09-01
 */
@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {

    void selectById(UpdateUserParam updateUserParam);
}
