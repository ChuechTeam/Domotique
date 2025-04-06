<script setup lang="ts">
import api, { CompleteDevice, ConsumptionOutput } from '@/api';
import { useGuards } from '@/guards';
import { deviceCategoryLabels } from '@/labels';
import { computed, ref, watch } from 'vue';

const guards = useGuards();

// Placeholder for energy theme page
const consumptionResult = ref<ConsumptionOutput | null>(null);
const consumptionPromise = ref(null);

const deviceContributions = ref<{ dev: CompleteDevice, val: number }[]>([]);
const resolveContributionsPromise = ref(null);

const query = ref({
    start: null as Date | null,
    end: new Date(Date.now()) as Date | null,
})

const consumptionFormated = computed(() => {
    if (consumptionResult.value == null) {
        return '0';
    }
    return consumptionResult.value.totalConsumption.toLocaleString('fr-FR', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 1,
    })
})

const consumptionSentence = computed(() => {
    const q = query.value;
    if (q.start == null || q.end == null) {
        return null;
    }

    const formatOptions = {
        weekday: 'long',
        day: 'numeric',
        month: 'long',
        year: 'numeric'
    } as any;

    const startFormatted = q.start.toLocaleDateString('fr-FR', formatOptions);
    const endFormatted = q.end.toLocaleDateString('fr-FR', formatOptions);

    return `Du ${startFormatted} au ${endFormatted}, l'EHPAD a consommé`;
})

const startMaxValue = computed(() => {
    if (query.value.end == null) {
        return null;
    }
    return query.value.end;
})
const endMinValue = computed(() => {
    if (query.value.start == null) {
        return null;
    }
    return query.value.start;
})

function formatWh(value: number) {
    return value.toLocaleString('fr-FR', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 1,
    })
}

function load() {
    if (query.value.start == null || query.value.end == null) {
        return;
    }

    let thisProm: any = null;
    async function actuallyDoIt() {
        try {
            const result = await api.energy.getTotalConsumption({
                start: query.value.start,
                end: query.value.end,
            });
            if (thisProm === consumptionPromise.value) {
                consumptionResult.value = result;
                loadContributions(result);
            }
        } finally {
            if (thisProm === consumptionPromise.value) {
                consumptionPromise.value = null;
                // deviceContributions.value = [];
            }
        }
    }

    thisProm = consumptionPromise.value = actuallyDoIt()
}

function loadContributions(output: ConsumptionOutput) {
    let thisProm: any = null;
    async function actuallyDoIt() {
        try {
            const result = await api.devices.getDevices({
                ids: output.perDevice.map(c => c.deviceId),
            })

            if (thisProm !== resolveContributionsPromise.value) {
                return;
            }

            const map = new Map<number, CompleteDevice>()
            for (const d of result.devices) {
                map.set(d.id, d)
            }

            deviceContributions.value = output.perDevice.map(c => ({ dev: map.get(c.deviceId), val: c.consumption }))
                .sort((a, b) => b.val - a.val);
        } finally {
            if (thisProm === resolveContributionsPromise.value) {
                resolveContributionsPromise.value = null;
            }
        }
    }

    thisProm = resolveContributionsPromise.value = actuallyDoIt()
}

function exportCSV() {
    if (!guards.mustHaveAdminRights()) {
        return;
    }

    // Create CSV header
    const header = 'device_id,device_name,device_owner_name,device_room_name,device_type_name,device_category,energy_consumption\n';
    
    // Generate CSV content
    const csvContent = deviceContributions.value.map(({ dev, val }) => {
        const deviceId = dev.id;
        const deviceName = dev.name.replace(/,/g, ' '); // Replace commas to avoid CSV parsing issues
        const ownerName = dev.owner ? `${dev.owner.firstName} ${dev.owner.lastName}`.replace(/,/g, ' ') : '';
        const roomName = dev.room ? dev.room.name.replace(/,/g, ' ') : '';
        const typeName = dev.type.name.replace(/,/g, ' ');
        const category = deviceCategoryLabels[dev.type.category];
        const consumption = val;

        return `${deviceId},${deviceName},${ownerName},${roomName},${typeName},${category},${consumption}`;
    }).join('\n');

    // Combine header and content
    const fullCsvContent = header + csvContent;
    
    // Create a Blob containing the CSV data
    const blob = new Blob([fullCsvContent], { type: 'text/csv;charset=utf-8;' });
    
    // Create a temporary URL for the Blob
    const url = URL.createObjectURL(blob);
    
    // Create a link element
    const link = document.createElement('a');
    link.href = url;
    
    // Set the download filename - include date range in the filename if available
    let filename = 'energy_consumption';
    if (query.value.start && query.value.end) {
        const startDate = query.value.start.toISOString().split('T')[0];
        const endDate = query.value.end.toISOString().split('T')[0];
        filename += `_${startDate}_to_${endDate}`;
    }
    link.download = `${filename}.csv`;
    
    // Append the link to the document, trigger click and remove it
    document.body.appendChild(link);
    link.click();
    
    // Clean up
    setTimeout(() => {
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
    }, 100);
}

