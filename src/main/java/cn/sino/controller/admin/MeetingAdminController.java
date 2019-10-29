package cn.sino.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;
import com.micro.model.DeptInfo;
import com.micro.model.UserInfo;
import com.micro.push.model.NettyUserBean;
import com.micro.push.model.PushResult;
import com.micro.push.service.DubboNettyService;
import com.micro.service.dubbo.user.DubboDeptService;
import com.micro.service.dubbo.user.DubboUserService;

import cn.sino.common.Result;
import cn.sino.common.ResultUtils;
import cn.sino.mvc.UserInfoAdmin;
import cn.sino.mvc.UserInfoUtils;
import cn.sino.service.dubbo.appointment.DubboMeetingService;

@RestController
@RequestMapping("/app/meeting")
public class MeetingAdminController {
	
	@Reference(check=false)
	private DubboMeetingService dubboMeetingService;
	@Reference(check=false)
	private DubboNettyService dubboNettyService;
	@Reference(check=false)
	private DubboUserService dubboUserService;
	@Reference(check=false)
	private DubboDeptService dubboDeptService;
	//梧州
	//系统id
	@Value("${server.netty.appId}")
	private String appId;
	//业务类型id
	@Value("${server.netty.busiTypeId}")
	private String busiTypeId;

	
	@RequestMapping("/add")
	public Result findMyasset(HttpServletRequest request){
		try {
			UserInfoAdmin userInfo = UserInfoUtils.getBeanAdmin(request);
			String userId = userInfo.getId();
			String username = userInfo.getNickname();
			String title = request.getParameter("title");
			String content = request.getParameter("content");
			String address = request.getParameter("address");
			String time = request.getParameter("time");
			String member = request.getParameter("member");
			String meetingid = dubboMeetingService.add(title, content, userId, username, address, time,member);
			List<Map> parseArray = JSONObject.parseArray(member,Map.class);		
			parseArray.forEach(d->{
				String userid = d.get("userid").toString();
				String name = d.get("username").toString();
				dubboMeetingService.addMember(meetingid, userid, name);
				NettyUserBean bean=new NettyUserBean();
				bean.setAppid(appId);
				bean.setBusitypeid(busiTypeId);
			    bean.setTitle("会议邀请");//推送标题
			    bean.setContent("内容：会议发布人："+username+"会议时间："+time+"，会议内容："+content);//推送内容
			    bean.setUserId(userid);//接收人id
			    bean.setUserName(name);//接收人姓名
			    PushResult result=dubboNettyService.sendToUser(bean);
			    System.out.println(result.getMsg());
			});
			return ResultUtils.success("发布成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findMyRelease")
	public Result findMyRelease(HttpServletRequest request){
		try {
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String time = request.getParameter("time");
			List<Map<String, Object>> list = dubboMeetingService.findMyRelease(userid, time);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findMyMeeting")
	public Result findMyMeeting(HttpServletRequest request){
		try {
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			String time = request.getParameter("time");
			List<Map<String, Object>> list = dubboMeetingService.findMyMeeting(userid, time);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findDetail")
	public Result findDetail(String id,HttpServletRequest request){
		try {
			UserInfoAdmin userinfo = UserInfoUtils.getBeanAdmin(request);
			String userid = userinfo.getId();
			Map<String, Object> map = dubboMeetingService.findDetail(id,userid);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findDetails")
	public Result findDetails(String id){
		try {
			Map<String, Object> map = dubboMeetingService.findDetails(id);
			return ResultUtils.success("查询成功", map);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	

	@RequestMapping("/updatestatus")
	public Result updatestatus(HttpServletRequest request,String id,String status){
		try {
			String reason ="";
			if(status.equals("2")){
				reason=request.getParameter("reason");
			}
			dubboMeetingService.updatestatus(id, status, reason);
			return ResultUtils.success("更新成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/findDeptsAndUsers")
	public Result findDeptsAndUsers(){
		try {
			List<DeptInfo> list = dubboDeptService.findAll("");
			List<Map<String,Object>>deptList=new ArrayList<Map<String,Object>>();
			list.forEach(d->{
				Map<String,Object>deptidMap =new HashMap<String,Object>();
				List<Map<String,Object>>userList=new ArrayList<Map<String,Object>>();
				String deptid = d.getId();
				String name = d.getName();
				deptidMap.put("deptid", deptid);
				deptidMap.put("deptname", name);
				dubboUserService.getUserByDeptId(deptid).forEach(u->{
					Map<String,Object>userMap=new HashMap<String,Object>();
					String userid = u.getId();
					String username = u.getNickname();
					userMap.put("userid", userid);
					userMap.put("username", username);
					userList.add(userMap);
				});
				deptidMap.put("userList",userList);
				deptList.add(deptList.size(), deptidMap);
			});
			return ResultUtils.success("查询成功", deptList);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	
	
	@RequestMapping("/findAllDept")
	public Result findAllDept(){
		try {
			List<DeptInfo> list = dubboDeptService.findAll("");
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/getUserByDeptId")
	public Result getUserByDeptId(String deptid){
		try {
			if(deptid==null||"".equals(deptid)){
				throw new RuntimeException("部门id为空");
			}
			List<UserInfo> list = dubboUserService.getUserByDeptId(deptid);
			return ResultUtils.success("查询成功", list);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}
	
	@RequestMapping("/withdraw")
	public Result withdraw(String id){
		try {
			dubboMeetingService.withdraw(id);
			return ResultUtils.success("撤回成功", null);
		} catch (Exception e) {
			return ResultUtils.error(e.getMessage());
		}
	}


}