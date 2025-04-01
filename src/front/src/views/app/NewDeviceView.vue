<script lang="ts" setup>
import DeviceForm, { defaultFormModel } from '@/components/DeviceForm.vue';
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const data = ref(defaultFormModel());
</script>

<template>
    <DeviceForm class="container-lg mt-4"
        v-model="data"
        @save-success="x => router.replace({ name: 'device-detail', params: { deviceId: x.id } })"
        @cancel="router.back()" />
</template>

<style lang="css" scoped></style>