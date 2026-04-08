package com.ailab.back.service;

import com.ailab.back.vo.ResultVO;

public interface DataService {
    // 备份数据库
    ResultVO<?> backupData();
    // 根据文件名恢复备份数据库
    ResultVO<?> recoverData(String filename);

    // 根据文件名删除备份文件
    ResultVO<?> deleteData(String filename);
    // 获取db目录下的符合命名要求的sql文件名及文件大小
    ResultVO<?> getFileList();
}
