package org.yy.controller.project.manager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import org.yy.controller.base.BaseController;
import org.yy.entity.Page;
import org.yy.util.Const;
import org.yy.util.DateUtil;
import org.yy.util.FileUpload;
import org.yy.util.Jurisdiction;
import org.yy.util.ObjectExcelView;
import org.yy.util.PathUtil;
import org.yy.util.SpringUtil;
import org.yy.util.Tools;
import org.yy.entity.PageData;
import org.yy.service.fhoa.StaffService;
import org.yy.service.project.manager.DTPROJECTFILEService;
import org.yy.service.project.manager.PROJECTTASKService;
import org.yy.service.project.manager.RUNDETAILService;

/** 
 * 说明：项目任务
 * 作者：YuanYes QQ356703572
 * 时间：2020-09-04
 * 官网：356703572@qq.com
 */
@Controller
@RequestMapping("/projecttask")
public class PROJECTTASKController extends BaseController {
	
	@Autowired
	private PROJECTTASKService projecttaskService;
	@Autowired
	private RUNDETAILService rundetailService;
	@Autowired
	private DTPROJECTFILEService dtprojectfileService;
	@Autowired
	private StaffService staffService;
	/**保存
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/add")
	//@RequiresPermissions("projecttask:add")
	@ResponseBody
	public Object add() throws Exception{
		Map<String,Object> map = new HashMap<String,Object>();
		String errInfo = "success";
		PageData pd = new PageData();
		pd = this.getPageData();
		pd.put("PROJECTTASK_ID", this.get32UUID());	//主键
		projecttaskService.save(pd);
		map.put("result", errInfo);
		return map;
	}
	
	/**删除
	 * @param out
	 * @throws Exception
	 */
	@RequestMapping(value="/delete")
	//@RequiresPermissions("projecttask:del")
	@ResponseBody
	public Object delete() throws Exception{
		Map<String,String> map = new HashMap<String,String>();
		String errInfo = "success";
		PageData pd = new PageData();
		pd = this.getPageData();
		projecttaskService.delete(pd);
		map.put("result", errInfo);				//返回结果
		return map;
	}
	
