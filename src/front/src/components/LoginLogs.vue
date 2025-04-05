<script lang="ts" setup>
import api, { LoginLog, UserProfile } from '@/api';
import { computed, ref, watch } from 'vue';

const props = defineProps<{ userId?: number, showUser?: boolean }>();
const logs = ref<LoginLog[]>();
const users = ref<Record<number, UserProfile>>({});
const err = ref(false);
const showUser = computed(() => props.showUser !== false);
try {
    const res = await api.loginLogs.getLoginLogs({
        userId: props.userId ?? undefined
    });
    logs.value = res.logs;
    for (const u of res.users) {
        users.value[u.id] = u;
    }
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
            <RouterLink class="user-link" v-if="showUser" :to="{ name: 'profile', params: { userId: l.userId } }">{{ 
                users[l.userId].firstName + ' ' + users[l.userId].lastName
            }}</RouterLink>
            <strong class="d-block">Connexion réussie</strong>
            <span style="font-size: 0.8em;" class="opacity-75"><i class="pi pi-clock" style="font-size: 1em;"></i> {{ formatDate(l.time) }}</span>   
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

.user-link {
    display: block;
    text-decoration: none;
}
</style>