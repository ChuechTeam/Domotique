<script lang="ts" setup>
import api, { CompleteDevice } from '@/api';
import DeviceCard from '@/components/DeviceCard.vue';
import { ref, watch } from 'vue';

const devices = ref<CompleteDevice[]>([]);
const filters = ref({
    name: null as string | null,
    powered: null as boolean | null,
});
const promise = ref<ReturnType<typeof api.devices.getDevices>>(null);
const searchTimer = ref<ReturnType<typeof setTimeout>>(null);

function load() {
    let thisProm: any;
    async function fetchFromAPI() {
        try {
            const f = filters.value;
            const response = await api.devices.getDevices({
                name: f.name,
                powered: f.powered
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

watch(filters, () => {
    clearTimeout(searchTimer.value);
    searchTimer.value = setTimeout(load, 150);
}, { deep: true });
load(); // Initial load

async function togglePower(id: number) {
    try {
        // Find the device in our list
        const deviceIndex = devices.value.findIndex(d => d.id === id);
        if (deviceIndex === -1) return;
        
        const device = devices.value[deviceIndex];
        const newPoweredState = !device.powered;
        
        // Update the device via API
        await api.devices.patchDevice({
            deviceId: id,
            devicePatchInput: {
                // Keep all existing values but toggle the powered state
                powered: newPoweredState,
                name: device.name,
                energyConsumption: device.energyConsumption,
                typeId: device.type.id,
                attributes: device.attributes,
                // Optional fields
                description: device.description,
                roomId: device.room?.id,
                userId: device.owner?.id
            }
        });
        
        // Update the local device data with the new state
        devices.value[deviceIndex] = {
            ...device,
            powered: newPoweredState
        };
    } catch (error) {
        console.error('Failed to toggle device power:', error);
        // You might want to add error handling/notification here
    }
}
</script>
<template>
    <div class="box container" v-if="$route.name === 'devices'">
        <div class="-filters">
            <div class="filter-section">
                <h3>Filtres</h3>

                <div class="filter-item">
                    <label for="name-filter">Nom</label>
                    <InputText id="name-filter" v-model="filters.name" placeholder="Rechercher par nom" />
                </div>

                <div class="filter-item">
                    <label for="powered-filter">État</label>
                    <Select id="powered-filter" v-model="filters.powered"
                        :options="[[null, 'Peu importe'], [true, 'Allumé'], [false, 'Éteint']]"
                        :option-label="x => x[1]" :option-value="x => x[0]" placeholder="Peu importe" />
                </div>

                <Button label="Réinitialiser" icon="pi pi-refresh" @click="filters = { name: null, powered: null }"
                    class="reset-button" />
            </div>
        </div>
        <div class="-results">
            <div v-if="devices.length === 0" class="no-results">
                Aucun appareil trouvé.
            </div>
            <div v-else class="device-grid">
                <DeviceCard 
                    v-for="device in devices" 
                    :key="device.id" 
                    :device="device" 
                    @toggle-power="togglePower"
                />
            </div>
        </div>
    </div>
    <RouterView v-else />
</template>
<style lang="css" scoped>
.box {
    display: grid;
    grid-template:
        "filters results" 1fr / minmax(320px, auto) minmax(auto, 1280px);

    height: 100%;
    flex: 1;
    padding-top: 2rem;

    & .-banner {
        max-width: calc(400px + 1280px);
        width: 100%;

        margin-left: auto;
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
    }
}

.filter-section {
    display: flex;
    flex-direction: column;
    gap: 1rem;

    max-width: 400px;
    margin-left: auto;
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
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
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