	/**修改
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/edit")
	//@RequiresPermissions("projecttask:edit")
	@ResponseBody
	public Object edit() throws Exception{
		Map<String,Object> map = new HashMap<String,Object>();
		String errInfo = "success";
		PageData pd = new PageData();
		pd = this.getPageData();
		projecttaskService.edit(pd);
		map.put("result", errInfo);
		return map;
	}
	/**修改出厂编码
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/editBM")
	//@RequiresPermissions("projecttask:edit")
	@ResponseBody
	public Object editBM() throws Exception{
		Map<String,Object> map = new HashMap<String,Object>();
		String errInfo = "success";
		PageData pd = new PageData();
		pd = this.getPageData();
		projecttaskService.editBM(pd);
		map.put("result", errInfo);
		return map;
	}
	/**完成任务
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/finish")
	@ResponseBody
	public Object finish(
			@RequestParam(value="FPATH",required=false) MultipartFile file,
			@RequestParam(value="PROJECTTASK_ID",required=false) String PROJECTTASK_ID,
			@RequestParam(value="PTRUNSTATE",required=false) String PTRUNSTATE,
			@RequestParam(value="FNAME",required=false) String FNAME,
			@RequestParam(value="PTISOUT",required=false) String PTISOUT
			) throws Exception{
		Map<String,Object> map = new HashMap<String,Object>();
		Date date = new Date();//时间
		SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//获取当前时间
		String errInfo = "success";
		PageData pd = new PageData();
		PageData pdSave = new PageData();
		pd = this.getPageData();
		String  ffile = DateUtil.getDays(), fileName = "";
		if (null != file && !file.isEmpty()) {
			DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			Calendar calendar = Calendar.getInstance();
			String dateName = df.format(calendar.getTime());
			String filePath = PathUtil.getProjectpath() + Const.FILEPATHFILE + ffile;	//文件上传路径
			String fileNamereal = FNAME.substring(0, FNAME.indexOf(".")); // 文件上传路径
			fileName = FileUpload.fileUp(file, filePath, fileNamereal+dateName);				//执行上传
			pdSave.put("DTPROJECTFILE_ID", this.get32UUID());			//主键
			pdSave.put("FFOUNDER", Jurisdiction.getName());				//创建人
			pdSave.put("FFOUNDERNUM", Jurisdiction.getUsername());		//创建人账号
			pdSave.put("FCREATTIME", Tools.date2Str(new Date()));		//创建时间
			pdSave.put("FLASTUPDATE", Jurisdiction.getName());			//修改人
			pdSave.put("FLASTUPDATENUM", Jurisdiction.getUsername());	//修改人账号
			pdSave.put("FLASTUPDATETIME", Tools.date2Str(new Date()));	//修改时间
			pdSave.put("VISIABLE", "1");								//删除状态,0删除,1未删除
			pdSave.put("DEPTMENT_FNAME", Jurisdiction.getDEPARTMENT_ID());	//部门名称
			pdSave.put("DEPTMENT_ID", Jurisdiction.getDEPARTMENT_ID());		//部门ID
			pdSave.put("YL1", "任务完成");									//项目文件来源
			pdSave.put("FFILETYPE", "交付物文档");			
			pdSave.put("FFILEPATH", Const.FILEPATHFILE + ffile + "/" + fileName);	
			pdSave.put("FFILENAME", FNAME);
			pd.put("type", "type2");
			pd.put("PID",PROJECTTASK_ID);
			PageData pdData = dtprojectfileService.getDatax(pd);	//根据ID读取
			if(pdData != null) {
				pdSave.put("EQUIPMENT_ID", pdData.getString("PTEQUIPMENT_ID"));
				pdSave.put("PEOJECT_STAGE", pdData.getString("DEPPLAN_ID"));
				pdSave.put("ACTIVITIES_FNAME", pdData.getString("PROJECTTASK_ID"));
				pdSave.put("PROJECT_ID", pdData.getString("PTPROJECT_ID"));
			}
			PageData pd1 = new PageData();
			pd1.put("USERNAME", Jurisdiction.getName());
			PageData pd2 = staffService.getDEPTNAME(pd1);
			if(pd2 != null){
				pdSave.put("DEPTMENT_FNAME", pd2.getString("DNAME"));
			}else{
				pdSave.put("DEPTMENT_FNAME", "");
			}
			dtprojectfileService.save(pdSave);
		}
		projecttaskService.editState(pd);
		
		PageData pdMain=projecttaskService.findById(pd);
		pdMain.put("FUPTYPE", "TASK");
		rundetailService.editActual(pdMain);//更新任务表实际时间、实际工时、完成进度、当前计划状态
		
		pdMain.put("FUPTYPE", "STAFFPLAN");
		rundetailService.editActual(pdMain);//更新人员计划表实际时间、实际工时、当前计划状态、执行状态
		
		pdMain.put("FUPTYPE", "DEPTPLAN");
		rundetailService.editActual(pdMain);//更新部门计划表实际时间、实际工时、当前计划状态、执行状态
		
		pdMain.put("FUPTYPE", "PROPLAN");
		rundetailService.editActual(pdMain);//更新部门计划表实际时间、实际工时、当前计划状态、执行状态
		map.put("result", errInfo);
		return map;
	}
	/**修改执行状态
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/editState")
	@ResponseBody
	public Object editState() throws Exception{
		Map<String,Object> map = new HashMap<String,Object>();
		String errInfo = "success";
		PageData pd = new PageData();
		pd = this.getPageData();
		projecttaskService.editState(pd);
		String PTRUNSTATE=pd.getString("PTRUNSTATE");
		PageData pdMain = projecttaskService.findById(pd);	//根据ID读取
		
		SimpleDateFormat sdfdate  =new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf  =new SimpleDateFormat("yyyy-MM-dd HH:mm");
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00");  
		double dd=60*60*1000;
		if(PTRUNSTATE.equals("执行中")) {
			
		//插入执行明细
		String closeTime=Tools.date2Str(new Date(),"yyyy-MM-dd")+" 16:30";//下班时间
		PageData pdMx = new PageData();
		pdMx.put("RUNDETAIL_ID", this.get32UUID());	//主键
		pdMx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
		pdMx.put("RDCREATOR", Jurisdiction.getName());
		pdMx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
		pdMx.put("RDLAST_MODIFIER", Jurisdiction.getName());
		pdMx.put("RDTYPE", "自动");
		pdMx.put("RDSTATE", "进行中");
		pdMx.put("RDVISIBLE", "1");
		pdMx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID"));	
		pdMx.put("RDSTARTTIME", Tools.date2Str(new Date(),"yyyy-MM-dd HH:mm"));	
		Calendar calendar=Calendar.getInstance();
		calendar.setTime(sdf.parse(pdMx.getString("RDSTARTTIME")));
		if(isWeekend(calendar)==false) {//工作日
			if(sdf.parse(pdMx.getString("RDSTARTTIME")).getTime()<=sdf.parse(closeTime).getTime()) {
				pdMx.put("RDISOVER", "否");
			}else {
				pdMx.put("RDISOVER", "是");
			}
		}else if(isWeekend(calendar)==true) {//周末
			pdMx.put("RDISOVER", "是");
		}
		rundetailService.save(pdMx);
		}else if(PTRUNSTATE.equals("暂停")) {
			pd.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID"));	
			PageData pdN = rundetailService.findByIdN(pd);//查询明细进行中明细信息
			PageData pdZ = new PageData();
			String RDSTARTTIME=pdN.getString("RDSTARTTIME");
			String RDENDTIME=Tools.date2Str(new Date(),"yyyy-MM-dd HH:mm");
			//String RDENDTIME="2020-10-21 12:30";
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			String closeTime_End=sdfdate.format(sdfdate.parse(RDENDTIME))+" 16:30";//下班时间
			String startTime_End=sdfdate.format(sdfdate.parse(RDENDTIME))+" 08:00";//上班时间
			String closeTime_Start=sdfdate.format(sdfdate.parse(RDSTARTTIME))+" 16:30";//下班时间
			String startTime_Start=sdfdate.format(sdfdate.parse(RDSTARTTIME))+" 08:00";//上班时间
			pdZ.put("RDPROJECTTASK_ID", pd.getString("PROJECTTASK_ID"));	
			pdZ.put("RDSTATE", "已结束");
			pdZ.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
			pdZ.put("RDLAST_MODIFIER", Jurisdiction.getName());
			if(differentDays(sdf.parse(RDSTARTTIME),sdf.parse(RDENDTIME))==0) {//1、开始结束日期是同一天
				
			
				if(sdf.parse(RDSTARTTIME).getTime()<=sdf.parse(closeTime_Start).getTime() && sdf.parse(RDENDTIME).getTime()>sdf.parse(closeTime_Start).getTime()) {
					pdZ.put("RDENDTIME", closeTime_Start);
					pdZ.put("RDHOUR", df.format((sdf.parse(closeTime_Start).getTime()-sdf.parse(RDSTARTTIME).getTime())*1.0/dd));
					rundetailService.editEndTime(pdZ);
					
					PageData pdMx = new PageData();
					pdMx.put("RUNDETAIL_ID", this.get32UUID());	//涓婚敭
					pdMx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
					pdMx.put("RDCREATOR", Jurisdiction.getName());
					pdMx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
					pdMx.put("RDLAST_MODIFIER", Jurisdiction.getName());
					pdMx.put("RDTYPE", "自动");
					pdMx.put("RDSTATE", "已结束");
					pdMx.put("RDVISIBLE", "1");
					pdMx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID"));	
					pdMx.put("RDSTARTTIME", closeTime_Start);	
					pdMx.put("RDENDTIME", RDENDTIME);
					pdMx.put("RDISOVER", "是");
					pdMx.put("RDHOUR", df.format((sdf.parse(RDENDTIME).getTime()-sdf.parse(closeTime_Start).getTime())*1.0/dd));

					rundetailService.save(pdMx);
				}else {
					pdZ.put("RDENDTIME", RDENDTIME);	
					pdZ.put("RDHOUR", df.format((sdf.parse(RDENDTIME).getTime()-sdf.parse(RDSTARTTIME).getTime())*1.0/dd));
					rundetailService.editEndTime(pdZ);
				}
			}
			
			/*else if(differentDays(sdf.parse(RDENDTIME),sdf.parse(RDSTARTTIME))==1){//2、开始结束日期差一天
				
				if(sdf.parse(RDSTARTTIME).getTime()<=sdf.parse(closeTime_Start).getTime() && sdf.parse(RDENDTIME).getTime()>sdf.parse(startTime_End).getTime() && sdf.parse(RDENDTIME).getTime()>sdf.parse(closeTime_End).getTime()) {
				    pdZ.put("RDENDTIME", closeTime_Start); 
				    pdZ.put("RDHOUR", df.format((sdf.parse(closeTime_Start).getTime()-sdf.parse(RDSTARTTIME).getTime())*1.0/dd));
				    rundetailService.editEndTime(pdZ);
				    //插入开始日期加班工时
				    PageData pdMx = new PageData();
				    pdMx.put("RUNDETAIL_ID", this.get32UUID()); //主键
				    pdMx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
				    pdMx.put("RDCREATOR", Jurisdiction.getName());
				    pdMx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
				    pdMx.put("RDLAST_MODIFIER", Jurisdiction.getName());
				    pdMx.put("RDTYPE", "自动");
				    pdMx.put("RDSTATE", "已结束");
				    pdMx.put("RDVISIBLE", "1");
				    pdMx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID")); 
				    pdMx.put("RDSTARTTIME", closeTime_Start); 
				    pdMx.put("RDENDTIME", startTime_End);
				    pdMx.put("RDISOVER", "是");
				    pdMx.put("RDHOUR", df.format((sdf.parse(startTime_End).getTime()-sdf.parse(closeTime_Start).getTime())*1.0/dd));
				    rundetailService.save(pdMx);
				    //插入结束日期正常工时
				    PageData pdMxx = new PageData();
				    pdMxx.put("RUNDETAIL_ID", this.get32UUID()); //主键
				    pdMxx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
				    pdMxx.put("RDCREATOR", Jurisdiction.getName());
				    pdMxx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
				    pdMxx.put("RDLAST_MODIFIER", Jurisdiction.getName());
				    pdMxx.put("RDTYPE", "自动");
				    pdMxx.put("RDSTATE", "已结束");
				    pdMxx.put("RDVISIBLE", "1");
				    pdMxx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID")); 
				    pdMxx.put("RDSTARTTIME", startTime_End); 
				    pdMxx.put("RDENDTIME", closeTime_End);
				    pdMxx.put("RDISOVER", "否");
				    pdMxx.put("RDHOUR", df.format((sdf.parse(closeTime_End).getTime()-sdf.parse(startTime_End).getTime())*1.0/dd));
				    rundetailService.save(pdMxx);
				    //插入结束日期加班工时
				    PageData pdMxxx = new PageData();
				    pdMxxx.put("RUNDETAIL_ID", this.get32UUID()); //主键
				    pdMxxx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
				    pdMxxx.put("RDCREATOR", Jurisdiction.getName());
				    pdMxxx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
				    pdMxxx.put("RDLAST_MODIFIER", Jurisdiction.getName());
				    pdMxxx.put("RDTYPE", "自动");
				    pdMxxx.put("RDSTATE", "已结束");
				    pdMxxx.put("RDVISIBLE", "1");
				    pdMxxx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID")); 
				    pdMxxx.put("RDSTARTTIME", closeTime_End); 
				    pdMxxx.put("RDENDTIME", RDENDTIME);
				    pdMxxx.put("RDISOVER", "是");
				    pdMxxx.put("RDHOUR", df.format((sdf.parse(RDENDTIME).getTime()-sdf.parse(closeTime_End).getTime())*1.0/dd));
				    rundetailService.save(pdMxxx);
				   }else if(sdf.parse(RDSTARTTIME).getTime()<=sdf.parse(closeTime_Start).getTime() && sdf.parse(RDENDTIME).getTime()>sdf.parse(startTime_End).getTime() && sdf.parse(RDENDTIME).getTime()<=sdf.parse(closeTime_End).getTime())  {
					   	pdZ.put("RDENDTIME", startTime_End); 
					    pdZ.put("RDHOUR", df.format((sdf.parse(startTime_End).getTime()-sdf.parse(RDSTARTTIME).getTime())*1.0/dd));
					    rundetailService.editEndTime(pdZ);
					  
					    //插入结束日期正常工时
					    PageData pdMxx = new PageData();
					    pdMxx.put("RUNDETAIL_ID", this.get32UUID()); //主键
					    pdMxx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
					    pdMxx.put("RDCREATOR", Jurisdiction.getName());
					    pdMxx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
					    pdMxx.put("RDLAST_MODIFIER", Jurisdiction.getName());
					    pdMxx.put("RDTYPE", "自动");
					    pdMxx.put("RDSTATE", "已结束");
					    pdMxx.put("RDVISIBLE", "1");
					    pdMxx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID")); 
					    pdMxx.put("RDSTARTTIME", startTime_End); 
					    pdMxx.put("RDENDTIME", RDENDTIME);
					    pdMxx.put("RDISOVER", "否");
					    pdMxx.put("RDHOUR", df.format((sdf.parse(RDENDTIME).getTime()-sdf.parse(startTime_End).getTime())*1.0/dd));
					    rundetailService.save(pdMxx);
				   }else if(sdf.parse(RDSTARTTIME).getTime()<=sdf.parse(closeTime_Start).getTime() && sdf.parse(RDENDTIME).getTime()<=sdf.parse(startTime_End).getTime())  {
					   pdZ.put("RDENDTIME", RDENDTIME); 
					    pdZ.put("RDHOUR", df.format((sdf.parse(RDENDTIME).getTime()-sdf.parse(RDSTARTTIME).getTime())*1.0/dd));
					    rundetailService.editEndTime(pdZ);
				   }
				 
				   else if(sdf.parse(RDSTARTTIME).getTime()>sdf.parse(closeTime_Start).getTime() && sdf.parse(RDENDTIME).getTime()>sdf.parse(startTime_End).getTime() && sdf.parse(RDENDTIME).getTime()>sdf.parse(closeTime_End).getTime()) {
				    pdZ.put("RDENDTIME", startTime_End); 
				    pdZ.put("RDHOUR", df.format((sdf.parse(startTime_End).getTime()-sdf.parse(RDSTARTTIME).getTime())*1.0/dd));
				    rundetailService.editEndTime(pdZ);
				   
				    //插入结束日期正常工时
				    PageData pdMxx = new PageData();
				    pdMxx.put("RUNDETAIL_ID", this.get32UUID()); //主键
				    pdMxx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
				    pdMxx.put("RDCREATOR", Jurisdiction.getName());
				    pdMxx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
				    pdMxx.put("RDLAST_MODIFIER", Jurisdiction.getName());
				    pdMxx.put("RDTYPE", "自动");
				    pdMxx.put("RDSTATE", "已结束");
				    pdMxx.put("RDVISIBLE", "1");
				    pdMxx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID")); 
				    pdMxx.put("RDSTARTTIME", startTime_End); 
				    pdMxx.put("RDENDTIME", closeTime_End);
				    pdMxx.put("RDISOVER", "否");
				    pdMxx.put("RDHOUR", df.format((sdf.parse(closeTime_End).getTime()-sdf.parse(startTime_End).getTime())*1.0/dd));
				    rundetailService.save(pdMxx);
				    //插入结束日期加班工时
				    PageData pdMxxx = new PageData();
				    pdMxxx.put("RUNDETAIL_ID", this.get32UUID()); //主键
				    pdMxxx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
				    pdMxxx.put("RDCREATOR", Jurisdiction.getName());
				    pdMxxx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
				    pdMxxx.put("RDLAST_MODIFIER", Jurisdiction.getName());
				    pdMxxx.put("RDTYPE", "自动");
				    pdMxxx.put("RDSTATE", "已结束");
				    pdMxxx.put("RDVISIBLE", "1");
				    pdMxxx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID")); 
				    pdMxxx.put("RDSTARTTIME", closeTime_End); 
				    pdMxxx.put("RDENDTIME", RDENDTIME);
				    pdMxxx.put("RDISOVER", "是");
				    pdMxxx.put("RDHOUR", df.format((sdf.parse(RDENDTIME).getTime()-sdf.parse(closeTime_End).getTime())*1.0/dd));
				    rundetailService.save(pdMxxx);
				   }else if(sdf.parse(RDSTARTTIME).getTime()>sdf.parse(closeTime_Start).getTime() && sdf.parse(RDENDTIME).getTime()>sdf.parse(startTime_End).getTime() && sdf.parse(RDENDTIME).getTime()<=sdf.parse(closeTime_End).getTime())  {
					   	pdZ.put("RDENDTIME", startTime_End); 
					    pdZ.put("RDHOUR", df.format((sdf.parse(startTime_End).getTime()-sdf.parse(RDSTARTTIME).getTime())*1.0/dd));
					    rundetailService.editEndTime(pdZ);
					    //插入结束日期正常工时
					    PageData pdMxx = new PageData();
					    pdMxx.put("RUNDETAIL_ID", this.get32UUID()); //主键
					    pdMxx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
					    pdMxx.put("RDCREATOR", Jurisdiction.getName());
					    pdMxx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
					    pdMxx.put("RDLAST_MODIFIER", Jurisdiction.getName());
					    pdMxx.put("RDTYPE", "自动");
					    pdMxx.put("RDSTATE", "已结束");
					    pdMxx.put("RDVISIBLE", "1");
					    pdMxx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID")); 
					    pdMxx.put("RDSTARTTIME", startTime_End); 
					    pdMxx.put("RDENDTIME", RDENDTIME);
					    pdMxx.put("RDISOVER", "否");
					    pdMxx.put("RDHOUR", df.format((sdf.parse(RDENDTIME).getTime()-sdf.parse(startTime_End).getTime())*1.0/dd));
					    rundetailService.save(pdMxx);
				   }else if(sdf.parse(RDSTARTTIME).getTime()>sdf.parse(closeTime_Start).getTime() && sdf.parse(RDENDTIME).getTime()<=sdf.parse(startTime_End).getTime())  {
						pdZ.put("RDENDTIME", RDENDTIME); 
					    pdZ.put("RDHOUR", df.format((sdf.parse(RDENDTIME).getTime()-sdf.parse(RDSTARTTIME).getTime())*1.0/dd));
					    rundetailService.editEndTime(pdZ);
					   
				   }
				
			}
			*/
			
