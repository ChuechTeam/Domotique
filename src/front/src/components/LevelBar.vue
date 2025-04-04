<template>
    <div class="root">
        <div class="bar-row">
            <div class="points">{{ value }}</div>
            <div class="level-bar-container">
                <div class="graduations">
                    <div class="graduation" v-for="i in graduationCount" :key="i"
                        :style="{ left: ((i - 1) / graduationCount * 100) + '%' }"></div>
                </div>
                <div class="level-bar" :style="{ width: progressPercentage + '%' }"></div>
                <div class="tick" v-for="(value, key) in info" :key="key"
                    :style="{ left: Math.min(99.9, (value / maxVal) * 100) + '%' }"
                    :class="{ '-done': value <= props.value }">
                    <div class="tick-content">
                        <div class="tick-label"><span v-if="value <= props.value" class="pi pi-check me-1"></span>{{ levelLabels[key] }}</div>
                        <div class="tick-value">
                            <div class="tick-value-box">{{ value }}</div>
                        </div>
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
.root {
    width: 100%;

    --tick-line-col: rgb(250, 217, 73);
    --tick-line-col-dim: rgb(128, 91, 24);

    --bar-height: 22px;
}

.points {
    background-color: var(--tick-line-col);

    text-align: center;
    min-width: 8ch;
    border-radius: 4px;

    margin-right: -12px;
    padding-right: 6px;

    height: calc(var(--bar-height) + 6px);

    display: flex;
    align-items: center;
    justify-content: center;

    font-weight: bold;
}

.bar-row {
    width: 100%;
    display: flex;
    align-items: center;

    margin-top: 3.5em;
    margin-bottom: 1.5em;
}

.level-bar-container {
    width: 100%;
    height: var(--bar-height);
    background-color: #d4d4d4;
    border-radius: 4px;

    border: 3px solid var(--tick-line-col);
    box-sizing: content-box;

    position: relative;
    overflow: visible;
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
    height: 41px;

    width: max-content;
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
    height: 100%;
}

.tick-value {
    transform: translateY(50%);

    bottom: -5px;
    left: 0;
    right: 0;
    position: absolute;

    display: flex;
}

.tick-value-box {
    margin: 0 auto;

    background-color: var(--tick-line-col);
    padding: 1px 6px;
    min-width: 6ch;
    text-align: center;

    border-radius: 6px;
}

.tick.-done .tick-value-box {
    background-color: var(--tick-line-col-dim);
    color: white;
}

.tick.-done .tick-label {
    background-color: var(--tick-line-col-dim);
    border-color: var(--tick-line-col-dim);
    color: rgba(255, 255, 255, 90%);
}

.tick.-done {
    border-color: var(--tick-line-col-dim);
    opacity: 1.0;
}

@media (max-width: 768px) {
    .tick-content {
        font-size: 0.7em;

        & .pi {
            display: none;
        }
    }

    .points {
        font-size: 0.8em;
    }
}
</style>