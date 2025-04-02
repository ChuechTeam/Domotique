<template>
    <div class="w-100">
        <div class="level-bar-container">
            <div class="graduations">
                <div class="graduation" v-for="i in graduationCount" :key="i"
                    :style="{ left: ((i - 1) / graduationCount * 100) + '%' }"></div>
            </div>
            <div class="level-bar" :style="{ width: progressPercentage + '%' }"></div>
            <div class="tick" v-for="(value, key) in info" :key="key"
                :style="{ left: Math.min(99.67, (value / maxVal) * 100) + '%' }">
                <div class="tick-content">
                    <div class="tick-label">{{ levelLabels[key] }}</div>
                    <div class="tick-value">
                        <div>{{ value }}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { levelLabels } from '@/labels';
import { useLevelInfoStore } from '@/stores/levelInfo';
import { computed, toRaw } from 'vue';

const li = useLevelInfoStore();

const info = structuredClone(toRaw(await li.fetchLevelInfo()));
delete info["BEGINNER"];

const maxVal = Math.max(...Object.values(info));

// Props definition
const props = defineProps({
    value: {
        type: Number,
        required: true
    },
    graduationCount: {
        type: Number,
        default: 20
    }
});

// Computed property for the progress percentage
const progressPercentage = computed(() => {
    const clamped = Math.min(Math.max(0, props.value), maxVal);
    return (clamped / maxVal) * 100;
});
</script>

<style scoped>
.level-bar-container {
    width: 100%;
    height: 20px;
    background-color: #d4d4d4;
    border-radius: 4px;

    margin-top: 3.5rem;
    margin-bottom: 1.5rem;

    position: relative;
    overflow: visible;

    --tick-line-col: rgb(250, 217, 73);
}

.graduations {
    position: absolute;
    width: 100%;
    height: 100%;
    z-index: 5;
    pointer-events: none;
}

.graduation {
    position: absolute;
    height: 50%;
    top: 25%;
    width: 1px;
    background-color: rgba(0, 0, 0, 0.2);
}

.graduation:nth-child(5n) {
    height: 70%;
    top: 15%;
    width: 1.5px;
    background-color: rgba(0, 0, 0, 0.4);
}

.level-bar {
    height: 100%;
    background-color: #4caf50;
    transition: width 0.3s ease;
    border-radius: 4px;
    position: relative;
    z-index: 2;
    overflow: visible;
}

.tick {
    position: absolute;
    top: -5px;
    z-index: 10;
    border-left: 3px solid var(--tick-line-col);
    height: 30px;
}

.tick-label {
    transform: translateY(-100%);

    background-color: color-mix(in oklab, var(--tick-line-col) 66%, white);
    color: black;
    padding: 4px 8px;
    border-radius: 16px;

    border: 3px solid var(--tick-line-col);
}

.tick-content {
    transform: translateX(-50%);
    position: relative;
}

.tick-value {
    transform: translateY(50%);

    bottom: -5px;
    left: 0;
    right: 0;
    position: absolute;

    display: flex;

    &>* {
        margin: 0 auto;

        background-color: var(--tick-line-col);
        padding: 1px 6px;
        min-width: 6ch;
        text-align: center;

        border-radius: 6px;
    }
}
</style>