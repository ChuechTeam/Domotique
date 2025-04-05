<script lang="ts" setup>
import FullscreenSpinner from '@/components/FullscreenSpinner.vue';
import LoginLogs from '@/components/LoginLogs.vue';
import { ref, onMounted } from 'vue';
</script>

<template>
  <div class="w-100">
    <div class="container-lg">
      <h1 class="sub-page-title">Historique des connexions</h1>

      <Suspense>
        <LoginLogs show-user />

        <template #fallback>
          <FullscreenSpinner />
        </template>
      </Suspense>
    </div>
  </div>
</template>

<style lang="css" scoped>
.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.content {
  margin-top: 1rem;
}
</style>