package com.tencent.wxcloudrun.dao;


import com.tencent.wxcloudrun.dto.UserInfo;
import com.tencent.wxcloudrun.dto.UserUsage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户信息数据库接口
 * @author czm
 */
@Repository
@Mapper
public interface UserServiceDao {

    @Select("select user_id,user_name from ts_user")
    List<UserInfo> queryUserInfo();

    @Insert("insert into ts_user (user_id,user_name,reg_type,email,wx_open_id,wx_union_id,reg_time,nick_name,avater,user_passwd) " +
            "values (#{userId},#{userName},#{regType},#{email},#{wxOpenId},#{wcUnionId},#{regTime},#{nickTime},#{avater},#{userPasswd})")
    int addUserInfo(UserInfo userInfo);

    @Select("select user_id,user_name,reg_type,email,wx_open_id,wx_union_id,reg_time,nick_name,avater,user_passwd from ts_user where email=#{email} and user_passwd=#{userPasswd}")
    UserInfo queryByEmail(String email, String userPasswd);

    @Select("select user_id,user_name,reg_type,email,wx_open_id,wx_union_id,reg_time,nick_name,avater,user_passwd from ts_user where wx_open_id=#{openId}")
    UserInfo queryByOpenId(String openId);

    @Select("select user_id,allow_count,used_count,allow_token,used_token from ts_user_usage where user_id=#{userId}")
    UserUsage queryUsage(String userId);

    @Update("update ts_user_usage set used_count=#{usedCount}, used_token=#{usedToken} where user_id=#{userId}")
    int updateUsage(UserUsage userUsage);

    @Insert("insert into ts_user_usage (user_id,allow_count,used_count,allow_token,used_token) " +
            "values (#{userId},#{allowCount},#{usedCount},#{allowToken},#{usedToken})")
    int addUsage(UserUsage userUsage);

}
