<template>
  <div class="note-editor">
    <el-input v-model="form.title" placeholder="笔记标题" class="mb10" />

    <el-select v-model="form.type" style="width:100%" class="mb10">
      <el-option label="学习笔记" value="study" />
      <el-option label="心得笔记" value="insight" />
      <el-option label="收藏笔记" value="collect" />
      <el-option label="自定义" value="custom" />
    </el-select>

    <el-input v-model="tagText" placeholder="标签：人物、事件、典故，逗号分隔" class="mb10" />

    <QuillEditor v-model:content="form.content" content-type="html" theme="snow" />

    <div style="text-align:right; margin-top:12px">
      <el-button type="primary" @click="save">保存笔记</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { addNote, getNoteDetail, updateNote } from '@/api/note'

const props = defineProps({ noteId: [Number, null] })
const emit = defineEmits(['saved'])

const form = ref({
  title: '',
  type: 'study',
  content: '',
  tags: [] as string[]
})

const tagText = ref('')

onMounted(async () => {
  if (props.noteId) {
    const res = await getNoteDetail(props.noteId)
    if (res.data.code === 200) {
      form.value = res.data.data
      tagText.value = res.data.data.tags?.join(',') || ''
    }
  }
})

const save = async () => {
  form.value.tags = tagText.value.split(',').map(t => t.trim()).filter(Boolean)
  if (!form.value.title) return ElMessage.warning('请输入标题')

  try {
    if (props.noteId) {
      await updateNote({ ...form.value, id: props.noteId })
      ElMessage.success('修改成功')
    } else {
      await addNote(form.value)
      ElMessage.success('创建成功')
    }
    emit('saved')
  } catch (e) {
    ElMessage.error('保存失败')
  }
}
</script>

<style>
.note-editor { padding: 10px; }
.mb10 { margin-bottom: 10px; }
.ql-editor { min-height: 360px; }
</style>