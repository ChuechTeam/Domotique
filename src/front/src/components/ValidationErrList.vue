<script lang="ts" setup>
import { computed } from 'vue';

const props = defineProps({
    errors: {
        type: Array
    }
});

const errs = computed(() => props.errors as string[] | null);
</script>

<template>
    <ul class="text-danger list" v-if="errs && errs.length > 0">
        <li v-for="e in errors">
            <!-- todo use real icon -->
            <span>❌ </span>
            <span>{{ e }}</span>
        </li>
    </ul>
</template>

<style scoped>
.list {
    list-style-type: none;
    padding-left: 0;
    margin: 0;
    font-size: 0.8rem;
}
</style>