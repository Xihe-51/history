<template>
  <div class="note-editor">
    <el-input v-model="form.title" placeholder="笔记标题（必填）" maxlength="200" show-word-limit class="mb10" />

    <el-select v-model="form.type" style="width:100%" class="mb10">
      <el-option label="学习笔记" value="study" />
      <el-option label="心得笔记" value="insight" />
      <el-option label="收藏笔记" value="collect" />
      <el-option label="自定义" value="custom" />
    </el-select>

    <el-input v-model="tagText" placeholder="标签（最多5个，用逗号分隔），如：李白,唐诗" class="mb10" />

    <div class="editor-wrapper">
      <QuillEditor ref="quillRef" v-model:content="form.content" content-type="html" theme="snow" />
    </div>

    <div class="editor-footer">
      <span class="hint">{{ isEdit ? '编辑模式' : '新建笔记' }}</span>
      <div>
        <el-button @click="emit('cancel')">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存笔记</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { addNote, getNoteDetail, updateNote } from '@/api/note'
import { errorMessage } from '@/utils/request'

const props = defineProps<{ noteId?: number | null }>()
const emit = defineEmits(['saved', 'cancel'])

const quillRef = ref()
const saving = ref(false)
const isEdit = ref(false)

const form = ref({
  title: '',
  type: 'study',
  content: '',
  tags: [] as string[]
})

const tagText = ref('')

const MAX_TAGS = 5
const MAX_TAG_LENGTH = 20

// 修复：抽屉/弹窗每次打开都会复用组件实例，必须在挂载时按 noteId 加载，
// 并且切换 noteId 时重置表单，否则会串数据。
onMounted(async () => {
  await loadNote()
})

async function loadNote() {
  if (!props.noteId) {
    form.value = { title: '', type: 'study', content: '', tags: [] }
    tagText.value = ''
    isEdit.value = false
    return
  }
  try {
    const res = await getNoteDetail(props.noteId)
    // 后端对“不存在/无权限”返回 code=500，这里统一处理，避免把错误信息当成笔记渲染
    if (res.data.code !== 200 || !res.data.data) {
      ElMessage.error(res.data.msg || '笔记加载失败')
      emit('cancel')
      return
    }
    const note = res.data.data
    form.value = {
      title: note.title || '',
      type: note.type || 'study',
      content: note.content || '',
      tags: Array.isArray(note.tags) ? note.tags : []
    }
    tagText.value = form.value.tags.join(',')
    isEdit.value = true
  } catch (e: any) {
    ElMessage.error(errorMessage(e, '笔记加载失败'))
    emit('cancel')
  }
}

function parseTags(): string[] {
  return tagText.value
    .split(/[,，、]/)
    .map(t => t.trim())
    .filter(Boolean)
}

async function save() {
  const title = form.value.title.trim()
  if (!title) {
    ElMessage.warning('请输入标题')
    return
  }

  const tags = parseTags()
  if (tags.length > MAX_TAGS) {
    ElMessage.warning(`单条笔记最多支持 ${MAX_TAGS} 个标签`)
    return
  }
  const tooLong = tags.find(t => t.length > MAX_TAG_LENGTH)
  if (tooLong) {
    ElMessage.warning(`标签「${tooLong}」超过 ${MAX_TAG_LENGTH} 个字符`)
    return
  }

  // 修复：原实现直接把 Quill 的 content 对象提交给后端，导致富文本内容丢失/格式错误
  const html = typeof form.value.content === 'string'
    ? form.value.content
    : quillRef.value?.getHTML?.() || ''

  const payload = { title, type: form.value.type, content: html, tags }

  saving.value = true
  try {
    const res = isEdit.value && props.noteId
      ? await updateNote({ ...payload, id: props.noteId })
      : await addNote(payload)

    if (res.data.code === 200) {
      ElMessage.success(res.data.msg || (isEdit.value ? '修改成功' : '创建成功'))
      emit('saved')
    } else {
      ElMessage.error(res.data.msg || '保存失败')
    }
  } catch (e: any) {
    ElMessage.error(errorMessage(e, '保存失败'))
  } finally {
    saving.value = false
  }
}
</script>

<style>
.note-editor { padding: 4px 4px 0; }
.note-editor .mb10 { margin-bottom: 10px; }
.editor-wrapper { background: #fff; }
.note-editor .ql-editor { min-height: 320px; max-height: 50vh; overflow-y: auto; }
.editor-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
}
.editor-footer .hint { color: #999; font-size: 13px; }
</style>