watch(query, () => {
    load();
}, { deep: true });
</script>

<template>
    <div class="energy-theme">
        <div class="container-lg">
            <h1 class="sub-page-title">Consommation d'énergie</h1>
            <div v-if="consumptionSentence" class="top-sentence">{{ consumptionSentence }}</div>
            <div v-else="consumptionResult == null" class="top-sentence">Sélectionnez une période pour voir la
                consommation d'énergie</div>
            <div class="value">{{ consumptionFormated }} Wh</div>
            <div class="commands">
                <IftaLabel class="flex-grow-1 w-100">
                    <DatePicker v-model="query.start" date-format="dd/mm/yy" showIcon showTime hourFormat="24" fluid
                        :max-date="startMaxValue" />
                    <label>Début</label>
                </IftaLabel>
                <i class="pi pi-arrow-right right-arr"></i>
                <i class="pi pi-arrow-down down-arr"></i>
                <IftaLabel class="flex-grow-1 w-100">
                    <DatePicker v-model="query.end" date-format="dd/mm/yy" showIcon showTime hourFormat="24" fluid
                        :min-date="endMinValue" />
                    <label>Fin</label>
                </IftaLabel>
            </div>

            <div class="contribs mt-4" v-if="deviceContributions.length > 0">
                <header class="contrib-header">
                    <div class="flex-grow-1 header-txt">Consommation par appareil</div>
                    <Button label="Exporter au format CSV" icon="pi pi-file-export" @click="exportCSV" />
                </header>
                <div class="values">
                    <div v-for="{ dev, val } in deviceContributions" class="dev-val">
                        <div class="device-name">
                            <RouterLink :to="{ name: 'device-detail', params: { deviceId: dev.id } }" class="d-block">
                                {{ dev.name }}
                            </RouterLink>

                            <div class="gap-3 d-flex mt-2">
                                <Chip :label="deviceCategoryLabels[dev.type.category]" class="device-chip" />
                                <Chip v-if="dev.owner" class="device-chip">
                                    <i class="pi pi-user"></i>
                                    <RouterLink :to="{ name: 'profile', params: { userId: dev.owner!.id } }"
                                        style="text-decoration: none; color: inherit;" @click.stop>{{
                                            dev.owner!.firstName + ' ' +
                                            dev.owner.lastName }}</RouterLink>
                                </Chip>
                            </div>
                        </div>
                        <div class="device-consumption">{{ formatWh(val) }} Wh</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.energy-theme {
    display: flex;
    flex-direction: column;
    height: 100%;

    background-color: rgb(255, 244, 229);
}

.top-sentence {
    text-align: center;
    font-size: 0.8em;
    margin-bottom: -1rem;
}

.value {
    font-size: 8em;
    text-align: center;
}

.commands {
    display: flex;
    gap: 32px;
    align-items: center;

    max-width: 640px;
    margin: 0 auto;
}

.down-arr {
    display: none;
}

.contribs {
    display: flex;
    flex-direction: column;

    width: 100%;
    max-width: 800px;
    margin: 0 auto;
    padding: 1rem;
    padding-top: 0;

    border-radius: 8px;

    --p-chip-background: #c2692e;
    --p-chip-color: white;
    --p-chip-padding-y: 0.5rem;
}

.device-chip {
    font-size: 0.8rem;
}

.values {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.contrib-header {
    padding: 0.5rem 0;
    & .header-txt {
        font-size: 1.5rem;
        font-weight: bold;
    }

    display: flex;
    align-items: center;
}

.dev-val {
    border: 1px solid #c2692e;
    color: black;
    border-radius: 4px;
    overflow: hidden;

    display: flex;

    &>* {
        padding: 0.75rem;
    }
}

.device-name {
    flex-grow: 1;
    background-color: #ffd8bd;

    &>a {
        color: #642800;
        text-decoration: none;
    }
}

.device-consumption {
    min-width: 128px;
    text-align: right;
    background-color: #c2692e;
    color: white;
    font-weight: bold;

    display: flex;
    align-items: center;
    justify-content: end;
}

@media (max-width: 768px) {
    .value {
        font-size: 4em;
    }

    .commands {
        flex-direction: column;
        gap: 16px;
    }

    .top-sentence {
        margin-bottom: -0.5rem;
    }

    .down-arr {
        display: block;
    }

    .right-arr {
        display: none;
    }

    .dev-val {
        flex-direction: column;
    }
    .device-consumption {
        justify-content: center;
    }
    .contrib-header {
        flex-direction: column;
        gap: 8px;

        & > button {
            width: 100%;
        }
    }

}
</style>