			else if(differentDays(sdf.parse(RDSTARTTIME),sdf.parse(RDENDTIME))>=1){//2、开始结束日期差几天
				String RDSTARTTIMEx=RDSTARTTIME;
				String RDENDTIMEx=RDENDTIME;
				int difff=differentDays(sdf.parse(RDSTARTTIME),sdf.parse(RDENDTIME));
				for(int i=1;i<=difff;i++) {
					if(i==1 && i<difff) {
						RDSTARTTIMEx=RDSTARTTIME;
						RDENDTIMEx=sdfdate.format(sdfdate.parse(getAddDate(RDSTARTTIME,1)))+" 16:30";
						
					}else if(i==1 && i==difff) {
						RDSTARTTIMEx=RDSTARTTIME;
						RDENDTIMEx=RDENDTIME;
						
					}else if(i>1 && i<difff) {
						RDSTARTTIMEx=RDENDTIMEx;
						RDENDTIMEx=sdfdate.format(sdfdate.parse(getAddDate(RDENDTIMEx,1)))+" 16:30";
						
					}else if(i>1 && i==difff) {
						RDSTARTTIMEx=RDENDTIMEx;
						RDENDTIMEx=RDENDTIME;
						
					}
					jusuan(RDSTARTTIMEx,RDENDTIMEx,pd);
					
				}
			}
			
			
			
		}
		pdMain.put("FUPTYPE", "TASK");
		rundetailService.editActual(pdMain);//更新任务表实际时间、实际总工时、正常工时、加班工时、完成进度、当前计划状态
		
		pdMain.put("FUPTYPE", "STAFFPLAN");
		rundetailService.editActual(pdMain);//更新人员计划表实际时间、实际总工时、正常工时、加班工时、当前计划状态、执行状态
		
		pdMain.put("FUPTYPE", "DEPTPLAN");
		rundetailService.editActual(pdMain);//更新部门计划表实际时间、实际总工时、正常工时、加班工时、当前计划状态、执行状态
		
		pdMain.put("FUPTYPE", "PROPLAN");
		rundetailService.editActual(pdMain);//更新部门计划表实际时间、实际总工时、正常工时、加班工时、当前计划状态、执行状态
		map.put("result", errInfo);
		return map;
	}


