<script lang="ts" setup>
import api, { CompleteDevice, DeviceCategory } from '@/api';
import DeviceCard from '@/components/DeviceCard.vue';
import FullscreenSpinner from '@/components/FullscreenSpinner.vue';
import { useGuards } from '@/guards';
import { deviceCategories, deviceCategoryLabels } from '@/labels';
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

const guards = useGuards()
const router = useRouter();

const devices = ref<CompleteDevice[]>([]);
const filters = ref({
    name: null as string | null,
    powered: null as boolean | null,
    category: null as DeviceCategory | null,
});
const promise = ref<ReturnType<typeof api.devices.getDevices>>(null);
const searchTimer = ref<ReturnType<typeof setTimeout>>(null);

const loading = computed(() => promise.value != null);

function load() {
    let thisProm: any;
    async function fetchFromAPI() {
        try {
            const f = filters.value;
            const response = await api.devices.getDevices({
                name: f.name,
                powered: f.powered,
                category: f.category
            });
            if (thisProm === promise.value) {
                devices.value = response.devices;
            }
        } catch (e) {
            // todo
        } finally {
            if (thisProm === promise.value) {
                promise.value = null;
            }
        }
    }
    thisProm = fetchFromAPI()
    promise.value = thisProm;
}

function removeDeletedDevice(id: number) {
    for (let i = 0; i < devices.value.length; i++) {
        if (devices.value[i].id === id) {
            devices.value.splice(i, 1);
            return;
        }
    }
}

function updateEditedDevice(device: CompleteDevice) {
    for (let i = 0; i < devices.value.length; i++) {
        if (devices.value[i].id === device.id) {
            devices.value[i] = device;
            return;
        }
    }
}

watch(filters, () => {
    // Trigger refresh when changing filters.
    clearTimeout(searchTimer.value);
    searchTimer.value = setTimeout(load, 150);
}, { deep: true });
load(); // Initial load

function createNewDevice() {
    if (!guards.mustManage()) {
        return;
    }

    router.push("/tech/devices/new");
}
</script>
<template>
    <div>
        <div class="box container-lg" v-if="$route.name === 'devices'">
            <div class="-filters">
                <div class="filter-section">
                    <Button @click="createNewDevice" icon="pi pi-plus" label="Créer un appareil" />

                    <h3>Filtres</h3>

                    <div class="filter-item">
                        <label for="name-filter">Nom</label>
                        <IconField>
                            <InputIcon class="pi pi-search" />
                            <InputText id="name-filter" fluid v-model="filters.name" placeholder="Rechercher par nom" />
                        </IconField>
                    </div>

                    <div class="filter-item">
                        <label for="powered-filter">État</label>
                        <Select id="powered-filter" v-model="filters.powered"
                            :options="[[true, 'Allumé'], [false, 'Éteint']]" :option-label="x => x[1]"
                            :option-value="x => x[0]" placeholder="Peu importe" show-clear />
                    </div>

                    <div class="filter-item">
                        <label for="category-filter">Catégorie</label>
                        <Select id="category-filter" v-model="filters.category" :options="deviceCategories"
                            :option-label="x => deviceCategoryLabels[x]" placeholder="Peu importe" show-clear />
                    </div>

                    <Button label="Réinitialiser" icon="pi pi-refresh"
                        @click="filters = { name: null, powered: null, category: null }" class="reset-button" />
                </div>
            </div>
            <div class="-results">
                <div v-if="loading">
                    <FullscreenSpinner />
                </div>
                <div v-else-if="devices.length === 0" class="no-results">
                    Aucun appareil trouvé.
                </div>
                <div v-else class="device-grid">
                    <DeviceCard v-for="(device, k) in devices" :key="device.id" v-model="devices[k]" />
                </div>
            </div>
        </div>
        <RouterView v-slot="{ Component }">
            <Suspense v-if="Component">
                <template #default>
                    <component :is="Component" @device-deleted="removeDeletedDevice"
                        @device-edited="updateEditedDevice" />
                </template>
                <template #fallback>
                    <FullscreenSpinner />
                </template>
            </Suspense>
        </RouterView>
    </div>
</template>
<style lang="css" scoped>
.box {
    display: grid;
    grid-template:
        "filters results" 1fr / minmax(320px, auto) minmax(auto, 1280px);

    min-height: 0;
    flex: 1;

    & .-results,
    & .-filters {
        padding-top: 2rem;
    }

    & .-results {
        grid-area: results;
        padding-left: 1rem;
        padding-right: 1rem;
        border-left: 1px solid rgba(4, 91, 172, 0.1);
    }

    & .-filters {
        grid-area: filters;
        padding-right: 1em;

        width: 100%;
        height: 100%;
    }
}

.filter-section {
    display: flex;
    flex-direction: column;
    gap: 1rem;

    max-width: 400px;
    margin-left: auto;

    position: sticky;
    top: 32px;
}


@media (max-width: 768px) {
    .box {
        grid-template:
            "filters" auto
            "results" auto / auto;

        min-height: unset;
        gap: 2rem;

        & .-results {
            border: none;
            padding: 0;
            overflow: unset;
        }

        & .-filters {
            padding: 0;
            border: none;
        }

        padding-top: 1rem;
    }

    .filter-section {
        max-width: 100%;
        margin: 0;
    }
}

.filter-item {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.filter-label {
    margin-left: 0.5rem;
}

.reset-button {
    margin-top: 1rem;
    align-self: flex-start;
}

.device-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(min(75vw, 400px), 1fr));
    gap: 1.5rem;
}

.no-results {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 200px;
    color: #666;
    font-style: italic;
    background: rgba(0, 0, 0, 0.02);
    border-radius: 8px;
}
</style>