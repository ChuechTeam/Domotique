<script setup lang="ts">
import api, { AttributeType, CompleteDevice, HealthStatus, HealthValue } from '@/api';
import { attributeTypeContents, attributeTypeLabels } from '@/labels';
import { Knob } from 'primevue';
import { attributeTypeFormats } from '@/labels';
import { computed, ref } from 'vue';
import DeviceCard from '@/components/DeviceCard.vue';
import FullscreenSpinner from '@/components/FullscreenSpinner.vue';

// Placeholder for health theme page
const health = await api.health.getMyHealth();
const devicesModalVisible = ref(false);
const devicesModalDevices = ref<CompleteDevice[]>([]);
const devicesModalPromise = ref(null);

api.userEvents.reportHealthCheck();

function openDevicesModal(ids: number[]) {
    let thePromise: any
    async function doIt() {
        try {
            const result = (await api.devices.getDevices({
                ids: ids,
            })).devices;

            if (thePromise === devicesModalPromise.value) {
                devicesModalDevices.value = result;
            }
        } finally {
            if (thePromise === devicesModalPromise.value) {
                devicesModalPromise.value = null;
            }
        }
    }

    thePromise = devicesModalPromise.value = doIt()
    devicesModalVisible.value = true;
}

function missingDataMsg(attr: AttributeType) {
    switch (attr) {
        case "BODY_WEIGHT":
            return "La taille est manquante !"
        default:
            return "Données manquantes !"
    }
}

const statusOrder: Record<HealthStatus, number> = {
    'WARNING': 0,
    'HEALTHY': 1,
    'MISSING_DATA': 2,
    'IGNORED': 3,
}

const values = computed(() => {
    if (health == null) {
        return [];
    }
    return Object.entries(health.values).sort((a, b) => {
        return statusOrder[a[1].status] - statusOrder[b[1].status];
    }) as [AttributeType, HealthValue][];
})

function formatNum(n: number, type: AttributeType) {
    let nstr;

    if (n > 1e100) {
        nstr = /* infinite symbol */ "\u221E";
    } else if (n < -1e100) {
        nstr = /* negative infinite symbol */ "-\u221E";
    } else {
        nstr = n.toLocaleString(undefined, {
            minimumFractionDigits: 0,
            maximumFractionDigits: 2,
        });
    }
    return nstr + (attributeTypeFormats[type].suffix ?? "");
}

function formatInterval(min: number, max: number, type: AttributeType) {
    if (max > 1e100 && min < -1e100) {
        return "inconnu";
    } else if (max > 1e100) {
        return formatNum(min, type) + " minimum";
    } else if (min < -1e100) {
        return formatNum(max, type) + " maximum";
    } else {
        return formatNum(min, type) + " à " + formatNum(max, type);
    }
}

</script>

<template>
    <div class="health-theme">
        <div class="container-lg">
            <h1 class="sub-page-title">Ma santé</h1>
            <div class="values" v-if="values.length > 0">
                <Card v-for="[a, v] in values" class="attr-card"
                    :class="{ '-caution': v.status === 'WARNING', '-unknown': v.status === 'MISSING_DATA' }" :pt="{
                        body: ['flex-grow-1'],
                        content: ['flex-grow-1'],
                    }">
                    <template #header>
                        <div class="pt-3 ps-3" style="min-height: 3rem;">
                            <h3 class="fw-bold m-0" style="font-size: 1.3em;">{{ attributeTypeLabels[a] }}</h3>
                            <div v-if="v.status === 'MISSING_DATA'" class="opacity-75" style="font-size: 0.85em;">
                                <i class="pi pi-exclamation-triangle"></i> {{ missingDataMsg(a as any) }}
                            </div>
                            <div v-else-if="v.range" style="font-size: 0.85em;">
                                <i :class="v.status === 'HEALTHY' ? 'pi pi-check' : 'pi pi-exclamation-triangle'"
                                    style="font-size: 1em; vertical-align: baseline;"></i>
                                Intervalle sain : {{ formatInterval(v.range.min, v.range.max, a) }}
                            </div>
                        </div>
                    </template>
                    <template #content>
                        <div class="h-100 d-flex align-items-center justify-content-center">
                            <Knob v-if="typeof v.value === 'number' && v.range != null"
                                :min="Math.min(v.range.min, v.value)" :max="Math.max(v.range.max, v.value)"
                                :model-value="v.value" readonly
                                :value-template="x => formatNum(x, a)" :size="150">
                            </Knob>
                            <Knob v-else-if="typeof v.value === 'number'" :size="150" :min="v.value" :max="v.value"
                                :value-template="x => x + attributeTypeFormats[a].suffix" :model-value="v.value" />
                        </div>
                    </template>
                    <template #footer>
                        <Button severity="success" fluid icon="pi pi-microchip" label="Voir les appareils"
                            @click="openDevicesModal(v.deviceIds)"></Button>
                    </template>
                </Card>
            </div>
            <div class="no-val" v-else>
                <div class="text-center"><span class="pi pi-exclamation-triangle -icon"></span></div>
                <p>
                    Vous n'avez aucun appareil comportant des données de santé. <br>Ajoutez-en pour obtenir des
                    informations
                    sur votre santé !
                </p>
            </div>

            <Dialog modal v-model:visible="devicesModalVisible" header="Appareils associés"
                style="min-width: min(97vw, 500px);">
                <FullscreenSpinner v-if="devicesModalPromise != null" />
                <div class="values mt-2" v-else-if="devicesModalDevices.length > 0">
                    <DeviceCard v-for="d in devicesModalDevices" :model-value="d" />
                </div>
            </Dialog>
        </div>
    </div>
</template>
<style scoped>
@property --suffix {
    syntax: "<string>";
    inherits: true;
}

.health-theme {
    display: flex;
    flex-direction: column;
    height: 100%;

    background-color: rgb(236, 253, 245);
}

:root {
    --subnav-bg-left: rgb(65, 145, 89);
    --subnav-bg-right: rgb(8, 150, 80);
    --subnav-shadow-col: rgb(8, 150, 80);
}

.no-val {
    text-align: center;
    background-color: rgb(198, 224, 211);
    padding: 2rem;
    border-radius: 8px;
    font-size: 1.5em;

    & .-icon {
        font-size: 4em;
        margin-bottom: 2rem;
    }
}

:deep(.p-knob-text) {
    font-size: 1em;
}

.attr-card {
    --p-card-background: rgba(8, 150, 80, 5%);
    border: 1px solid rgb(8, 150, 80);
    --p-card-shadow: 0px 0px 8px rgb(8, 150, 80, 33%);

    &.-caution {
        --p-card-background: rgba(255, 255, 0, 20%);
        border: 1px solid rgb(126, 126, 0);
        --p-card-shadow: 0px 0px 8px rgb(255, 255, 0, 33%);

        --p-knob-value-background: orange;
        --p-knob-range-background: rgb(255, 216, 144);
    }

    &.-unknown {
        border: 1px solid rgb(5, 44, 25);
        --p-card-background: rgba(0, 0, 0, 0.02);
        opacity: 0.66;
    }
}

.values {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));

    gap: 24px;
}
</style>