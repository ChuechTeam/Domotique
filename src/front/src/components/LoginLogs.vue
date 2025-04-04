<script lang="ts" setup>
import api, { LoginLog } from '@/api';
import { computed, ref, watch } from 'vue';

const props = defineProps<{ userId: number }>();
const logs = ref<LoginLog[]>();
const err = ref(false);
try {
    logs.value = (await api.loginLogs.getLoginLogs({
        userId: props.userId
    })).logs;
} catch (e) {
    console.error("Failed to fetch login logs", e);
    err.value = true;
}


// Format date in French
const formatDate = (d: Date) => {
  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'full',
    timeStyle: 'medium'
  }).format(d);
};

</script>

<template>
    <ul class="list-unstyled">
        <li v-for="l in logs" class="log">
            <strong class="d-block">Connexion réussie</strong>
            <span>{{ formatDate(l.time) }}</span>   
        </li>
    </ul>
</template>

<style lang="css" scoped>
.log {
    border-left: 4px solid rgb(10, 123, 10);
    padding: 0.5rem 1rem;
    margin: 1rem 0;
    border-radius: 4px;
}
</style>