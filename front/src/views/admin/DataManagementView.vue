<template>
    <div class="backup-layout">
        <!-- 顶部操作栏 -->
        <div class="backup-header">
            <h2 class="backup-title">数据管理</h2>
            <el-button type="primary" @click="backup" class="backup-btn">
                <el-icon>
                    <Plus />
                </el-icon>
                <span>新增备份</span>
            </el-button>
        </div>

        <!-- 表格区域 -->
        <div class="backup-table-container">
            <el-table :data="formattedTableData" style="width: 100%" :header-cell-style="headerCellStyle"
                :cell-style="{ 'text-align': 'center' }" stripe :hover-row-class-name="'table-row-hover'">
                <el-table-column prop="createTime" label="备份时间"></el-table-column>
                <el-table-column prop="fileSize" label="备份文件大小">
                    <template #default="scope">
                        {{ scope.row.fileSize }} KB
                    </template>
                </el-table-column>
                <el-table-column label="操作">
                    <template #default="{ row }">
                        <el-button type="success" size="small" @click="recover(row)" class="operate-btn recover-btn" :loading="recovering">
                            <el-icon>
                                <RefreshLeft />
                            </el-icon>
                            <span>恢复</span>
                        </el-button>
                        <el-button type="danger" size="small" @click="del(row)" class="operate-btn delete-btn">
                            <el-icon>
                                <Delete />
                            </el-icon>
                            <span>删除</span>
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 空数据提示 -->
            <div v-if="formattedTableData.length === 0" class="empty-tip">
                <el-empty description="暂无备份数据"></el-empty>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { getFileList, recoverData, backupData, deleteData } from '@/api/data';
import { ElMessage, ElEmpty } from 'element-plus';

// 添加类型定义
interface BackupItem {
    fileName: string;
    fileSize: number;
    createTime: string;
}

const recovering = ref(false);
const tableData = ref<BackupItem[]>([]);  // 添加泛型

// 时间格式化工具函数
const formatDateTime = (dateStr: string) => {
    if (!dateStr) return '';
    try {
        const date = new Date(dateStr);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    } catch (error) {
        console.error('时间格式化失败:', error);
        return dateStr;
    }
};

// 格式化表格数据 - 修复类型错误
const formattedTableData = computed(() => {
    return tableData.value.map((item: BackupItem) => ({
        ...item,
        fileSize: item.fileSize ? (item.fileSize / 1024).toFixed(2) : '0.00',
        createTime: formatDateTime(item.createTime)
    }));
});

// 表格头部样式
const headerCellStyle = {
    'text-align': 'center',
    'background-color': '#FFF9E6',
    'color': '#D4A017',
    'font-weight': 'bold'
};

const fetchData = async () => {
    try {
        const res = await getFileList();
        console.log(res.data);
        if (res.data.code === 200) {
            tableData.value = res.data.data;
        } else {
            ElMessage.error('获取数据失败');
        }
    } catch (error) {
        ElMessage.error('网络异常，获取数据失败');
        console.error('获取备份列表失败:', error);
    }
}

const backup = async () => {
    try {
        const res = await backupData();
        if (res.data.code === 200) {
            ElMessage.success('备份成功');
            fetchData();
        } else {
            ElMessage.error('备份失败');
        }
    } catch (error) {
        ElMessage.error('网络异常，备份失败');
        console.error('备份失败:', error);
    }
}

const recover = async (row: any) => {
    try {
        recovering.value = true;
        const res = await recoverData(row.fileName);
        if (res.data.code === 200) {
            ElMessage.success('恢复成功');
        } else {
            ElMessage.error('恢复失败');
        }
    } catch (error) {
        ElMessage.error('网络异常，恢复失败');
        console.error('恢复失败:', error);
    } finally {
        recovering.value = false;
    }
}

const del = async (row: any) => {
    try {
        console.log(row.fileName);
        const res = await deleteData(row.fileName);
        console.log(res.data);
        if (res.data.code === 200) {
            ElMessage.success('删除成功');
            fetchData();
        } else {
            ElMessage.error('删除失败: ' + (res.data.mes || '未知错误'));
        }
    } catch (error) {
        ElMessage.error('网络异常，删除失败');
        console.error('删除失败:', error);
    }
}

// 生命周期
onMounted(() => {
    fetchData();
});
</script>
<style scoped>
/* 整体布局 */
.backup-layout {
    padding: 0 20px;
}

/* 头部区域 */
.backup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 2px solid #FFE888;
}

.backup-title {
    color: #D4A017;
    font-size: 20px;
    margin: 0;
}

/* 新增备份按钮 */
.backup-btn {
    background-color: #FFC107;
    border-color: #FFC107;
    color: #333;
    font-weight: 500;
    padding: 8px 20px;
}

.backup-btn:hover {
    background-color: #FFB300;
    border-color: #FFB300;
}

/* 表格容器 */
.backup-table-container {
    background-color: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(255, 200, 50, 0.1);
}

/* 表格行悬停样式 */
:deep(.table-row-hover) {
    background-color: #FFF9E6 !important;
}

/* 操作按钮样式 */
.operate-btn {
    margin: 0 4px;
}

.recover-btn {
    background-color: #9ACD32;
    border-color: #9ACD32;
}

.recover-btn:hover {
    background-color: #8FBC8F;
    border-color: #8FBC8F;
}

.delete-btn {
    background-color: #FF6347;
    border-color: #FF6347;
}

.delete-btn:hover {
    background-color: #FF4500;
    border-color: #FF4500;
}

/* 空数据提示 */
.empty-tip {
    padding: 40px 0;
    text-align: center;
}

/* 表格单元格内边距优化 */
:deep(.el-table td),
:deep(.el-table th) {
    padding: 12px 0;
}
</style>