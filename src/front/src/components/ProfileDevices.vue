<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/index.js';
import { useToast } from 'primevue/usetoast';
import DeviceCard from './DeviceCard.vue';

const toast = useToast();
const devices = ref([]);
const loading = ref(true);

const props = defineProps({
    userId: {
        type: [Number, String],
        required: true
    }
});

// Function to toggle device power state
const toggleDevicePower = async (device) => {
    try {
        await api.devices.patchDevice({
            deviceId: device.id,
            devicePatchInput: {
                powered: !device.powered
            }
        });

        // Update local device state
        device.powered = !device.powered;

        toast.add({
            severity: 'success',
            summary: 'Appareil mis à jour',
            detail: `${device.name} est maintenant ${device.powered ? 'allumé' : 'éteint'}`,
            life: 3000
        });
    } catch (error) {
        toast.add({
            severity: 'error',
            summary: 'Erreur',
            detail: "Impossible de modifier l'état de l'appareil",
            life: 3000
        });
        console.error('Error toggling device:', error);
    }
};

// Get device category icon
const getDeviceIcon = (category) => {
    const icons = {
        LIGHTING: 'pi pi-lightbulb',
        HEATING: 'pi pi-thermometer',
        COOLING: 'pi pi-sun',
        MULTIMEDIA: 'pi pi-desktop',
        SECURITY: 'pi pi-shield',
        HOUSEHOLD: 'pi pi-home',
        MONITORING: 'pi pi-chart-bar',
        OTHER: 'pi pi-cog'
    };
    return icons[category] || 'pi pi-cog';
};

// Get device category color
const getDeviceColor = (category) => {
    const colors = {
        LIGHTING: 'var(--yellow-500)',
        HEATING: 'var(--orange-500)',
        COOLING: 'var(--blue-500)',
        MULTIMEDIA: 'var(--purple-500)',
        SECURITY: 'var(--red-500)',
        HOUSEHOLD: 'var(--green-500)',
        MONITORING: 'var(--cyan-500)',
        OTHER: 'var(--gray-500)'
    };
    return colors[category] || 'var(--gray-500)';
};

// Load data
const loadDevices = async () => {
    try {
        loading.value = true;
        const response = await api.devices.getDevices({ userId: props.userId });
        devices.value = response.devices || [];
    } catch (error) {
        console.error('Error loading devices:', error);
        toast.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Impossible de charger les appareils',
            life: 3000
        });
    } finally {
        loading.value = false;
    }
};

// Initial load
await loadDevices();
</script>

<template>
    <div class="devices-container">
        <ProgressSpinner v-if="loading" />

        <div v-else-if="devices.length === 0" class="empty-state">
            <i class="pi pi-mobile text-5xl mb-3 text-gray-400"></i>
            <p class="text-lg text-gray-600">Aucun appareil trouvé</p>
        </div>

        <div v-else class="grid">
            <DeviceCard v-for="d, k in devices" :key="d.id" :device="d" v-model="devices[k]" />
        </div>
    </div>
</template>

<style scoped>
.devices-container {
    min-height: 200px;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 3rem;
    text-align: center;
}
</style>
