<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>
<template>
  <!-- 修复：原来引用了不存在的 <GlobalNoteWidget />，导致 Vue 运行时警告 -->
  <RouterView />
</template>

<style>
body, html {
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
  color: #000000;
}
</style>