private void jusuan(String RDSTARTTIME,String RDENDTIME,PageData pd) throws Exception {
	PageData pdMain = projecttaskService.findById(pd);	//根据ID读取
	SimpleDateFormat sdfdate  =new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat sdf  =new SimpleDateFormat("yyyy-MM-dd HH:mm");
	java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00");  
	double dd=60*60*1000;
	String closeTime_End=sdfdate.format(sdfdate.parse(RDENDTIME))+" 16:30";//下班时间
	String startTime_End=sdfdate.format(sdfdate.parse(RDENDTIME))+" 08:00";//上班时间
	String closeTime_Start=sdfdate.format(sdfdate.parse(RDSTARTTIME))+" 16:30";//下班时间
	String startTime_Start=sdfdate.format(sdfdate.parse(RDSTARTTIME))+" 08:00";//上班时间
	PageData pdZ=new PageData();
	pdZ.put("RDPROJECTTASK_ID", pd.getString("PROJECTTASK_ID"));	
	pdZ.put("RDSTATE", "已结束");
	pdZ.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
	pdZ.put("RDLAST_MODIFIER", Jurisdiction.getName());
	if(sdf.parse(RDSTARTTIME).getTime()<=sdf.parse(closeTime_Start).getTime() && sdf.parse(RDENDTIME).getTime()>sdf.parse(startTime_End).getTime() && sdf.parse(RDENDTIME).getTime()>sdf.parse(closeTime_End).getTime()) {
	    pdZ.put("RDENDTIME", closeTime_Start); 
	    pdZ.put("RDHOUR", df.format((sdf.parse(closeTime_Start).getTime()-sdf.parse(RDSTARTTIME).getTime())*1.0/dd));
	    rundetailService.editEndTime(pdZ);
	    //插入开始日期加班工时
	    PageData pdMx = new PageData();
	    pdMx.put("RUNDETAIL_ID", this.get32UUID()); //主键
	    pdMx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
	    pdMx.put("RDCREATOR", Jurisdiction.getName());
	    pdMx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
	    pdMx.put("RDLAST_MODIFIER", Jurisdiction.getName());
	    pdMx.put("RDTYPE", "自动");
	    pdMx.put("RDSTATE", "已结束");
	    pdMx.put("RDVISIBLE", "1");
	    pdMx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID")); 
	    pdMx.put("RDSTARTTIME", closeTime_Start); 
	    pdMx.put("RDENDTIME", startTime_End);
	    pdMx.put("RDISOVER", "是");
	    pdMx.put("RDHOUR", df.format((sdf.parse(startTime_End).getTime()-sdf.parse(closeTime_Start).getTime())*1.0/dd));
	    rundetailService.save(pdMx);
	    //插入结束日期正常工时
	    PageData pdMxx = new PageData();
	    pdMxx.put("RUNDETAIL_ID", this.get32UUID()); //主键
	    pdMxx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
	    pdMxx.put("RDCREATOR", Jurisdiction.getName());
	    pdMxx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
	    pdMxx.put("RDLAST_MODIFIER", Jurisdiction.getName());
	    pdMxx.put("RDTYPE", "自动");
	    pdMxx.put("RDSTATE", "已结束");
	    pdMxx.put("RDVISIBLE", "1");
	    pdMxx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID")); 
	    pdMxx.put("RDSTARTTIME", startTime_End); 
	    pdMxx.put("RDENDTIME", closeTime_End);
	    pdMxx.put("RDISOVER", "否");
	    pdMxx.put("RDHOUR", df.format((sdf.parse(closeTime_End).getTime()-sdf.parse(startTime_End).getTime())*1.0/dd));
	    rundetailService.save(pdMxx);
	    //插入结束日期加班工时
	    PageData pdMxxx = new PageData();
	    pdMxxx.put("RUNDETAIL_ID", this.get32UUID()); //主键
	    pdMxxx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
	    pdMxxx.put("RDCREATOR", Jurisdiction.getName());
	    pdMxxx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
	    pdMxxx.put("RDLAST_MODIFIER", Jurisdiction.getName());
	    pdMxxx.put("RDTYPE", "自动");
	    pdMxxx.put("RDSTATE", "已结束");
	    pdMxxx.put("RDVISIBLE", "1");
	    pdMxxx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID")); 
	    pdMxxx.put("RDSTARTTIME", closeTime_End); 
	    pdMxxx.put("RDENDTIME", RDENDTIME);
	    pdMxxx.put("RDISOVER", "是");
	    pdMxxx.put("RDHOUR", df.format((sdf.parse(RDENDTIME).getTime()-sdf.parse(closeTime_End).getTime())*1.0/dd));
	    rundetailService.save(pdMxxx);
	   }else if(sdf.parse(RDSTARTTIME).getTime()<=sdf.parse(closeTime_Start).getTime() && sdf.parse(RDENDTIME).getTime()>sdf.parse(startTime_End).getTime() && sdf.parse(RDENDTIME).getTime()<=sdf.parse(closeTime_End).getTime())  {
		    pdZ.put("RDENDTIME", closeTime_Start); 
		    pdZ.put("RDHOUR", df.format((sdf.parse(closeTime_Start).getTime()-sdf.parse(RDSTARTTIME).getTime())*1.0/dd));
		    rundetailService.editEndTime(pdZ);
		    //插入开始日期加班工时
		    PageData pdMx = new PageData();
		    pdMx.put("RUNDETAIL_ID", this.get32UUID()); //主键
		    pdMx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
		    pdMx.put("RDCREATOR", Jurisdiction.getName());
		    pdMx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
		    pdMx.put("RDLAST_MODIFIER", Jurisdiction.getName());
		    pdMx.put("RDTYPE", "自动");
		    pdMx.put("RDSTATE", "已结束");
		    pdMx.put("RDVISIBLE", "1");
		    pdMx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID")); 
		    pdMx.put("RDSTARTTIME", closeTime_Start); 
		    pdMx.put("RDENDTIME", startTime_End);
		    pdMx.put("RDISOVER", "是");
		    pdMx.put("RDHOUR", df.format((sdf.parse(startTime_End).getTime()-sdf.parse(closeTime_Start).getTime())*1.0/dd));
		    rundetailService.save(pdMx);
		  
		    //插入结束日期正常工时
		    PageData pdMxx = new PageData();
		    pdMxx.put("RUNDETAIL_ID", this.get32UUID()); //主键
		    pdMxx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
		    pdMxx.put("RDCREATOR", Jurisdiction.getName());
		    pdMxx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
		    pdMxx.put("RDLAST_MODIFIER", Jurisdiction.getName());
		    pdMxx.put("RDTYPE", "自动");
		    pdMxx.put("RDSTATE", "已结束");
		    pdMxx.put("RDVISIBLE", "1");
		    pdMxx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID")); 
		    pdMxx.put("RDSTARTTIME", startTime_End); 
		    pdMxx.put("RDENDTIME", RDENDTIME);
		    pdMxx.put("RDISOVER", "否");
		    pdMxx.put("RDHOUR", df.format((sdf.parse(RDENDTIME).getTime()-sdf.parse(startTime_End).getTime())*1.0/dd));
		    rundetailService.save(pdMxx);
	   }else if(sdf.parse(RDSTARTTIME).getTime()<=sdf.parse(closeTime_Start).getTime() && sdf.parse(RDENDTIME).getTime()<=sdf.parse(startTime_End).getTime())  {
		   	pdZ.put("RDENDTIME", closeTime_Start); 
		    pdZ.put("RDHOUR", df.format((sdf.parse(closeTime_Start).getTime()-sdf.parse(RDSTARTTIME).getTime())*1.0/dd));
		    rundetailService.editEndTime(pdZ);
		    //插入开始日期加班工时
		    PageData pdMx = new PageData();
		    pdMx.put("RUNDETAIL_ID", this.get32UUID()); //主键
		    pdMx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
		    pdMx.put("RDCREATOR", Jurisdiction.getName());
		    pdMx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
		    pdMx.put("RDLAST_MODIFIER", Jurisdiction.getName());
		    pdMx.put("RDTYPE", "自动");
		    pdMx.put("RDSTATE", "已结束");
		    pdMx.put("RDVISIBLE", "1");
		    pdMx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID")); 
		    pdMx.put("RDSTARTTIME", closeTime_Start); 
		    pdMx.put("RDENDTIME", RDENDTIME);
		    pdMx.put("RDISOVER", "是");
		    pdMx.put("RDHOUR", df.format((sdf.parse(RDENDTIME).getTime()-sdf.parse(closeTime_Start).getTime())*1.0/dd));
		    rundetailService.save(pdMx);
	   }
	 
	   else if(sdf.parse(RDSTARTTIME).getTime()>sdf.parse(closeTime_Start).getTime() && sdf.parse(RDENDTIME).getTime()>sdf.parse(startTime_End).getTime() && sdf.parse(RDENDTIME).getTime()>sdf.parse(closeTime_End).getTime()) {
	    pdZ.put("RDENDTIME", startTime_End); 
	    pdZ.put("RDHOUR", df.format((sdf.parse(startTime_End).getTime()-sdf.parse(RDSTARTTIME).getTime())*1.0/dd));
	    rundetailService.editEndTime(pdZ);
	   
	    //插入结束日期正常工时
	    PageData pdMxx = new PageData();
	    pdMxx.put("RUNDETAIL_ID", this.get32UUID()); //主键
	    pdMxx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
	    pdMxx.put("RDCREATOR", Jurisdiction.getName());
	    pdMxx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
	    pdMxx.put("RDLAST_MODIFIER", Jurisdiction.getName());
	    pdMxx.put("RDTYPE", "自动");
	    pdMxx.put("RDSTATE", "已结束");
	    pdMxx.put("RDVISIBLE", "1");
	    pdMxx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID")); 
	    pdMxx.put("RDSTARTTIME", startTime_End); 
	    pdMxx.put("RDENDTIME", closeTime_End);
	    pdMxx.put("RDISOVER", "否");
	    pdMxx.put("RDHOUR", df.format((sdf.parse(closeTime_End).getTime()-sdf.parse(startTime_End).getTime())*1.0/dd));
	    rundetailService.save(pdMxx);
	    //插入结束日期加班工时
	    PageData pdMxxx = new PageData();
	    pdMxxx.put("RUNDETAIL_ID", this.get32UUID()); //主键
	    pdMxxx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
	    pdMxxx.put("RDCREATOR", Jurisdiction.getName());
	    pdMxxx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
	    pdMxxx.put("RDLAST_MODIFIER", Jurisdiction.getName());
	    pdMxxx.put("RDTYPE", "自动");
	    pdMxxx.put("RDSTATE", "已结束");
	    pdMxxx.put("RDVISIBLE", "1");
	    pdMxxx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID")); 
	    pdMxxx.put("RDSTARTTIME", closeTime_End); 
	    pdMxxx.put("RDENDTIME", RDENDTIME);
	    pdMxxx.put("RDISOVER", "是");
	    pdMxxx.put("RDHOUR", df.format((sdf.parse(RDENDTIME).getTime()-sdf.parse(closeTime_End).getTime())*1.0/dd));
	    rundetailService.save(pdMxxx);
	   }else if(sdf.parse(RDSTARTTIME).getTime()>sdf.parse(closeTime_Start).getTime() && sdf.parse(RDENDTIME).getTime()>sdf.parse(startTime_End).getTime() && sdf.parse(RDENDTIME).getTime()<=sdf.parse(closeTime_End).getTime())  {
		   	pdZ.put("RDENDTIME", startTime_End); 
		    pdZ.put("RDHOUR", df.format((sdf.parse(startTime_End).getTime()-sdf.parse(RDSTARTTIME).getTime())*1.0/dd));
		    rundetailService.editEndTime(pdZ);
		    //插入结束日期正常工时
		    PageData pdMxx = new PageData();
		    pdMxx.put("RUNDETAIL_ID", this.get32UUID()); //主键
		    pdMxx.put("RDCREATE_TIME", Tools.date2Str(new Date()));
		    pdMxx.put("RDCREATOR", Jurisdiction.getName());
		    pdMxx.put("RDLAST_MODIFIED_TIME", Tools.date2Str(new Date()));
		    pdMxx.put("RDLAST_MODIFIER", Jurisdiction.getName());
		    pdMxx.put("RDTYPE", "自动");
		    pdMxx.put("RDSTATE", "已结束");
		    pdMxx.put("RDVISIBLE", "1");
		    pdMxx.put("RDPROJECTTASK_ID", pdMain.getString("PROJECTTASK_ID")); 
		    pdMxx.put("RDSTARTTIME", startTime_End); 
		    pdMxx.put("RDENDTIME", RDENDTIME);
		    pdMxx.put("RDISOVER", "否");
		    pdMxx.put("RDHOUR", df.format((sdf.parse(RDENDTIME).getTime()-sdf.parse(startTime_End).getTime())*1.0/dd));
		    rundetailService.save(pdMxx);
	   }else if(sdf.parse(RDSTARTTIME).getTime()>sdf.parse(closeTime_Start).getTime() && sdf.parse(RDENDTIME).getTime()<=sdf.parse(startTime_End).getTime())  {
			pdZ.put("RDENDTIME", RDENDTIME); 
		    pdZ.put("RDHOUR", df.format((sdf.parse(RDENDTIME).getTime()-sdf.parse(RDSTARTTIME).getTime())*1.0/dd));
		    rundetailService.editEndTime(pdZ);
		   
	   }
	
}

	/**
	 * 判断是否是周末
	 * @return
	 */ 

	private boolean isWeekend(Calendar cal){ 
	    int week=cal.get(Calendar.DAY_OF_WEEK)-1; 
	    if(week ==6 || week==0){//0代表周日，6代表周六 
	        return true; 
	    } 
	    return false; 
	}
	/**
	 * date2比date1多的天数
	 * @param date1 
	 * @param date2
	 * @return 
	 */
	 public static int differentDays(Date date1,Date date2)
	 {
	 Calendar cal1 = Calendar.getInstance();
	 cal1.setTime(date1);
	  
	 Calendar cal2 = Calendar.getInstance();
	 cal2.setTime(date2);
	 int day1= cal1.get(Calendar.DAY_OF_YEAR);
	 int day2 = cal2.get(Calendar.DAY_OF_YEAR);
	  
	 int year1 = cal1.get(Calendar.YEAR);
	 int year2 = cal2.get(Calendar.YEAR);
	 if(year1 != year2) //同一年
	 {
	  int timeDistance = 0 ;
	  for(int i = year1 ; i < year2 ; i ++)
	  {
	  if(i%4==0 && i%100!=0 || i%400==0) //闰年  
	  {
	   timeDistance += 366;
	  }
	  else //不是闰年
	  {
	   timeDistance += 365;
	  }
	  }
	   
	  return timeDistance + (day2-day1) ;
	 }
	 else //不同年
	 {
	  return day2-day1;
	 }
	 }
	 public static String getAddDate(String time,int num) throws ParseException {
			//String time = "2019-6-02 11:05:51";	//当前时间
			//int num = 15;	//加的天数
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date  currdate = format.parse(time);
			Calendar ca = Calendar.getInstance();
		    ca.setTime(currdate);
			ca.add(Calendar.DATE, num);
			currdate = ca.getTime();
			String enddate = format.format(currdate);
			System.out.println("增加天数以后的时间：" + enddate);
			return enddate;
	 }

	/**列表
	 * @param page
	 * @throws Exception
	 */
	@RequestMapping(value="/list")
	//@RequiresPermissions("projecttask:list")
	@ResponseBody
	public Object list(Page page) throws Exception{
		Map<String,Object> map = new HashMap<String,Object>();
		String errInfo = "success";
		PageData pd = new PageData();
		pd = this.getPageData();
		String KEYWORDS = pd.getString("KEYWORDS");						//关键词检索条件
		if(Tools.notEmpty(KEYWORDS))pd.put("KEYWORDS", KEYWORDS.trim());
		//pd.put("ISALL", Jurisdiction.getUserISALL());
		//pd.put("ISBZ", Jurisdiction.getUserSFLD());
		pd.put("Guan", Jurisdiction.getName());
		//pd.put("DEPARTMENT_ID", Jurisdiction.getDEPARTMENT_IDNEW());
		page.setPd(pd);
	
		List<PageData>	varList = projecttaskService.list(page);	//列出PROJECTTASK列表
		map.put("varList", varList);
		map.put("page", page);
		map.put("result", errInfo);
		return map;
	}
	
	 /**去修改页面获取数据
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/goEdit")
	//@RequiresPermissions("projecttask:edit")
	@ResponseBody
	public Object goEdit() throws Exception{
		Map<String,Object> map = new HashMap<String,Object>();
		String errInfo = "success";
		PageData pd = new PageData();
		pd = this.getPageData();
		pd = projecttaskService.findById(pd);	//根据ID读取
		map.put("pd", pd);
		map.put("result", errInfo);
		return map;
	}	
	
	 /**批量删除
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/deleteAll")
	@RequiresPermissions("projecttask:del")
	@ResponseBody
	public Object deleteAll() throws Exception{
		Map<String,Object> map = new HashMap<String,Object>();
		String errInfo = "success";
		PageData pd = new PageData();		
		pd = this.getPageData();
		String DATA_IDS = pd.getString("DATA_IDS");
		if(Tools.notEmpty(DATA_IDS)){
			String ArrayDATA_IDS[] = DATA_IDS.split(",");
			projecttaskService.deleteAll(ArrayDATA_IDS);
			errInfo = "success";
		}else{
			errInfo = "error";
		}
		map.put("result", errInfo);				//返回结果
		return map;
	}
	
	 /**导出到excel
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/excel")
	@RequiresPermissions("toExcel")
	public ModelAndView exportExcel() throws Exception{
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		String KEYWORDS = pd.getString("KEYWORDS");						//关键词检索条件
		if(Tools.notEmpty(KEYWORDS))pd.put("KEYWORDS", KEYWORDS.trim());
		//pd.put("ISALL", Jurisdiction.getUserISALL());
		//pd.put("ISBZ", Jurisdiction.getUserSFLD());
		pd.put("Guan", Jurisdiction.getName());
		//pd.put("DEPARTMENT_ID", Jurisdiction.getDEPARTMENT_IDNEW());
		Map<String,Object> dataMap = new HashMap<String,Object>();
		List<String> titles = new ArrayList<String>();
		titles.add("序号");	//1
		titles.add("项目号");	//2
		titles.add("设备名称");	//3
		titles.add("设备号");	//4
		titles.add("TYPE");	//5
		titles.add("客户名称");	//6
		titles.add("项目阶段");	//7
		titles.add("图样代号");	//8
		titles.add("任务名称");	//9
		titles.add("负责人");	//10
		titles.add("最新计划开始时间");	//11
		titles.add("最新计划结束时间");	//12
		titles.add("实际开始");	//13
		titles.add("实际结束");	//14
		titles.add("完成进度%");	//15
		titles.add("任务工时");	//16
		titles.add("加班工时");	//17
		titles.add("状态");	//18
		titles.add("出厂编码");	//19
		titles.add("有无此项");	//20
		dataMap.put("titles", titles);
		List<PageData> varOList = projecttaskService.listAll(pd);
		List<PageData> varList = new ArrayList<PageData>();
		for(int i=0;i<varOList.size();i++){
			PageData vpd = new PageData();
			vpd.put("var1", Integer.valueOf(i + 1).toString());	    		//1
			vpd.put("var2", varOList.get(i).getString("PPROJECT_CODE"));	//2
			vpd.put("var3", varOList.get(i).getString("EEQM_NAME"));	    //3
			vpd.put("var4", varOList.get(i).getString("EEQM_NO"));	    	//4
			vpd.put("var5", varOList.get(i).getString("ETYPE"));	    	//5
			vpd.put("var6", varOList.get(i).getString("ECUSTOMER_NAME"));	//6
			vpd.put("var7", varOList.get(i).getString("PTPHASENAME"));	    //7
			vpd.put("var8", varOList.get(i).getString("YL2"));	    		//8
			vpd.put("var9", varOList.get(i).getString("PTTASK_NAME"));	    //9
			vpd.put("var10", varOList.get(i).getString("PTPRINCIPAL"));	    //10
			vpd.put("var11", varOList.get(i).getString("PTPLANSTARTDATE"));	//11
			vpd.put("var12", varOList.get(i).get("PTPLANENDDATE"));	//12
			vpd.put("var13", varOList.get(i).getString("PTACTUALSTARTDATE"));	//13
			vpd.put("var14", varOList.get(i).getString("PTACTUALENDDATE"));	    //14
			vpd.put("var15", varOList.get(i).containsKey("PTFINISHPROGRESS")?varOList.get(i).get("PTFINISHPROGRESS").toString():"");	//15
			vpd.put("var16", varOList.get(i).containsKey("PTNORMAL_HOUR")?varOList.get(i).get("PTNORMAL_HOUR").toString():"");			//16
			vpd.put("var17", varOList.get(i).containsKey("PTOVER_HOUR")?varOList.get(i).get("PTOVER_HOUR").toString():"");				//17
			vpd.put("var18", varOList.get(i).getString("PTRUNSTATE"));	    	//18
			vpd.put("var19", varOList.get(i).getString("OUT_BIANMA"));	    	//19
			vpd.put("var20", varOList.get(i).getString("YL3"));	    			//20
			varList.add(vpd);
		}
		dataMap.put("varList", varList);
		ObjectExcelView erv = new ObjectExcelView();
		mv = new ModelAndView(erv,dataMap);
		return mv;
	}
	
}
