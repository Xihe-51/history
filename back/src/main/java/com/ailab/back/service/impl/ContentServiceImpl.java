package com.ailab.back.service.impl;

import com.ailab.back.Entity.*;
import com.ailab.back.service.*;
import com.ailab.back.utils.ExcelUtils;
import com.ailab.back.vo.ResultIM;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Excel 批量导入。
 *
 * <p>列定义（第 1 行为表头，从第 2 行开始读取，全部按 0 起始的列序号）：
 * <table>
 *   <tr><th>dataType</th><th>列顺序</th></tr>
 *   <tr><td>dynasty</td><td>0 朝代名称, 1 起始年份, 2 结束年份, 3 都城, 4 开国者, 5 简介</td></tr>
 *   <tr><td>dynastyStage</td><td>0 阶段名称, 1 起始年份, 2 结束年份, 3 阶段描述</td></tr>
 *   <tr><td>historicalEvent</td><td>0 事件名称, 1 事件年份, 2 背景, 3 过程, 4 影响, 5 事件类型</td></tr>
 *   <tr><td>historicalPerson</td><td>0 人物姓名, 1 出生年份, 2 去世年份, 3 身份类型, 4 标签(逗号分隔), 5 生平</td></tr>
 *   <tr><td>allusion</td><td>0 典故名称, 1 主题类型, 2 出处, 3 原文, 4 释义, 5 寓意</td></tr>
 * </table>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final DynastyService dynastyService;
    private final AllusionService allusionService;
    private final DynastyStageService dynastyStageService;
    private final HistoricalEventService historicalEventService;
    private final HistoricalPersonService historicalPersonService;

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            "dynasty", "allusion", "dynastyStage", "historicalEvent", "historicalPerson");

    /** 需要 dynastyId 才能导入的类型 */
    private static final Set<String> NEED_DYNASTY_ID = Set.of(
            "dynastyStage", "historicalEvent", "historicalPerson");

    @Override
    public ResultIM importResult(MultipartFile file, String dataType, Long dynastyId) {
        ResultIM resultIM = new ResultIM();
        if (!SUPPORTED_TYPES.contains(dataType)) {
            throw new IllegalArgumentException("dataType 不合法：" + dataType
                    + "，可选值：" + String.join("/", SUPPORTED_TYPES));
        }
        if (NEED_DYNASTY_ID.contains(dataType)) {
            if (dynastyId == null) {
                throw new IllegalArgumentException("导入 " + dataType + " 时必须提供 dynastyId");
            }
            if (dynastyService.getById(dynastyId) == null) {
                throw new IllegalArgumentException("朝代ID不存在：" + dynastyId);
            }
        }

        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            List<Map<Integer, String>> rows = ExcelUtils.readExcel(workbook);
            if (rows.isEmpty()) {
                throw new IllegalArgumentException("Excel 中没有可导入的数据（第 1 行为表头，从第 2 行开始读取）");
            }

            int rowNum = 1; // 表头占第 1 行
            for (Map<Integer, String> row : rows) {
                rowNum++;
                try {
                    switch (dataType) {
                        case "dynasty" -> importDynasty(row);
                        case "allusion" -> importAllusion(row);
                        case "dynastyStage" -> importDynastyStage(row, dynastyId);
                        case "historicalEvent" -> importHistoricalEvent(row, dynastyId);
                        case "historicalPerson" -> importHistoricalPerson(row, dynastyId);
                        default -> throw new IllegalArgumentException("dataType 不合法：" + dataType);
                    }
                    resultIM.setSuccessCount(resultIM.getSuccessCount() + 1);
                } catch (Exception e) {
                    resultIM.setFailCount(resultIM.getFailCount() + 1);
                    ResultIM.FailDetail failDetail = new ResultIM.FailDetail();
                    failDetail.setRow(rowNum);
                    failDetail.setReason(e.getMessage() == null ? "未知错误" : e.getMessage());
                    resultIM.getFailDetailList().add(failDetail);
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Excel 解析失败", e);
            throw new IllegalArgumentException("Excel 解析失败，请确认文件为 .xlsx/.xls 格式且未损坏");
        }
        return resultIM;
    }

    // ==================== 各类型导入 ====================

    private void importDynasty(Map<Integer, String> row) {
        String name = cell(row, 0);
        requireText(name, "朝代名称不能为空");
        Integer startYear = requireInt(cell(row, 1), "起始年份不能为空且必须为整数");
        Integer endYear = requireInt(cell(row, 2), "结束年份不能为空且必须为整数");
        if (startYear > endYear) {
            throw new IllegalArgumentException("起始年份不能晚于结束年份");
        }

        Dynasty dynasty = new Dynasty();
        dynasty.setName(name);
        dynasty.setStartYear(startYear);
        dynasty.setEndYear(endYear);
        dynasty.setCapital(emptyToNull(cell(row, 3)));
        dynasty.setFounder(emptyToNull(cell(row, 4)));
        dynasty.setDynastyDesc(emptyToNull(cell(row, 5)));
        dynastyService.save(dynasty);
    }

    private void importAllusion(Map<Integer, String> row) {
        String allusionName = cell(row, 0);
        requireText(allusionName, "典故名称不能为空");
        String themeType = cell(row, 1);

        Allusion allusion = new Allusion();
        allusion.setAllusionName(allusionName);
        allusion.setThemeType(StringUtils.hasText(themeType) ? themeType : "其他");
        allusion.setSource(emptyToNull(cell(row, 2)));
        allusion.setOriginalText(emptyToNull(cell(row, 3)));
        allusion.setParaphrase(emptyToNull(cell(row, 4)));
        allusion.setMoral(emptyToNull(cell(row, 5)));
        allusionService.save(allusion);
    }

    private void importDynastyStage(Map<Integer, String> row, Long dynastyId) {
        String stageName = cell(row, 0);
        requireText(stageName, "阶段名称不能为空");
        Integer startYear = requireInt(cell(row, 1), "阶段起始年份不能为空且必须为整数");
        Integer endYear = requireInt(cell(row, 2), "阶段结束年份不能为空且必须为整数");

        DynastyStage stage = new DynastyStage();
        stage.setDynastyId(dynastyId);
        stage.setStageName(stageName);
        stage.setStartYear(startYear);
        stage.setEndYear(endYear);
        stage.setStageDesc(emptyToNull(cell(row, 3)));
        dynastyStageService.save(stage);
    }

    private void importHistoricalEvent(Map<Integer, String> row, Long dynastyId) {
        String eventName = cell(row, 0);
        requireText(eventName, "事件名称不能为空");
        Integer eventYear = requireInt(cell(row, 1), "事件年份不能为空且必须为整数");
        String eventType = cell(row, 5);

        HistoricalEvent event = new HistoricalEvent();
        event.setDynastyId(dynastyId);
        event.setEventName(eventName);
        event.setEventYear(eventYear);
        event.setEventBg(emptyToNull(cell(row, 2)));
        event.setEventProcess(emptyToNull(cell(row, 3)));
        event.setEventImpact(emptyToNull(cell(row, 4)));
        event.setEventType(StringUtils.hasText(eventType) ? eventType : "其他");
        historicalEventService.save(event);
    }

    private void importHistoricalPerson(Map<Integer, String> row, Long dynastyId) {
        String personName = cell(row, 0);
        requireText(personName, "人物姓名不能为空");
        String identityType = cell(row, 3);
        requireText(identityType, "人物身份类型不能为空");

        Integer birthYear = optionalInt(cell(row, 1), "出生年份格式错误");
        Integer deathYear = optionalInt(cell(row, 2), "去世年份格式错误");
        if (birthYear != null && deathYear != null && birthYear > deathYear) {
            throw new IllegalArgumentException("出生年份不能晚于去世年份");
        }

        HistoricalPerson person = new HistoricalPerson();
        person.setDynastyId(dynastyId);
        person.setPersonName(personName);
        person.setBirthYear(birthYear);
        person.setDeathYear(deathYear);
        person.setIdentityType(identityType);
        person.setTags(parseTags(cell(row, 4)));
        person.setPersonBio(emptyToNull(cell(row, 5)));
        historicalPersonService.save(person);
    }

    // ==================== 工具方法 ====================

    private static String cell(Map<Integer, String> row, int index) {
        String v = row.get(index);
        return v == null ? "" : v.trim();
    }

    private static void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private static Integer requireInt(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        try {
            // 兼容 Excel 中出现的 -221.0
            if (value.endsWith(".0")) {
                value = value.substring(0, value.length() - 2);
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message + "（当前值：" + value + "）");
        }
    }

    private static Integer optionalInt(String value, String message) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return requireInt(value, message);
    }

    private static String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }

    private static List<String> parseTags(String value) {
        if (!StringUtils.hasText(value)) {
            return new ArrayList<>();
        }
        String normalized = value.replace('，', ',').replace('、', ',');
        return Arrays.stream(normalized.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
