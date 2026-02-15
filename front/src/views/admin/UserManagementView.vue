<template>
    <div class="user-management-container">
        <!-- 页面标题和添加按钮 -->
        <div class="page-header">
            <h2>用户管理</h2>
            <el-button type="primary" class="add-btn" @click="handleAdd">
                <el-icon>
                    <plus />
                </el-icon>
                <span>添加用户</span>
            </el-button>
        </div>

        <!-- 查询表单 -->
        <div class="search-form">
            <el-form :inline="true" :model="searchForm">
                <el-form-item label="用户名">
                    <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable></el-input>
                </el-form-item>
                <el-form-item label="角色">
                    <el-select style="width: 120px;" v-model="searchForm.role" placeholder="请选择" clearable>
                        <el-option label="管理员" value="admin"></el-option>
                        <el-option label="普通用户" value="user"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="状态">
                    <el-select style="width: 100px;" v-model="searchForm.status" placeholder="请选择" clearable>
                        <el-option label="正常" value="1"></el-option>
                        <el-option label="禁用" value="0"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="getUserListData" class="search-btn">
                        <el-icon>
                            <search />
                        </el-icon>
                        <span>查询</span>
                    </el-button>
                    <el-button @click="resetSearchForm">
                        <el-icon>
                            <refresh />
                        </el-icon>
                        <span>重置</span>
                    </el-button>
                </el-form-item>
            </el-form>
        </div>

        <!-- 用户列表 -->
        <div class="user-table">
            <el-table :data="userList" stripe v-loading="loading" style="width: 100%"
                :header-cell-style="{ 'text-align': 'center' }" :cell-style="{ 'text-align': 'center' }">
                <el-table-column prop="id" label="UID" width="80"></el-table-column>
                <el-table-column label="头像" width="80">
                    <template #default="scope">
                        <el-avatar :src="BASE_URL + scope.row.avatarPath" :size="40"></el-avatar>
                    </template>
                </el-table-column>
                <el-table-column prop="username" label="用户名" width="120"></el-table-column>
                <el-table-column prop="email" label="邮箱" width="180"></el-table-column>
                <el-table-column prop="role" label="角色" width="80">
                    <template #default="scope">
                        <el-tag :type="scope.row.role === 'admin' ? 'warning' : 'success'">
                            {{ scope.row.role === 'admin' ? '管理员' : '普通用户' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="80">
                    <template #default="scope">
                        <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0"
                            @click="handleStatusChange(scope.row)"></el-switch>
                    </template>
                </el-table-column>
                <el-table-column label="最后登录时间" width="180">
                    <template #default="scope">
                        {{ formatIsoDate(scope.row.lastLoginTime) || '从未登录' }}
                    </template>
                </el-table-column>
                <el-table-column label="更新时间" width="180">
                    <template #default="scope">
                        {{ formatIsoDate(scope.row.updatedTime) || '从未更新' }}
                    </template>
                </el-table-column>
                <el-table-column prop="createdTime" label="注册时间" width="180">
                    <template #default="scope">
                        {{ formatIsoDate(scope.row.createdTime) }}
                    </template>
                </el-table-column>
                <el-table-column label="操作">
                    <template #default="scope">
                        <el-button type="primary" size="small" @click="handleEdit(scope.row)" style="gap: 20px;">
                            <el-icon>
                                <edit />
                            </el-icon>
                            <span>编辑</span>
                        </el-button>
                        <el-button type="danger" size="small" @click="handleDelete(scope.row.id)" style="gap: 20px;">
                            <el-icon>
                                <delete />
                            </el-icon>
                            <span>删除</span>
                        </el-button>
                        <el-button size="small" @click="handleResetPassword(scope.row.id)" style="gap: 20px;">
                            <span>重置密码</span>
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页组件 -->
            <div class="pagination">
                <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                    :current-page="searchForm.pageNum" :page-sizes="[10, 20, 50, 100]" :page-size="searchForm.pageSize"
                    layout="total, sizes, prev, pager, next, jumper" :total="total">
                </el-pagination>
            </div>
        </div>

        <!-- 编辑用户弹窗 -->
        <el-dialog title="编辑用户信息" v-model="dialogVisible" width="500px" @close="resetEditForm">
            <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
                <el-form-item label="UID" prop="id">
                    <el-input v-model="editForm.id" disabled></el-input>
                </el-form-item>
                <el-form-item label="用户名" prop="username">
                    <el-input v-model="editForm.username" disabled></el-input>
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="editForm.email"></el-input>
                </el-form-item>
                <el-form-item label="角色" prop="role">
                    <el-select v-model="editForm.role">
                        <el-option label="管理员" value="admin"></el-option>
                        <el-option label="普通用户" value="user"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0"></el-switch>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitEditForm">确定</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 添加用户弹窗 -->
        <el-dialog title="添加新用户" v-model="addDialogVisible" width="500px" @close="resetAddForm">
            <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="80px" style="margin-top: 20px;">
                <el-form-item label="用户名" prop="username">
                    <el-input v-model="addForm.username" placeholder="请输入用户名"></el-input>
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="addForm.email" placeholder="请输入邮箱地址"></el-input>
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input v-model="addForm.password" placeholder="请输入密码" type="password" show-password></el-input>
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input v-model="addForm.confirmPassword" placeholder="请再次输入密码" type="password"
                        show-password></el-input>
                </el-form-item>
                <el-form-item label="角色" prop="role">
                    <el-select v-model="addForm.role" placeholder="请选择角色">
                        <el-option label="管理员" value="admin"></el-option>
                        <el-option label="普通用户" value="user"></el-option>
                    </el-select>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="addDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitAddForm">确定</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { getUserList, updateUserById, deleteUserById, register } from "@/api/user";
import { formatIsoDate } from "@/utils/date";
import { BASE_URL } from "@/utils/request";

// 加载状态
const loading = ref(false);
// 用户列表数据
const userList = ref([]);
// 总条数
const total = ref(0);
// 编辑弹窗显示状态
const dialogVisible = ref(false);
// 添加用户弹窗显示状态
const addDialogVisible = ref(false);
// 表单引用
const editFormRef = ref();
const addFormRef = ref();

// 查询表单
const searchForm = reactive({
    pageNum: 1,
    pageSize: 10,
    role: "",
    status: "",
    username: "",
});

// 编辑表单
const editForm = reactive({
    id: "",
    username: "",
    email: "",
    role: "",
    status: "1",
});

// 添加用户表单
const addForm = reactive({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    role: "user",
});

// 编辑表单校验规则
const editRules = reactive({
    role: [{ required: true, message: "请选择角色", trigger: "change" }],
});

// 添加用户表单校验规则
const addRules = reactive({
    username: [
        { required: true, message: "请输入用户名", trigger: "blur" },
        { min: 3, max: 20, message: "用户名长度在 3 到 20 个字符", trigger: "blur" }
    ],
    password: [
        { required: true, message: "请输入密码", trigger: "blur" },
        { min: 6, max: 20, message: "密码长度在 6 到 20 个字符", trigger: "blur" }
    ],
    confirmPassword: [
        { required: true, message: "请确认密码", trigger: "blur" },
        {
            validator: (rule: any, value: string, callback: Function) => {
                if (value !== addForm.password) {
                    callback(new Error('两次输入的密码不一致!'));
                } else {
                    callback();
                }
            },
            trigger: 'blur'
        }
    ],
    role: [{ required: true, message: "请选择角色", trigger: "change" }],
    email: [
        { required: true, message: "请输入邮箱地址", trigger: "blur" },
        { type: 'email', message: "请输入有效的邮箱地址", trigger: "blur" }
    ],
});

// 页面初始化加载数据
onMounted(() => {
    getUserListData();
});

// 获取用户列表数据
const getUserListData = async () => {
    try {
        loading.value = true;
        // 调用API接口
        const response = await getUserList({
            pageNum: searchForm.pageNum,
            pageSize: searchForm.pageSize,
            role: searchForm.role || undefined,
            status: searchForm.status ? Number(searchForm.status) : undefined,
            username: searchForm.username || undefined,
        });
        // console.log("用户列表接口响应:", response.data.data);
        // 处理后端返回的ResultVO格式数据
        if (response.data.code === 200) {
            userList.value = response.data.data.records || [];
            total.value = response.data.data.total || 0;
        } else {
            ElMessage.error(response.data.msg || "获取用户列表失败");
        }
    } catch (error) {
        console.error("获取用户列表失败:", error);
        ElMessage.error("获取用户列表失败，请稍后重试");
    } finally {
        loading.value = false;
    }
};

// 重置查询表单
const resetSearchForm = () => {
    searchForm.pageNum = 1;
    searchForm.pageSize = 10;
    searchForm.role = "";
    searchForm.status = "";
    searchForm.username = "";
    getUserListData();
};

// 分页大小改变
const handleSizeChange = (val: number) => {
    searchForm.pageSize = val;
    getUserListData();
};

// 当前页改变
const handleCurrentChange = (val: number) => {
    searchForm.pageNum = val;
    getUserListData();
};

// 处理状态切换
const handleStatusChange = async (row: any) => {
    console.log("切换状态:", row);
    try {
        const response = await updateUserById({
            id: row.id,
            status: row.status,
        });
        if (response.data.code === 200) {
            ElMessage.success("状态修改成功");
        } else {
            // 状态修改失败，回显原来的状态
            row.status = row.status === "1" ? "0" : "1";
            ElMessage.error(response.data.msg || "状态修改失败");
        }
    } catch (error) {
        console.error("修改状态失败:", error);
        row.status = row.status === "1" ? "0" : "1";
        ElMessage.error("修改状态失败，请稍后重试");
    }
};

// 编辑用户
const handleEdit = (row: any) => {
    // 填充编辑表单数据
    editForm.id = row.id;
    editForm.username = row.username;
    editForm.email = row.email;
    editForm.role = row.role;
    editForm.status = row.status;
    // 打开弹窗
    dialogVisible.value = true;
};

// 提交编辑表单
const submitEditForm = async () => {
    try {
        // 表单校验
        await editFormRef.value.validate();

        const response = await updateUserById({
            id: editForm.id,
            email: editForm.email,
            role: editForm.role,
            status: editForm.status,
        });

        if (response.data.code === 200) {
            ElMessage.success("用户信息修改成功");
            dialogVisible.value = false;
            // 重新加载列表
            getUserListData();
        } else {
            ElMessage.error(response.data.msg || "用户信息修改失败");
        }
    } catch (error) {
        console.error("修改用户信息失败:", error);
        ElMessage.error("修改用户信息失败，请稍后重试");
    }
};
const handleResetPassword = async (id: number | string) => {
    try {
        // 确认重置密码
        await ElMessageBox.confirm(
            "此操作将重置该用户的密码为默认密码, 是否继续?",
            "提示",
            {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                type: "warning",
            }
        );
        const response = await updateUserById({
            id: id,
            passwordHash: "123456", // 默认密码
        });
        if (response.data.code === 200) {
            ElMessage.success("密码重置成功，默认密码为：123456");
        } else {
            ElMessage.error(response.data.msg || "密码重置失败");
        }
    } catch (error) {
        if (error !== "cancel") {
            console.error("重置密码失败:", error);
            ElMessage.error("密码重置失败，请稍后重试");
        }
    }
};
// 重置编辑表单
const resetEditForm = () => {
    if (editFormRef.value) {
        editFormRef.value.resetFields();
    }
    editForm.id = "";
    editForm.username = "";
    editForm.role = "";
    editForm.status = "1";
};

// 删除用户
const handleDelete = async (id: number | string) => {
    try {
        // 确认删除
        await ElMessageBox.confirm(
            "此操作将永久删除该用户, 是否继续?",
            "提示",
            {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                type: "warning",
            }
        );

        const response = await deleteUserById(id);
        if (response.data.code === 200) {
            ElMessage.success("删除成功");
            // 重新加载列表
            getUserListData();
        } else {
            ElMessage.error(response.data.msg || "删除失败");
        }
    } catch (error) {
        if (error !== "cancel") {
            console.error("删除用户失败:", error);
            ElMessage.error("删除失败，请稍后重试");
        }
    }
};

// 打开添加用户弹窗
const handleAdd = () => {
    addDialogVisible.value = true;
};

// 提交添加用户表单
const submitAddForm = async () => {
    try {
        // 表单校验
        await addFormRef.value.validate();

        // 构造注册参数
        const registerParams = {
            username: addForm.username,
            email: addForm.email,
            passwordHash: addForm.password,
            role: addForm.role
        };

        const response = await register(registerParams);

        if (response.data.code === 200) {
            ElMessage.success("用户添加成功");
            addDialogVisible.value = false;
            // 重新加载列表
            getUserListData();
        } else {
            ElMessage.error(response.data.msg || "用户添加失败");
        }
    } catch (error) {
        console.error("添加用户失败:", error);
        ElMessage.error("添加用户失败，请稍后重试");
    }
};

// 重置添加用户表单
const resetAddForm = () => {
    if (addFormRef.value) {
        addFormRef.value.resetFields();
    }
    addForm.username = "";
    addForm.password = "";
    addForm.confirmPassword = "";
    addForm.role = "user";
};
</script>

<style scoped>
.user-management-container {
    padding: 0 20px;
    background-color: #fff;
}

.page-header {
    margin-bottom: 20px;
    padding-bottom: 5px;
    border-bottom: 2px solid #f5a623;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.page-header h2 {
    color: #f5a623;
    margin: 0;
    font-size: 20px;
    font-weight: 600;
}

.add-btn {
    background-color: #f5a623;
    border-color: #f5a623;
}

.add-btn:hover {
    background-color: #e09612;
    border-color: #e09612;
}

.search-form {
    display: flex;
    align-items: center;
}

.search-btn {
    background-color: #f5a623;
    border-color: #f5a623;
}

.search-btn:hover {
    background-color: #e09612;
    border-color: #e09612;
}

.user-table {
    background-color: #fff;
    border-radius: 8px;
    padding: 10px;
    border: 1px solid #ffe888;
}

.pagination {
    margin-top: 10px;
    text-align: right;
}

:deep(.el-pagination .el-pager li.active) {
    color: #f5a623;
}

:deep(.el-pagination button:hover) {
    color: #f5a623;
}

:deep(.el-dialog__header) {
    border-bottom: 1px solid #ffe888;
}

:deep(.el-dialog__title) {
    color: #f5a623;
    font-weight: 600;
}

:deep(.el-button--primary) {
    background-color: #f5a623;
    border-color: #f5a623;
}

:deep(.el-button--primary:hover) {
    background-color: #e09612;
    border-color: #e09612;
}

:deep(.el-switch__core) {
    background-color: #ff0000;
}

:deep(.el-switch__core:after) {
    background-color: #fff;
}

:deep(.el-tag--warning) {
    background-color: #fff9e6;
    color: #f5a623;
    border-color: #ffe888;
}
</style>