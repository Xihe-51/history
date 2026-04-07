package com.ailab.back.service.impl;

import com.ailab.back.Entity.*;
import com.ailab.back.service.*;
import com.ailab.back.utils.ExcelUtils;
import com.ailab.back.vo.ResultIM;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
//依赖注入
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {
    //年代，典故，阶段，事件，人物
    private final DynastyService dynastyService;
    private final AllusionService allusionService;
    private final DynastyStageService dynastyStageService;
    private final HistoricalEventService historicalEventService;
    private final HistoricalPersonService historicalPersonService;
    @Override
    //Excel文件，数据类型，朝代ID
    public ResultIM importResult(MultipartFile file, String dataType, Long dynastyId) {
        //返回前端
        ResultIM resultIM=new ResultIM();
        //成功计数，失败存错误信息
        //汇总错误信息
        //处理文件情况，处理失败情况
        try {
             //读excel文件
            Workbook workbook= WorkbookFactory.create(file.getInputStream());
            //读表
            List<Map<Integer,String>>rows= ExcelUtils.readExcel(workbook);
            //跳过表头
            int rowNum=1;
            for(Map<Integer,String>row:rows){
                rowNum++;
                try{
                    switch (dataType){
                        case "dynasty":
                            importDynasty(row);
                            break;

                        case "allusion":
                            importAllusion(row);
                            break;
                        case "dynastyStage":
                            ImportDynastyStage(row,dynastyId);
                            break;
                        case "historicalEvent":
                            ImportHistoricalEvent(row,dynastyId);
                            break;
                        case "historicalPerson":
                            ImportHistoricalPerson(row,dynastyId);
                            break;
                        default:
                            throw new RuntimeException("dataType:"+dataType+"不合法");
                    }
                    resultIM.setSuccessCount(resultIM.getSuccessCount()+1);
                } catch (Exception e) {
                    resultIM.setFailCount(resultIM.getFailCount()+1);
                    //封装一个错误信息对象
                    ResultIM.FailDetail failDetail=new ResultIM.FailDetail();

                    failDetail.setRow(rowNum);
                    failDetail.setReason(e.getMessage());
                    //返回详细错误信列表
                    resultIM.getFailDetailList().add(failDetail);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Excel 解析失败："+e.getMessage());
        }
        return resultIM;
    }
    //年代
    public void importDynasty(Map<Integer,String> row){
        //取不为空的字段数据
        String name=row.get(0);
        String startYear=row.get(1);
        String endYear=row.get(2);

        if(name.isEmpty()){
            throw new RuntimeException("朝代名称不能为空");
        }
        if(startYear.isEmpty()){
            throw new RuntimeException("起始时间不能为空");
        }
        if(endYear.isEmpty()){
            throw new RuntimeException("结束时间不能为空");
        }
        //封装一个对象
        Dynasty dynasty=new Dynasty();
        //存入数据库，Mybatis-Plus自动执行sql语句
        dynasty.setName(name);
        dynasty.setStartYear(Integer.parseInt(startYear));
        dynasty.setEndYear(Integer.parseInt(endYear));
        //时间如何操作
        //参考bean实体类的时间注解使用
        //通用包下的自动填充处理器
        dynastyService.save(dynasty);
    }
    // 典故
public  void importAllusion(Map<Integer,String> row){
    String allusionName=row.get(0);
    String themeType=row.get(1);
    if(allusionName.isEmpty()){
        throw new RuntimeException("典故名称不能为空");
    }
    if(themeType.isEmpty()){
        throw new RuntimeException("典故主题类型不能为空");
    }
    Allusion allusion=new Allusion();
    allusion.setAllusionName(allusionName);
    allusion.setThemeType(themeType);

    allusionService.save(allusion);
}
//有dynastyId的类型有
    //阶段
    //事件
    //人物

    // 阶段
    public void ImportDynastyStage(Map<Integer,String> row,Long dynastyId){
        String stageName=row.get(0);
        String startYear=row.get(1);
        String endYear=row.get(2);

        if(stageName.isEmpty()){
            throw new RuntimeException("阶段名称不能为空");
        }
        if(startYear.isEmpty()){
            throw new RuntimeException("阶段起始时间不能为空");
        }
        if(endYear.isEmpty()){
            throw  new RuntimeException("阶段结束时间不能为空");
        }

        DynastyStage dynastyStage=new DynastyStage();
        //dynastyId必添
        dynastyStage.setDynastyId(dynastyId);
        dynastyStage.setStageName(stageName);
        dynastyStage.setStartYear(Integer.parseInt(startYear));
        dynastyStage.setEndYear(Integer.parseInt(endYear));

        dynastyStageService.save(dynastyStage);
    }
    // 事件
    public void ImportHistoricalEvent(Map<Integer,String> row,Long dynastyId){
        String eventName=row.get(0);
        String eventYear=row.get(1);
        String eventType=row.get(5);

        if(eventName.isEmpty()){
            throw new RuntimeException("事件名称不能为空");
        }
        if(eventYear.isEmpty()){
            throw new RuntimeException("事件年份不能为空");
        }
        if(eventType.isEmpty()){
            throw new RuntimeException("事件结束时间不能为空");
        }
        HistoricalEvent historicalEvent=new HistoricalEvent();

        historicalEvent.setDynastyId(dynastyId);
        historicalEvent.setEventName(eventName);
        historicalEvent.setEventYear(Integer.parseInt(eventYear));
        historicalEvent.setEventType(eventType);

        historicalEventService.save(historicalEvent);
    }
    // 人物
    public void ImportHistoricalPerson(Map<Integer,String> row,Long dynastyId){
        String personName=row.get(0);
        String identityType=row.get(3);

        if(personName.isEmpty()){
            throw new RuntimeException("人物名称不能为空");
        }
        if(identityType.isEmpty()){
            throw new RuntimeException("人物身份类型不能为空");
        }
        HistoricalPerson historicalPerson=new HistoricalPerson();
        historicalPerson.setDynastyId(dynastyId);
        historicalPerson.setPersonName(personName);
        historicalPerson.setIdentityType(identityType);

        historicalPersonService.save(historicalPerson);
    }
}
