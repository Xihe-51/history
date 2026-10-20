package com.ailab.back.utils;
import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel 读取工具。
 */
public class ExcelUtils {

    /**
     * 读取第一个工作表的每一行（跳过表头），返回 列序号 -> 单元格文本。
     *
     * <p>修复点：
     * <ul>
     *   <li>数值单元格按“整数”输出，避免年份 960 被读成 "960.0"；</li>
     *   <li>列数按行内最大列计算，短行缺失的列补空串，下游无需再判空。</li>
     * </ul>
     */
    public static List<Map<Integer, String>> readExcel(Workbook workbook) {
        List<Map<Integer, String>> list = new ArrayList<>();
        if (workbook == null || workbook.getNumberOfSheets() == 0) {
            return list;
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return list;
        }
        int maxCols = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                maxCols = Math.max(maxCols, row.getLastCellNum());
            }
        }
        if (maxCols <= 0) {
            return list;
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            Map<Integer, String> map = new HashMap<>();
            boolean allBlank = true;
            for (int j = 0; j < maxCols; j++) {
                Cell cell = row.getCell(j);
                String value = cellToString(cell);
                if (!value.isEmpty()) {
                    allBlank = false;
                }
                map.put(j, value);
            }
            // 整行为空则跳过
            if (!allBlank) {
                list.add(map);
            }
        }
        return list;
    }

    private static String cellToString(Cell cell) {
        if (cell == null) {
            return "";
        }
        CellType type = cell.getCellType() == CellType.FORMULA
                ? cell.getCachedFormulaResultType()
                : cell.getCellType();
        switch (type) {
            case NUMERIC:
                double d = cell.getNumericCellValue();
                if (d == Math.rint(d) && !Double.isInfinite(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case STRING:
                return cell.getStringCellValue().trim();
            default:
                return "";
        }
    }
}
