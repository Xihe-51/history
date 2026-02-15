package com.ailab.back.controller;

import com.ailab.back.service.DataService;
import com.ailab.back.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/data")
public class DataController {
    @Autowired
    private DataService dataService;

    @PostMapping("/backup")
    ResultVO<?> backupData() { return dataService.backupData(); };
    @PostMapping("/recover")
    ResultVO<?> recoverData(@RequestParam String filename) { return dataService.recoverData(filename); }
    @GetMapping("/list")
    ResultVO<?> getFileList() { return dataService.getFileList(); }
    @DeleteMapping("/delete")
    ResultVO<?> deleteData(@RequestParam String filename) { return dataService.deleteData(filename); };
}
