<script setup lang="ts">
import { computed, ref, toRaw } from 'vue';
import { DevicesApi } from '@/api/gen/apis/DevicesApi';
import type { PowerLog } from '@/api/gen/models/PowerLog';
import Checkbox from 'primevue/checkbox';
import Button from 'primevue/button';

// Define props
interface Props {
    deviceId: number;
}

type PoweredPeriod = {
    start: Date;
    end: Date;
    totalConsumption: number;
    powered: boolean;
    live: boolean;
}

const props = defineProps<Props>();
const settings = defineModel<{ offPeriods: boolean }>();

// Create API instance
const api = new DevicesApi();

// Fetch power logs using async/await directly in setup
const powerLogs = ref<PowerLog[]>([]);
const response = await api.getPowerLogs({ deviceId: props.deviceId });
powerLogs.value = response.logs;

// const includeOffPeriods = ref(false);
const poweredPeriods = computed(() => calculatePeriods(powerLogs.value, settings.value.offPeriods));

// Logs are sorted by time in descending order
function calculatePeriods(logs: PowerLog[], includeOff: boolean): PoweredPeriod[] {
    const periods: PoweredPeriod[] = [];
    for (let i = logs.length - 1; i >= 0; i--) {
        const earlier = logs[i];
        const later = logs[i - 1];

        const start = earlier.time;
        const end = later?.time ?? new Date();

        const live = later == null;

        const powered = earlier.status === "POWER_ON";
        if (!powered && !includeOff) {
            continue;
        }

        const period: PoweredPeriod = {
            start,
            end,
            powered,
            live,
            totalConsumption: powered ?
                (end.getTime() - start.getTime()) / 1000 * earlier.energyConsumption / 3600 :
                0,
        };
        periods.push(period);
    }

    // We need time in descending order
    periods.reverse();

    return periods;
}

function formatDate(date: Date): string {
    return date.toLocaleString("fr-FR", { timeStyle: "short", dateStyle: "medium" });
}
function formatTime(date: Date): string {
    return date.toLocaleString("fr-FR", { timeStyle: "short" });
}
function sameDay(d1: Date, d2: Date): boolean {
    return d1.getDate() === d2.getDate() && d1.getMonth() === d2.getMonth() && d1.getFullYear() === d2.getFullYear();
}

function exportCsv() {
    // Create CSV header
    const header = 'start_date,end_date,status,totalConsumption\n';

    // Convert periods to CSV rows
    const csvContent = poweredPeriods.value.map(period => {
        const status = period.powered ? 'ON' : 'OFF';
        const startDate = period.start.toISOString();
        const endDate = period.end.toISOString();
        return `${startDate},${endDate},${status},${period.totalConsumption}`;
    }).join('\n');

    // Combine header and content
    const fullCsv = header + csvContent;

    // Create download link
    const blob = new Blob([fullCsv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `device-${props.deviceId}-logs.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}
</script>

<template>
    <div>
        <header class="header">
            <h2>Historique d'activité</h2>
            <div class="header-controls">
                <div class="toggle-container">
                    <Checkbox v-model="settings.offPeriods" :binary="true" inputId="includeOffPeriods" />
                    <label for="includeOffPeriods" class="checkbox-label">Afficher périodes inactives</label>
                </div>
                <Button label="Exporter au format CSV" icon="pi pi-download" @click="exportCsv" size="small" />
            </div>
        </header>
        <div v-if="powerLogs.length === 0" class="no-logs">
            No power logs available for this device.
        </div>

        <div v-else class="device-logs">
            <div class="periods-list">
                <div v-for="(period, index) in poweredPeriods" :key="index" class="period-item"
                    :class="{ 'period-off': !period.powered, 'period-live': period.live }">
                    <span class="period-icon" :class="period.powered ? 'pi pi-power-off' : 'pi pi-ban'"></span>
                    <span class="period-time">
                        <span style="white-space: pre;">{{ formatDate(period.start) + ' ' }}</span>
                        <div>
                            <span class="pi pi-arrow-right arrow-icon"></span>
                            <span style="white-space: pre;">&nbsp;</span>
                            <span v-if="!period.live">{{ sameDay(period.start, period.end) ?
                                formatTime(period.end) : formatDate(period.end) }}</span>
                            <span v-else>Maintenant</span>
                        </div>
                    </span>
                    <div class="period-numbers">
                        <span class="period-duration">{{ Math.round((period.end.getTime() - period.start.getTime()) /
                            60000) }} minutes</span>
                        <span v-if="period.powered" class="period-energy">{{
                            period.totalConsumption.toLocaleString("fr-FR", {
                                minimumFractionDigits: 0, maximumFractionDigits: 1, useGrouping: true, style: 'decimal'
                            }) }}
                            Wh</span>
                        <span v-else class="period-energy">0 Wh</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
h3 {
    margin-bottom: 0.75rem;
    color: #333;
    font-size: 1rem;
}

.periods-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.arrow-icon {
    font-size: unset;
    vertical-align: auto;
}

.period-icon {
    flex: 0;
}

.period-item {
    display: flex;
    align-items: center;
    background-color: #139643;
    color: white;
    border-radius: 12px;
    padding: 0.6rem 0.75rem;
    box-shadow: 0 1px 6px #0f863b2d;
    font-size: 0.9rem;

    position: relative;


    overflow: visible;

    gap: 12px;
}

.period-live::before {
    content: '';
    position: absolute;
    top: -4px;
    left: -4px;
    right: -4px;
    bottom: -4px;
    background-color: #f0fff5;
    border: 1px solid #139643;
    border-radius: 12px;

    z-index: -1;
}

.period-off {
    background-color: rgb(241, 241, 241);
    border: 1px solid #ccc;
    color: #666;
    opacity: 0.8;
    box-shadow: none;

    & .period-energy {
        opacity: 0.75;
    }
}

.period-time {
    flex-grow: 1;
    flex-shrink: 0.5;

    display: flex;
    flex-wrap: wrap;
}

.period-numbers {
    display: flex;
    gap: 0.5rem;
    flex-shrink: 1;
    flex-wrap: wrap;

    justify-content: flex-end;
    width: auto;

    &>* {
        width: max-content;
        text-align: right;
    }
}

.period-energy {
    font-weight: bold;
    min-width: 10ch;
}

.no-logs {
    padding: 1rem;
    text-align: center;
    color: #666;
    font-style: italic;
}

.header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 0.5rem;
    flex-wrap: wrap;
    gap: 0.5rem;
}

.header-controls {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    flex-wrap: wrap;
}

.toggle-container {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.checkbox-label {
    font-size: 0.9rem;
    color: #666;
    cursor: pointer;
    white-space: nowrap;
}

@media (max-width: 768px) {
    .header {
        flex-direction: column;
        align-items: flex-start;
    }

    .header-controls {
        width: 100%;
        justify-content: space-between;
    }
}
</style>
