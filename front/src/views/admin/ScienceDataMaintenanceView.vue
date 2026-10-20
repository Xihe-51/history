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
            clearable
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
            clearable
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
            <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
            </el-button>
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
      :header-cell-style="{ 'text-align': 'center' }" :cell-style="{ 'text-align': 'center' }"
    >
      <!-- 全选列 -->
      <el-table-column type="selection" width="55" />

      <!-- 一级类型：朝代 -->
      <template v-if="activeFirstType === 'dynasty' && !activeSubType">
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
        <el-table-column label="所属朝代">
          <template #default="scope">
            {{ dynastyList.find(item => String(item.id) === String(scope.row.dynastyId))?.name || '—' }}
          </template>
        </el-table-column>
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
        <el-table-column prop="stageName" label="阶段名称" />
        <el-table-column prop="startYear" label="起始年份">
          <template #default="scope">
            {{ scope.row.startYear < 0 ? `公元前${-scope.row.startYear}年` : `公元${scope.row.startYear}年` }}
          </template>
        </el-table-column>
        <el-table-column prop="endYear" label="结束年份">
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
        <el-table-column prop="eventName" label="事件名称" />
        <el-table-column prop="eventYear" label="事件年份">
          <template #default="scope">
            {{ scope.row.eventYear < 0 ? `公元前${-scope.row.eventYear}年` : `公元${scope.row.eventYear}年` }}
          </template>
        </el-table-column>
        <el-table-column prop="eventType" label="事件类型" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </template>

      <!-- 朝代子级：人物 -->
      <template v-if="activeFirstType === 'dynasty' && activeSubType === 'historicalPerson'">
        <el-table-column prop="personName" label="人物姓名"/>
        <el-table-column label="人物标签">
         <template #default="scope">
            <el-tag v-for="tag in scope.row.tags" :key="tag" style="margin-right: 4px">{{ tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="identityType" label="身份类型"/>
        <el-table-column prop="birthYear" label="出生年份">
          <template #default="scope">
            {{ scope.row.birthYear ? (scope.row.birthYear < 0 ? `公元前${-scope.row.birthYear}年` : `公元${scope.row.birthYear}年`) : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="deathYear" label="死亡年份">
          <template #default="scope">
            {{ scope.row.deathYear ? (scope.row.deathYear < 0 ? `公元前${-scope.row.deathYear}年` : `公元${scope.row.deathYear}年`) : '未知' }}
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
      :title="dialogType === 'add' ? '新增' : '编辑'"
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
              <el-button>上传地图</el-button>
            </el-upload>
            <!-- 地图预览区域 -->
            <div class="upload-preview-container" style="margin-top: 10px">
              <div v-if="previewFiles.mapPath" class="preview-title">预览：</div>
              <img 
                v-if="previewFiles.mapPath" 
                :src="previewFiles.mapPath" 
                alt="朝代地图预览" 
                class="image-preview"
              />
              <img 
                v-else-if="formData.mapPath" 
                :src="resolveAssetUrl(formData.mapPath)" 
                alt="现有朝代地图" 
                class="image-preview"
              />
              <div v-else class="no-preview-text">暂无地图</div>
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
          <el-form-item label="所属朝代" prop="dynastyId">
            <el-select v-model="formData.dynastyId" placeholder="请选择所属朝代" clearable>
              <el-option
                v-for="item in dynastyList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
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
              <el-button>上传多媒体资源</el-button>
            </el-upload>
            <!-- 多媒体预览区域 -->
            <div class="upload-preview-container" style="margin-top: 10px">
              <div v-if="previewFiles.multimediaPath || formData.multimediaPath" class="preview-title">预览：</div>
              
              <!-- 图片预览 -->
              <img 
                v-if="previewFiles.multimediaPath && isImageFile(previewFiles.multimediaPath)" 
                :src="previewFiles.multimediaPath" 
                alt="多媒体预览" 
                class="image-preview"
              />
              <img 
                v-else-if="formData.multimediaPath && isImageFile(formData.multimediaPath)" 
                :src="resolveAssetUrl(formData.multimediaPath)" 
                alt="现有多媒体" 
                class="image-preview"
              />
              
              <!-- 视频预览 -->
              <video 
                v-if="previewFiles.multimediaPath && isVideoFile(previewFiles.multimediaPath)" 
                :src="previewFiles.multimediaPath" 
                controls 
                class="video-preview"
              >您的浏览器不支持视频播放</video>
              <video 
                v-else-if="formData.multimediaPath && isVideoFile(formData.multimediaPath)" 
                :src="resolveAssetUrl(formData.multimediaPath)" 
                controls 
                class="video-preview"
              >您的浏览器不支持视频播放</video>
              
              <!-- 音频预览 -->
              <audio 
                v-if="previewFiles.multimediaPath && isAudioFile(previewFiles.multimediaPath)" 
                :src="previewFiles.multimediaPath" 
                controls 
                class="audio-preview"
              >您的浏览器不支持音频播放</audio>
              <audio 
                v-else-if="formData.multimediaPath && isAudioFile(formData.multimediaPath)" 
                :src="resolveAssetUrl(formData.multimediaPath)" 
                controls 
                class="audio-preview"
              >您的浏览器不支持音频播放</audio>
              
              <!-- 无资源提示 -->
              <div v-else class="no-preview-text">暂无多媒体资源</div>
              
              <!-- 文件名显示 -->
              <div v-if="previewFiles.multimediaPathName || (formData.multimediaPath && !isMediaFile(formData.multimediaPath))" class="file-name-text">
                {{ previewFiles.multimediaPathName || formData.multimediaPath }}
              </div>
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
              <el-button>上传配图</el-button>
            </el-upload>
            <!-- 事件配图预览区域 -->
            <div class="upload-preview-container" style="margin-top: 10px">
              <div v-if="previewFiles.eventPicPath" class="preview-title">预览：</div>
              <img 
                v-if="previewFiles.eventPicPath" 
                :src="previewFiles.eventPicPath" 
                alt="事件配图预览" 
                class="image-preview"
              />
              <img 
                v-else-if="formData.eventPicPath" 
                :src="resolveAssetUrl(formData.eventPicPath)" 
                alt="现有事件配图" 
                class="image-preview"
              />
              <div v-else class="no-preview-text">暂无事件配图</div>
            </div>
          </el-form-item>
        </template>

        <!-- 朝代子级：人物表单 -->
        <template v-if="activeFirstType === 'dynasty' && activeSubType === 'historicalPerson'">
          <el-form-item label="所属朝代" prop="dynastyId">
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
              <el-button>上传画像</el-button>
            </el-upload>
            <!-- 人物头像预览区域 -->
            <div class="upload-preview-container" style="margin-top: 10px">
              <div v-if="previewFiles.avatarPath" class="preview-title">预览：</div>
              <img 
                v-if="previewFiles.avatarPath" 
                :src="previewFiles.avatarPath" 
                alt="人物头像预览" 
                class="avatar-preview"
              />
              <img 
                v-else-if="formData.avatarPath" 
                :src="resolveAssetUrl(formData.avatarPath)" 
                alt="现有人物头像" 
                class="avatar-preview"
              />
              <div v-else class="no-preview-text">暂无人物头像</div>
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
import { Search, Upload, Plus, Delete } from '@element-plus/icons-vue'
import request, { resolveAssetUrl, errorMessage } from '@/utils/request'
import { uploadFile } from '@/api/file'

// 数据类型 -> 后端接口前缀
const apiPrefixMap = {
  dynasty: '/dynasty',
  allusion: '/allusion',
  dynastyStage: '/dynasty-stage',
  historicalEvent: '/historical-event',
  historicalPerson: '/historical-person'
}

// 响应式数据
const activeFirstType = ref('dynasty') // 一级类型：dynasty/allusion
const activeSubType = ref('') // 朝代子级类型：dynastyStage/historicalEvent/historicalPerson
const selectedDynastyId = ref(null) // 选中的朝代ID
const dynastyList = ref([]) // 朝代列表（用于选择）
const loading = ref(false) // 表格加载状态
const selectedIds = ref([]) // 选中的ID集合
const dialogVisible = ref(false) // 弹窗显示状态
const dialogType = ref('add') // 弹窗类型：add/edit
const formRef = ref(null) // 表单引用

// 预览文件存储
const previewFiles = reactive({
  mapPath: '',         // 朝代地图预览
  multimediaPath: '',  // 多媒体资源预览
  multimediaPathName: '', // 多媒体资源文件名
  eventPicPath: '',    // 事件配图预览
  avatarPath: ''       // 人物头像预览
})

// 计算属性：是否为子级类型
const isSubType = computed(() => !!activeSubType.value)

// 当前操作的数据类型（朝代/典故/朝代阶段/历史事件/历史人物）
const currentDataType = computed(() => {
  if (activeFirstType.value === 'allusion') return 'allusion'
  return activeSubType.value || 'dynasty'
})

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

// 表格数据
const tableData = ref([])

// 表单数据
const formData = reactive({})

// 表单校验规则
const formRules = reactive({
  // 朝代校验规则
  name: [{ required: true, message: '请输入朝代名称', trigger: 'blur' }],
  startYear: [{ required: true, message: '请输入起始年份', trigger: 'blur' }],
  endYear: [{ required: true, message: '请输入结束年份', trigger: 'blur' }],
  // 事件校验规则
  eventName: [{ required: true, message: '请输入事件名称', trigger: 'blur' }],
  dynastyId: [{ required: true, message: '请选择所属朝代', trigger: 'change' }],
  eventYear: [{ required: true, message: '请输入事件年份', trigger: 'blur' }],
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

// 判断文件类型 - 图片
const isImageFile = (filePath) => {
  if (!filePath) return false
  const imageTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/bmp', 'image/webp']
  if (filePath.startsWith('blob:')) {
    // 处理blob URL，从previewFiles中获取原始文件类型
    const fileType = Object.values(previewFiles).find(item => item === filePath)
    if (fileType) {
      const file = previewFiles[Object.keys(previewFiles).find(key => previewFiles[key] === filePath)]
      return imageTypes.includes(file?.type)
    }
  }
  // 处理远程URL
  const ext = filePath.split('.').pop().toLowerCase()
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)
}

// 判断文件类型 - 视频
const isVideoFile = (filePath) => {
  if (!filePath) return false
  const videoTypes = ['video/mp4', 'video/avi', 'video/mov', 'video/wmv', 'video/flv']
  if (filePath.startsWith('blob:')) {
    const fileType = Object.values(previewFiles).find(item => item === filePath)
    if (fileType) {
      const file = previewFiles[Object.keys(previewFiles).find(key => previewFiles[key] === filePath)]
      return videoTypes.includes(file?.type)
    }
  }
  const ext = filePath.split('.').pop().toLowerCase()
  return ['mp4', 'avi', 'mov', 'wmv', 'flv'].includes(ext)
}

// 判断文件类型 - 音频
const isAudioFile = (filePath) => {
  if (!filePath) return false
  const audioTypes = ['audio/mp3', 'audio/wav', 'audio/ogg', 'audio/m4a']
  if (filePath.startsWith('blob:')) {
    const fileType = Object.values(previewFiles).find(item => item === filePath)
    if (fileType) {
      const file = previewFiles[Object.keys(previewFiles).find(key => previewFiles[key] === filePath)]
      return audioTypes.includes(file?.type)
    }
  }
  const ext = filePath.split('.').pop().toLowerCase()
  return ['mp3', 'wav', 'ogg', 'm4a'].includes(ext)
}

// 判断是否为媒体文件（图片/视频/音频）
const isMediaFile = (filePath) => {
  return isImageFile(filePath) || isVideoFile(filePath) || isAudioFile(filePath)
}

// 获取搜索框占位符
const getSearchPlaceholder = () => {
  if (activeFirstType.value === 'dynasty' && !activeSubType.value) {
    return '请输入朝代名称搜索'
  } else if (activeFirstType.value === 'allusion') {
    return '请输入典故名称搜索'
  } else if (activeSubType.value === 'dynastyStage') {
    return '请输入阶段名称搜索'
  } else if (activeSubType.value === 'historicalEvent') {
    return '请输入事件名称搜索'
  } else if (activeSubType.value === 'historicalPerson') {
    return '请输入人物姓名搜索'
  }
  return '请输入关键词搜索'
}

// 获取空状态描述
const getEmptyDescription = () => {
  if (activeSubType.value && !selectedDynastyId.value) {
    return '请先选择朝代'
  }
  if (activeFirstType.value === 'dynasty' && !activeSubType.value) {
    return '暂无朝代数据'
  } else if (activeFirstType.value === 'allusion') {
    return '暂无典故数据'
  } else if (activeSubType.value === 'dynastyStage') {
    return `暂无【${dynastyList.value.find(item => item.id === selectedDynastyId.value)?.name}】的阶段数据`
  } else if (activeSubType.value === 'historicalEvent') {
    return `暂无【${dynastyList.value.find(item => item.id === selectedDynastyId.value)?.name}】的事件数据`
  } else if (activeSubType.value === 'historicalPerson') {
    return `暂无【${dynastyList.value.find(item => item.id === selectedDynastyId.value)?.name}】的人物数据`
  }
  return '暂无数据'
}

// 统一校验后端业务响应：code !== 200 视为失败（HTTP 层仍为 200）
const ensureOk = (res) => {
  const body = res?.data
  if (!body || body.code !== 200) {
    throw new Error(body?.msg || '操作失败')
  }
  return body.data
}

// 加载朝代列表（用于下拉选择）
const loadDynastyList = async () => {
  try {
    const res = await request.get('/dynasty/list/all')
    dynastyList.value = ensureOk(res) || []
  } catch (e) {
    ElMessage.error(errorMessage(e, '加载朝代列表失败'))
    dynastyList.value = []
  }
}

// 加载表格数据（按当前类型分页 / 关键词查询）
const loadTableData = async () => {
  // 子级数据必须选中所属朝代
  if (isSubType.value && !selectedDynastyId.value) {
    tableData.value = []
    pagination.total = 0
    return
  }
  loading.value = true
  try {
    const type = currentDataType.value
    const keyword = (searchForm.keyword || '').trim() || undefined
    const base = { pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    let res
    if (type === 'dynasty') {
      res = await request.get('/dynasty/page', { params: { ...base, name: keyword } })
    } else if (type === 'allusion') {
      res = await request.get('/allusion/page', { params: { ...base, allusionName: keyword } })
    } else if (type === 'dynastyStage') {
      res = await request.get('/dynasty-stage/page', { params: { ...base, dynastyId: selectedDynastyId.value, stageName: keyword } })
    } else if (type === 'historicalEvent') {
      res = await request.get('/historical-event/page', { params: { ...base, dynastyId: selectedDynastyId.value, eventName: keyword } })
    } else if (type === 'historicalPerson') {
      res = await request.get('/historical-person/page', { params: { ...base, dynastyId: selectedDynastyId.value, personName: keyword } })
    }
    const page = ensureOk(res)
    tableData.value = page?.records || []
    pagination.total = page?.total || 0
  } catch (e) {
    ElMessage.error(errorMessage(e, '加载数据失败'))
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 一级类型切换
const handleFirstTypeChange = () => {
  // 切换一级类型时重置子级状态
  activeSubType.value = ''
  selectedDynastyId.value = null
  searchForm.keyword = ''
  pagination.pageNum = 1
  loadTableData()
}

// 朝代选择变更
const handleDynastyChange = () => {
  searchForm.keyword = ''
  pagination.pageNum = 1
  // 仅子级数据（阶段/事件/人物）依赖所选朝代，朝代列表本身不受影响
  if (isSubType.value) {
    loadTableData()
  }
}

// 子级类型切换
const handleSubTypeChange = () => {
  searchForm.keyword = ''
  pagination.pageNum = 1
  loadTableData()
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadTableData()
}

// 分页变更
const handlePageChange = () => {
  loadTableData()
}

// 选择项变更
const handleSelectionChange = (val) => {
  selectedIds.value = val.map(item => item.id)
}

// 根据文件 MIME 判断上传资源类型
const getResourceType = (file) => {
  const type = file?.type || ''
  if (type.startsWith('video/')) return '视频'
  if (type.startsWith('audio/')) return '音频'
  return '图片'
}

// 清空预览
const resetPreviewFiles = () => {
  Object.keys(previewFiles).forEach(key => {
    previewFiles[key] = ''
  })
}

// 文件上传（真实上传，并记录后端返回的访问路径）
const handleUpload = async (field, file) => {
  if (field === 'multimediaPath') {
    previewFiles.multimediaPathName = file.name
  }
  try {
    const res = await uploadFile({ file: file.raw, resourceType: getResourceType(file.raw) })
    const path = ensureOk(res)
    if (!path) throw new Error('上传失败')
    formData[field] = path
    previewFiles[field] = resolveAssetUrl(path)
    ElMessage.success(`${file.name} 上传成功`)
  } catch (e) {
    ElMessage.error(errorMessage(e, '文件上传失败'))
  }
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
  ).then(async () => {
    try {
      const fd = new FormData()
      fd.append('file', file.raw)
      fd.append('dataType', currentDataType.value)
      if (isSubType.value) {
        fd.append('dynastyId', selectedDynastyId.value)
      }
      const res = await request.post('/content/import', fd, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      const data = ensureOk(res)
      const msg = res.data?.msg || '导入完成'
      if (data && data.failCount > 0) {
        ElMessage.warning(msg)
      } else {
        ElMessage.success(msg)
      }
      await loadTableData()
    } catch (e) {
      ElMessage.error(errorMessage(e, '导入失败'))
    }
  }).catch(() => {})
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
  resetPreviewFiles()
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
  resetPreviewFiles()
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
  ).then(async () => {
    try {
      await request.delete(`${apiPrefixMap[currentDataType.value]}/delete/${id}`)
      ElMessage.success('删除成功!')
      await loadTableData()
    } catch (e) {
      ElMessage.error(errorMessage(e, '删除失败'))
    }
  }).catch(() => {})
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
  ).then(async () => {
    try {
      await request.delete(`${apiPrefixMap[currentDataType.value]}/delete/batch`, {
        data: selectedIds.value
      })
      ElMessage.success('批量删除成功!')
      selectedIds.value = []
      await loadTableData()
    } catch (e) {
      ElMessage.error(errorMessage(e, '批量删除失败'))
    }
  }).catch(() => {})
}

// 数字字段转换（空值转 null）
const toNumberOrNull = (val) => {
  if (val === '' || val === null || val === undefined) return null
  const num = Number(val)
  return Number.isNaN(num) ? null : num
}

// 组装提交给后端的数据
const buildPayload = () => {
  const type = currentDataType.value
  const payload = { ...formData }
  delete payload.createdTime
  delete payload.updatedTime
  if (type === 'dynasty') {
    payload.startYear = toNumberOrNull(payload.startYear)
    payload.endYear = toNumberOrNull(payload.endYear)
  } else if (type === 'dynastyStage') {
    payload.dynastyId = toNumberOrNull(payload.dynastyId)
    payload.startYear = toNumberOrNull(payload.startYear)
    payload.endYear = toNumberOrNull(payload.endYear)
  } else if (type === 'historicalEvent') {
    payload.dynastyId = toNumberOrNull(payload.dynastyId)
    payload.stageId = toNumberOrNull(payload.stageId)
    payload.eventYear = toNumberOrNull(payload.eventYear)
  } else if (type === 'historicalPerson') {
    payload.dynastyId = toNumberOrNull(payload.dynastyId)
    payload.birthYear = toNumberOrNull(payload.birthYear)
    payload.deathYear = toNumberOrNull(payload.deathYear)
    if (!Array.isArray(payload.tags)) payload.tags = []
  } else if (type === 'allusion') {
    payload.dynastyId = toNumberOrNull(payload.dynastyId)
  }
  return payload
}

// 提交表单
const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    const prefix = apiPrefixMap[currentDataType.value]
    const payload = buildPayload()
    try {
      if (dialogType.value === 'add') {
        await request.post(`${prefix}/add`, payload)
      } else {
        await request.put(`${prefix}/update`, payload)
      }
      dialogVisible.value = false
      resetPreviewFiles()
      ElMessage.success(`${dialogType.value === 'add' ? '新增' : '编辑'}成功`)
      await loadTableData()
    } catch (e) {
      ElMessage.error(errorMessage(e, '保存失败'))
    }
  })
}

// 初始化
onMounted(() => {
  loadDynastyList()
  loadTableData()
})
</script>

<style scoped>
.data-management-container {
  padding: 20px;
  background: #fff;
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

/* 上传预览样式 */
.upload-preview-container {
  margin-top: 10px;
}

.preview-title {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.image-preview {
  max-width: 200px;
  max-height: 200px;
  border: 1px solid #eee;
  border-radius: 4px;
}

.avatar-preview {
  max-width: 100px;
  max-height: 100px;
  border: 1px solid #eee;
  border-radius: 4px;
}

.video-preview {
  max-width: 300px;
  max-height: 200px;
  border: 1px solid #eee;
  border-radius: 4px;
}

.audio-preview {
  margin-top: 8px;
}

.no-preview-text {
  color: #999;
  font-size: 14px;
  padding: 8px 0;
}

.file-name-text {
  color: #666;
  font-size: 14px;
  margin-top: 8px;
  word-break: break-all;
}
</style>