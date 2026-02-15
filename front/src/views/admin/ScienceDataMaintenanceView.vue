<template>
  <div class="data-management-container">
    <!-- 顶部操作区 -->
    <div class="header-actions">
      <el-space wrap>
        <!-- 一级数据类型切换（朝代/典故） -->
        <el-select v-model="activeFirstType" placeholder="选择一级数据类型" @change="handleFirstTypeChange" style="width: 100px;">
          <el-option label="朝代管理" value="dynasty" />
          <el-option label="典故管理" value="allusion" />
        </el-select>

        <!-- 朝代子级选择（仅当一级类型为朝代时显示） -->
        <template v-if="activeFirstType === 'dynasty'">
          <!-- 先选择具体朝代 -->
          <el-select
            v-model="selectedDynastyId"
            placeholder="请先选择朝代"
            style="width: 150px"
            @change="handleDynastyChange"
            filterable
          >
            <el-option
              v-for="item in dynastyList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>

          <!-- 朝代子级数据类型切换（阶段/事件/人物） -->
          <el-select
            v-model="activeSubType"
            placeholder="选择子级数据"
            @change="handleSubTypeChange"
            :disabled="!selectedDynastyId"
            style="width: 180px"
          >
            <el-option label="朝代阶段" value="dynastyStage" />
            <el-option label="历史事件" value="historicalEvent" />
            <el-option label="历史人物" value="historicalPerson" />
          </el-select>
        </template>

        <!-- 搜索框（根据当前类型动态提示） -->
        <el-input
          v-model="searchForm.keyword"
          :placeholder="getSearchPlaceholder()"
          style="width: 300px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button icon="el-icon-search" @click="handleSearch" />
          </template>
        </el-input>

        <!-- 批量导入 -->
        <el-upload
          class="upload-btn"
          action="#"
          :auto-upload="false"
          :on-change="handleFileChange"
          accept=".xlsx,.xls"
          :show-file-list="false"
        >
          <el-button type="primary">
            <el-icon><upload /></el-icon>
            <span>批量导入</span>
          </el-button>
        </el-upload>

        <!-- 新增按钮（子级需选择朝代后才可用） -->
        <el-button
          type="success"
          @click="handleAdd"
          :disabled="isSubType && !selectedDynastyId"
        >
          <el-icon><plus /></el-icon>
          <span>新增</span>
        </el-button>

        <!-- 批量删除 -->
        <el-button
          type="danger"
          @click="handleBatchDelete"
          :disabled="selectedIds.length === 0"
        >
          <el-icon><delete /></el-icon>
          <span>批量删除</span>
        </el-button>
      </el-space>
    </div>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%; margin-top: 20px"
      @selection-change="handleSelectionChange"
    >
      <!-- 全选列 -->
      <el-table-column type="selection" width="55" />

      <!-- 一级类型：朝代 -->
      <template v-if="activeFirstType === 'dynasty' && !activeSubType">
        <el-table-column prop="id" label="朝代ID" width="80" />
        <el-table-column prop="name" label="朝代名称" min-width="100" />
        <el-table-column prop="startYear" label="起始年份" width="120">
          <template #default="scope">
            {{ scope.row.startYear < 0 ? `公元前${-scope.row.startYear}年` : `公元${scope.row.startYear}年` }}
          </template>
        </el-table-column>
        <el-table-column prop="endYear" label="结束年份" width="120">
          <template #default="scope">
            {{ scope.row.endYear < 0 ? `公元前${-scope.row.endYear}年` : `公元${scope.row.endYear}年` }}
          </template>
        </el-table-column>
        <el-table-column prop="capital" label="都城" min-width="100" />
        <el-table-column prop="founder" label="开国者" min-width="100" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </template>

      <!-- 一级类型：典故 -->
      <template v-if="activeFirstType === 'allusion'">
        <el-table-column prop="allusionName" label="典故名称"/>
        <el-table-column prop="source" label="出处" />
        <el-table-column prop="themeType" label="主题类型" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </template>

      <!-- 朝代子级：阶段 -->
      <template v-if="activeFirstType === 'dynasty' && activeSubType === 'dynastyStage'">
        <el-table-column prop="id" label="阶段ID" width="80" />
        <el-table-column prop="dynastyId" label="所属朝代ID" width="100" />
        <el-table-column prop="stageName" label="阶段名称" min-width="100" />
        <el-table-column prop="startYear" label="起始年份" width="120">
          <template #default="scope">
            {{ scope.row.startYear < 0 ? `公元前${-scope.row.startYear}年` : `公元${scope.row.startYear}年` }}
          </template>
        </el-table-column>
        <el-table-column prop="endYear" label="结束年份" width="120">
          <template #default="scope">
            {{ scope.row.endYear < 0 ? `公元前${-scope.row.endYear}年` : `公元${scope.row.endYear}年` }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </template>

      <!-- 朝代子级：事件 -->
      <template v-if="activeFirstType === 'dynasty' && activeSubType === 'historicalEvent'">
        <el-table-column prop="id" label="事件ID" width="80" />
        <el-table-column prop="dynastyId" label="所属朝代ID" width="100" />
        <el-table-column prop="stageId" label="所属阶段ID" width="100" />
        <el-table-column prop="eventName" label="事件名称" min-width="150" />
        <el-table-column prop="eventYear" label="事件年份" width="120">
          <template #default="scope">
            {{ scope.row.eventYear < 0 ? `公元前${-scope.row.eventYear}年` : `公元${scope.row.eventYear}年` }}
          </template>
        </el-table-column>
        <el-table-column prop="eventType" label="事件类型" width="100" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </template>

      <!-- 朝代子级：人物 -->
      <template v-if="activeFirstType === 'dynasty' && activeSubType === 'historicalPerson'">
        <el-table-column prop="id" label="人物ID" width="80" />
        <el-table-column prop="dynastyId" label="所属朝代ID" width="100" />
        <el-table-column prop="personName" label="人物姓名" min-width="100" />
        <el-table-column prop="identityType" label="身份类型" width="100" />
        <el-table-column prop="birthYear" label="出生年份" width="120">
          <template #default="scope">
            {{ scope.row.birthYear ? (scope.row.birthYear < 0 ? `公元前${-scope.row.birthYear}年` : `公元${scope.row.birthYear}年`) : '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </template>
    </el-table>

    <!-- 空状态提示 -->
    <div v-if="tableData.length === 0 && !loading" class="empty-tip">
      <el-empty :description="getEmptyDescription()" />
    </div>

    <!-- 分页控件 -->
    <el-pagination
      style="margin-top: 20px; text-align: right"
      v-model:current-page="pagination.pageNum"
      v-model:page-size="pagination.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handlePageChange"
      @current-change="handlePageChange"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="getDialogTitle()"
      width="800px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        style="max-height: 600px; overflow-y: auto"
      >
        <!-- 一级类型：朝代表单 -->
        <template v-if="activeFirstType === 'dynasty' && !activeSubType">
          <el-form-item label="朝代名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入朝代名称" />
          </el-form-item>
          <el-form-item label="起始年份" prop="startYear">
            <el-input
              v-model="formData.startYear"
              placeholder="公元前请输入负数，如公元前221年输入-221"
              type="number"
            />
          </el-form-item>
          <el-form-item label="结束年份" prop="endYear">
            <el-input
              v-model="formData.endYear"
              placeholder="公元前请输入负数，如公元前207年输入-207"
              type="number"
            />
          </el-form-item>
          <el-form-item label="都城" prop="capital">
            <el-input v-model="formData.capital" placeholder="请输入都城名称" />
          </el-form-item>
          <el-form-item label="开国者" prop="founder">
            <el-input v-model="formData.founder" placeholder="请输入开国者姓名" />
          </el-form-item>
          <el-form-item label="朝代地图">
            <el-upload
              action="#"
              :auto-upload="false"
              :on-change="(file) => handleUpload('mapPath', file)"
              :show-file-list="false"
              accept="image/*"
            >
              <el-button icon="el-icon-picture">上传地图</el-button>
            </el-upload>
            <div v-if="formData.mapPath" style="margin-top: 10px">
              <img :src="formData.mapPath" alt="朝代地图" style="width: 200px; height: auto" />
            </div>
          </el-form-item>
          <el-form-item label="朝代简介" prop="dynastyDesc">
            <el-input
              v-model="formData.dynastyDesc"
              type="textarea"
              rows="5"
              placeholder="请输入朝代简介"
            />
          </el-form-item>
        </template>

        <!-- 一级类型：典故表单 -->
        <template v-if="activeFirstType === 'allusion'">
          <el-form-item label="典故名称" prop="allusionName">
            <el-input v-model="formData.allusionName" placeholder="请输入典故名称" />
          </el-form-item>
          <el-form-item label="出处" prop="source">
            <el-input v-model="formData.source" placeholder="请输入古籍出处" />
          </el-form-item>
          <el-form-item label="原文" prop="originalText">
            <el-input v-model="formData.originalText" type="textarea" rows="3" placeholder="请输入原文" />
          </el-form-item>
          <el-form-item label="释义" prop="paraphrase">
            <el-input v-model="formData.paraphrase" type="textarea" rows="3" placeholder="请输入释义" />
          </el-form-item>
          <el-form-item label="寓意" prop="moral">
            <el-input v-model="formData.moral" type="textarea" rows="3" placeholder="请输入寓意" />
          </el-form-item>
          <el-form-item label="主题类型" prop="themeType">
            <el-select v-model="formData.themeType" placeholder="请选择主题类型">
              <el-option label="励志" value="励志" />
              <el-option label="权谋" value="权谋" />
              <el-option label="友情" value="友情" />
              <el-option label="忠诚" value="忠诚" />
              <el-option label="智慧" value="智慧" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="多媒体资源">
            <el-upload
              action="#"
              :auto-upload="false"
              :on-change="(file) => handleUpload('multimediaPath', file)"
              :show-file-list="false"
              accept="image/*,audio/*,video/*"
            >
              <el-button icon="el-icon-video-camera">上传多媒体</el-button>
            </el-upload>
            <div v-if="formData.multimediaPath" style="margin-top: 10px">
              <span>{{ formData.multimediaPath }}</span>
            </div>
          </el-form-item>
        </template>

        <!-- 朝代子级：阶段表单 -->
        <template v-if="activeFirstType === 'dynasty' && activeSubType === 'dynastyStage'">
          <el-form-item label="所属朝代ID" prop="dynastyId">
            <el-select v-model="formData.dynastyId" placeholder="请选择所属朝代" disabled>
              <el-option
                v-for="item in dynastyList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="阶段名称" prop="stageName">
            <el-input v-model="formData.stageName" placeholder="请输入阶段名称（如：盛唐、晚唐）" />
          </el-form-item>
          <el-form-item label="起始年份" prop="startYear">
            <el-input
              v-model="formData.startYear"
              type="number"
              placeholder="公元前请输入负数，如公元618年输入618"
            />
          </el-form-item>
          <el-form-item label="结束年份" prop="endYear">
            <el-input
              v-model="formData.endYear"
              type="number"
              placeholder="公元前请输入负数，如公元907年输入907"
            />
          </el-form-item>
          <el-form-item label="阶段描述" prop="stageDesc">
            <el-input
              v-model="formData.stageDesc"
              type="textarea"
              rows="5"
              placeholder="请输入阶段详细描述"
            />
          </el-form-item>
        </template>

        <!-- 朝代子级：事件表单 -->
        <template v-if="activeFirstType === 'dynasty' && activeSubType === 'historicalEvent'">
          <el-form-item label="所属朝代ID" prop="dynastyId">
            <el-select v-model="formData.dynastyId" placeholder="请选择所属朝代" disabled>
              <el-option
                v-for="item in dynastyList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="所属阶段ID" prop="stageId">
            <el-input v-model="formData.stageId" type="number" placeholder="可选，输入所属阶段ID" />
          </el-form-item>
          <el-form-item label="事件名称" prop="eventName">
            <el-input v-model="formData.eventName" placeholder="请输入事件名称" />
          </el-form-item>
          <el-form-item label="事件年份" prop="eventYear">
            <el-input
              v-model="formData.eventYear"
              type="number"
              placeholder="公元前请输入负数，如公元前202年输入-202"
            />
          </el-form-item>
          <el-form-item label="事件类型" prop="eventType">
            <el-select v-model="formData.eventType" placeholder="请选择事件类型">
              <el-option label="政治" value="政治" />
              <el-option label="军事" value="军事" />
              <el-option label="文化" value="文化" />
              <el-option label="经济" value="经济" />
              <el-option label="社会" value="社会" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="事件背景" prop="eventBg">
            <el-input v-model="formData.eventBg" type="textarea" rows="3" placeholder="请输入事件背景" />
          </el-form-item>
          <el-form-item label="事件过程" prop="eventProcess">
            <el-input v-model="formData.eventProcess" type="textarea" rows="5" placeholder="请输入事件过程" />
          </el-form-item>
          <el-form-item label="事件影响" prop="eventImpact">
            <el-input v-model="formData.eventImpact" type="textarea" rows="3" placeholder="请输入事件影响" />
          </el-form-item>
          <el-form-item label="事件配图">
            <el-upload
              action="#"
              :auto-upload="false"
              :on-change="(file) => handleUpload('eventPicPath', file)"
              :show-file-list="false"
              accept="image/*"
            >
              <el-button icon="el-icon-picture">上传配图</el-button>
            </el-upload>
            <div v-if="formData.eventPicPath" style="margin-top: 10px">
              <img :src="formData.eventPicPath" alt="事件配图" style="width: 200px; height: auto" />
            </div>
          </el-form-item>
        </template>

        <!-- 朝代子级：人物表单 -->
        <template v-if="activeFirstType === 'dynasty' && activeSubType === 'historicalPerson'">
          <el-form-item label="所属朝代ID" prop="dynastyId">
            <el-select v-model="formData.dynastyId" placeholder="请选择所属朝代" disabled>
              <el-option
                v-for="item in dynastyList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="人物姓名" prop="personName">
            <el-input v-model="formData.personName" placeholder="请输入人物姓名" />
          </el-form-item>
          <el-form-item label="出生年份" prop="birthYear">
            <el-input
              v-model="formData.birthYear"
              type="number"
              placeholder="公元前请输入负数，未知留空"
            />
          </el-form-item>
          <el-form-item label="去世年份" prop="deathYear">
            <el-input
              v-model="formData.deathYear"
              type="number"
              placeholder="公元前请输入负数，未知留空"
            />
          </el-form-item>
          <el-form-item label="身份类型" prop="identityType">
            <el-select v-model="formData.identityType" placeholder="请选择身份类型">
              <el-option label="帝王" value="帝王" />
              <el-option label="将相" value="将相" />
              <el-option label="文人" value="文人" />
              <el-option label="科学家" value="科学家" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="人物标签" prop="tags">
            <el-select v-model="formData.tags" multiple placeholder="请选择人物标签">
              <el-option label="诗人" value="诗人" />
              <el-option label="政治家" value="政治家" />
              <el-option label="军事家" value="军事家" />
              <el-option label="书法家" value="书法家" />
            </el-select>
          </el-form-item>
          <el-form-item label="人物头像">
            <el-upload
              action="#"
              :auto-upload="false"
              :on-change="(file) => handleUpload('avatarPath', file)"
              :show-file-list="false"
              accept="image/*"
            >
              <el-button icon="el-icon-user">上传头像</el-button>
            </el-upload>
            <div v-if="formData.avatarPath" style="margin-top: 10px">
              <img :src="formData.avatarPath" alt="人物头像" style="width: 100px; height: auto" />
            </div>
          </el-form-item>
          <el-form-item label="人物生平" prop="personBio">
            <el-input
              v-model="formData.personBio"
              type="textarea"
              rows="5"
              placeholder="请输入人物生平简介"
            />
          </el-form-item>
        </template>
      </el-form>

      <template #footer>
        <el-space>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确认</el-button>
        </el-space>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElEmpty } from 'element-plus'

// 数据类型映射
const dataTypeMap = {
  dynasty: '朝代',
  dynastyStage: '朝代阶段',
  historicalEvent: '历史事件',
  historicalPerson: '历史人物',
  allusion: '典故'
}

// 响应式数据
const activeFirstType = ref('dynasty') // 一级类型：dynasty/allusion
const activeSubType = ref('dynastyStage') // 朝代子级类型：dynastyStage/historicalEvent/historicalPerson
const selectedDynastyId = ref('') // 选中的朝代ID
const dynastyList = ref([]) // 朝代列表（用于选择）
const loading = ref(false) // 表格加载状态
const selectedIds = ref([]) // 选中的ID集合
const dialogVisible = ref(false) // 弹窗显示状态
const dialogType = ref('add') // 弹窗类型：add/edit
const formRef = ref(null) // 表单引用

// 计算属性：是否为子级类型
const isSubType = computed(() => !!activeSubType.value)

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 模拟表格数据
const tableData = ref([])

// 表单数据
const formData = reactive({})

// 表单校验规则
const formRules = reactive({
  // 朝代校验规则
  name: [{ required: true, message: '请输入朝代名称', trigger: 'blur' }],
  startYear: [{ required: true, message: '请输入起始年份', trigger: 'blur', type: 'number' }],
  endYear: [{ required: true, message: '请输入结束年份', trigger: 'blur', type: 'number' }],
  // 事件校验规则
  eventName: [{ required: true, message: '请输入事件名称', trigger: 'blur' }],
  dynastyId: [{ required: true, message: '请选择所属朝代', trigger: 'change' }],
  eventYear: [{ required: true, message: '请输入事件年份', trigger: 'blur', type: 'number' }],
  eventType: [{ required: true, message: '请选择事件类型', trigger: 'change' }],
  // 人物校验规则
  personName: [{ required: true, message: '请输入人物姓名', trigger: 'blur' }],
  identityType: [{ required: true, message: '请选择身份类型', trigger: 'change' }],
  // 典故校验规则
  allusionName: [{ required: true, message: '请输入典故名称', trigger: 'blur' }],
  themeType: [{ required: true, message: '请选择主题类型', trigger: 'change' }],
  // 朝代阶段校验规则
  stageName: [{ required: true, message: '请输入阶段名称', trigger: 'blur' }]
})

// 获取搜索框占位符
const getSearchPlaceholder = () => {
  if (activeFirstType === 'dynasty' && !activeSubType) {
    return '请输入朝代名称/开国者/都城搜索'
  } else if (activeFirstType === 'allusion') {
    return '请输入典故名称/出处/主题类型搜索'
  } else if (activeSubType === 'dynastyStage') {
    return '请输入阶段名称搜索'
  } else if (activeSubType === 'historicalEvent') {
    return '请输入事件名称/类型搜索'
  } else if (activeSubType === 'historicalPerson') {
    return '请输入人物姓名/身份类型搜索'
  }
  return '请输入关键词搜索'
}

// 获取空状态描述
const getEmptyDescription = () => {
  if (activeFirstType === 'dynasty' && !activeSubType) {
    return '暂无朝代数据'
  } else if (activeFirstType === 'allusion') {
    return '暂无典故数据'
  } else if (!selectedDynastyId) {
    return '请先选择朝代'
  } else if (activeSubType === 'dynastyStage') {
    return `暂无【${dynastyList.value.find(item => item.id === selectedDynastyId)?.name}】的阶段数据`
  } else if (activeSubType === 'historicalEvent') {
    return `暂无【${dynastyList.value.find(item => item.id === selectedDynastyId)?.name}】的事件数据`
  } else if (activeSubType === 'historicalPerson') {
    return `暂无【${dynastyList.value.find(item => item.id === selectedDynastyId)?.name}】的人物数据`
  }
  return '暂无数据'
}

// 获取弹窗标题
const getDialogTitle = () => {
  let typeName = ''
  if (activeFirstType === 'dynasty' && !activeSubType) {
    typeName = '朝代'
  } else if (activeFirstType === 'allusion') {
    typeName = '典故'
  } else if (activeSubType) {
    typeName = dataTypeMap[activeSubType]
  }
  return `${dialogType.value === 'add' ? '新增' : '编辑'}${typeName}`
}

// 加载朝代列表（用于选择）
const loadDynastyList = () => {
  // 模拟接口请求
  dynastyList.value = [
    { id: 1, name: '秦朝', startYear: -221, endYear: -207 },
    { id: 2, name: '汉朝', startYear: -202, endYear: 220 },
    { id: 3, name: '唐朝', startYear: 618, endYear: 907 }
  ]
}

// 初始化模拟数据
const initMockData = () => {
  loading.value = true
  // 模拟异步请求
  setTimeout(() => {
    // 一级类型：朝代
    if (activeFirstType.value === 'dynasty' && !activeSubType.value) {
      tableData.value = [
        { id: 1, name: '秦朝', startYear: -221, endYear: -207, capital: '咸阳', founder: '嬴政' },
        { id: 2, name: '汉朝', startYear: -202, endYear: 220, capital: '长安、洛阳', founder: '刘邦' },
        { id: 3, name: '唐朝', startYear: 618, endYear: 907, capital: '长安', founder: '李渊' }
      ]
    }
    // 一级类型：典故
    else if (activeFirstType.value === 'allusion') {
      tableData.value = [
        { id: 1, allusionName: '指鹿为马', source: '《史记·秦始皇本纪》', themeType: '权谋' },
        { id: 2, allusionName: '沉鱼落雁', source: '《庄子·齐物论》', themeType: '形容' },
        { id: 3, allusionName: '破釜沉舟', source: '《史记·项羽本纪》', themeType: '励志' }
      ]
    }
    // 朝代子级：阶段
    else if (activeSubType.value === 'dynastyStage' && selectedDynastyId.value) {
      const stageMap = {
        1: [{ id: 1, dynastyId: 1, stageName: '秦朝统一', startYear: -221, endYear: -207 }],
        2: [{ id: 2, dynastyId: 2, stageName: '西汉', startYear: -202, endYear: 8 }, { id: 3, dynastyId: 2, stageName: '东汉', startYear: 25, endYear: 220 }],
        3: [{ id: 4, dynastyId: 3, stageName: '盛唐', startYear: 618, endYear: 755 }, { id: 5, dynastyId: 3, stageName: '晚唐', startYear: 756, endYear: 907 }]
      }
      tableData.value = stageMap[selectedDynastyId.value] || []
    }
    // 朝代子级：事件
    else if (activeSubType.value === 'historicalEvent' && selectedDynastyId.value) {
      const eventMap = {
        1: [{ id: 1, dynastyId: 1, stageId: null, eventName: '秦始皇统一六国', eventYear: -221, eventType: '政治' }],
        2: [{ id: 2, dynastyId: 2, stageId: null, eventName: '张骞出使西域', eventYear: -138, eventType: '外交' }],
        3: [{ id: 3, dynastyId: 3, stageId: 4, eventName: '安史之乱', eventYear: 755, eventType: '军事' }]
      }
      tableData.value = eventMap[selectedDynastyId.value] || []
    }
    // 朝代子级：人物
    else if (activeSubType.value === 'historicalPerson' && selectedDynastyId.value) {
      const personMap = {
        1: [{ id: 1, dynastyId: 1, personName: '秦始皇', birthYear: -259, deathYear: -210, identityType: '帝王', tags: ['政治家', '军事家'] }],
        2: [{ id: 2, dynastyId: 2, personName: '司马迁', birthYear: -145, deathYear: -86, identityType: '文人', tags: ['史学家'] }],
        3: [{ id: 3, dynastyId: 3, personName: '李白', birthYear: 701, deathYear: 762, identityType: '文人', tags: ['诗人', '酒仙'] }]
      }
      tableData.value = personMap[selectedDynastyId.value] || []
    } else {
      tableData.value = []
    }
    pagination.total = tableData.value.length
    loading.value = false
  }, 500)
}

// 一级类型切换
const handleFirstTypeChange = () => {
  // 切换一级类型时重置子级状态
  activeSubType.value = ''
  selectedDynastyId.value = ''
  pagination.pageNum = 1
  if (activeFirstType.value === 'dynasty') {
    loadDynastyList()
  }
  initMockData()
}

// 朝代选择变更
const handleDynastyChange = () => {
  pagination.pageNum = 1
  initMockData()
}

// 子级类型切换
const handleSubTypeChange = () => {
  pagination.pageNum = 1
  initMockData()
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  initMockData()
}

// 分页变更
const handlePageChange = () => {
  initMockData()
}

// 选择项变更
const handleSelectionChange = (val) => {
  selectedIds.value = val.map(item => item.id)
}

// 文件上传（模拟）
const handleUpload = (field, file) => {
  ElMessage.success(`${file.name} 上传成功`)
  formData[field] = URL.createObjectURL(file.raw) // 模拟文件路径
}

// Excel文件变更（批量导入）
const handleFileChange = (file) => {
  if (isSubType.value && !selectedDynastyId.value) {
    ElMessage.warning('请先选择朝代再进行导入')
    return
  }
  ElMessageBox.confirm(
    `确认导入文件 ${file.name} 吗？`,
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    setTimeout(() => {
      ElMessage.success('导入成功')
      initMockData()
    }, 1000)
  })
}

// 新增
const handleAdd = () => {
  dialogType.value = 'add'
  // 重置表单
  formRef.value?.resetFields()
  // 清空表单数据
  Object.keys(formData).forEach(key => {
    delete formData[key]
  })
  // 子级类型默认填充选中的朝代ID
  if (isSubType.value && selectedDynastyId.value) {
    formData.dynastyId = selectedDynastyId.value
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogType.value = 'edit'
  // 重置表单
  formRef.value?.resetFields()
  // 复制行数据到表单
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

// 删除单条
const handleDelete = (id) => {
  ElMessageBox.confirm(
    '此操作将永久删除该数据, 是否继续?',
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    tableData.value = tableData.value.filter(item => item.id !== id)
    pagination.total = tableData.value.length
    ElMessage.success('删除成功!')
  })
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(
    '此操作将永久删除选中的所有数据, 是否继续?',
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    tableData.value = tableData.value.filter(item => !selectedIds.value.includes(item.id))
    pagination.total = tableData.value.length
    selectedIds.value = []
    ElMessage.success('批量删除成功!')
  })
}

// 提交表单
const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (!valid) return
    setTimeout(() => {
      if (dialogType.value === 'add') {
        // 新增
        formData.id = tableData.value.length + 1
        tableData.value.unshift({ ...formData })
      } else {
        // 编辑
        const index = tableData.value.findIndex(item => item.id === formData.id)
        if (index !== -1) {
          tableData.value[index] = { ...formData }
        }
      }
      pagination.total = tableData.value.length
      dialogVisible.value = false
      ElMessage.success(`${dialogType.value === 'add' ? '新增' : '编辑'}成功`)
    }, 500)
  })
}

// 初始化
onMounted(() => {
  loadDynastyList()
  initMockData()
})
</script>

<style scoped>
.data-management-container {
  padding: 20px;
  background: #fff;
  min-height: calc(100vh - 60px);
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-btn {
  display: inline-block;
}

.empty-tip {
  margin-top: 50px;
  text-align: center;
}
</style>