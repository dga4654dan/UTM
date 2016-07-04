package com.dc.utm.user.flag;

import com.dc.utm.entity.IBaseUser;

/**
 * 
 * 用户登录标志位管理类
 * 用户登录标志位 是 utm最重要的标志位，用于标识用户是否已经登录，在哪里登录
 * 建议将 用户登录标志 作为db的一张表处理，下面的每个方法都将给出相应的参考sql或伪代码（仅作为参考），
 * 表结构：（机器id为当前服务的Id，不同服务需要有不同的Id）
 * CREATE TABLE `user_login_flag` (
 *   `user_id` INT(11) UNSIGNED NOT NULL COMMENT '用户id',   
 *   `machine_id` INT(11) NOT NULL COMMENT '机器id',                         
 *   PRIMARY KEY pk_user_login_flag(`user_id`, `machine_id`)               
 * ) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT '用户登录信息表';
 * 
 * 
 * 当然，为了前期快速开发出一个原型，这里提供了一个简单的基于hashmap的UserFlagBusiness的实现SimpleLocalUserFlagBusiness
 * 注意：这个只是用来方便前期开发的，并不建议最终使用这个实现，因为这个实现只能保证一个服务的正确性
 * （因为它仅仅是将信息记录在内存中，推荐将信息保存在db中或者cache中，这样多个服务可以共享这些数据），
 * 而且在数据发生错误时会比较麻烦（如果在db中只需更新下数据）
 * 
 * @author Daemon
 * 
 * @param <Visitor> 游客
 * @param <UserKey> 用户Id
 * @param <User> 用户
 */
public abstract class UserFlagBusiness<Visitor, UserKey, User extends IBaseUser<UserKey>> {
	
	/**
	 * 尝试设置用户标志位，如果用户没有登录则设置标志位并返回"成功"，如果用户在其他地方登录则返回 "在本服务登录"或 "在其他服务登录"
	 * 
	 * eg: 
	 * while(true){
	 *     try {
	 *         UserLoginFlag userLoginFlag = < SELECT * FROM user_login_flag WHERE user_id = #{userId};
	 *         if( userLoginFlag == null ) {
	 *				
	 *				> INSERT INTO user_login_flag(user_id,machine_id) VALUES(#{userId}, #{machineId});
	 *				return SetUserLoginFlagResult.SUCCESS;
	 *			} else {
	 *				
	 *				if( userLoginFlag.getMachineId() == machineId ) {
	 *					return SetUserLoginFlagResult.FAIL_LOGIN_LOCAL;
	 *				} else {
	 *					return SetUserLoginFlagResult.FAIL_LOGIN_OTHER;
	 *				}
	 *			}
	 *     } catch (Exception e) {
	 *     
	 *         if( e 是唯一键冲突  ){
	 *             continue;
	 *         } else {
	 *             // 其他未知异常，记录异常方便查询，暂且返回FAIL_LOGIN_LOCAL
	 *             log exception
	 *             return SetUserLoginFlagResult.FAIL_LOGIN_LOCAL;
	 *         }
	 *     }
	 * }
	 * 
	 * @param requestId 请求Id
	 * @param visitor 游客对象
	 * @param user 用户对象
	 * @param param 请求参数
	 * @return 设置用户登录标志位结果
	 */
	public abstract SetLoginFlagResult setLoginFlag(int requestId, Visitor visitor, User user, Object param);
	
	/**
	 * 登录连接检查失败，移除登录标志位
	 * （关于“登录连接检查”问题，可以查看OnUserLoginHandler.loginLinkCheck的说明）
	 * 
	 * eg: > DELETE FROM user_login_flag WHERE user_id = #{userId} AND machine_id = #{machineId}
	 * 
	 * @param requestId 请求Id
	 * @param user 用户
	 * @param param 参数
	 */
	public abstract void rollBackLoginFlagWhenLinkDisable(int requestId, User user, Object param);
	
	/**
	 * 用户退出，移除登录标志位
	 * 
	 * eg: > DELETE FROM user_login_flag WHERE user_id = #{userId} AND machine_id = #{machineId}
	 * 
	 * @param requestId 请求Id
	 * @param user 用户
	 * @param param 参数
	 */
	public abstract void removeLoginFlag(int requestId, User user, Object param);
	
	/**
	 * 用户（id：userKey）是否在本服务上登录
	 * 
	 * eg: > SELECT 1 FROM user_login_flag WHERE user_id = #{userId} AND machine_id = #{machineId}
	 * 
	 * @param userKey 用户id
	 */
	public abstract boolean isLoginLocal(UserKey userKey);
	
}
