<script lang="ts" setup>
import api, { CompleteDevice } from '@/api';
import { useGuards } from '@/guards';
import { deviceCategoryLabels } from '@/labels';
import { useToast } from 'primevue';
import { useRouter } from 'vue-router';

const device = defineModel<CompleteDevice>();

const router = useRouter();
const guards = useGuards();
const toast = useToast();

async function togglePower(event: Event) {
  event.stopPropagation(); // Prevent navigation when toggling power

  if (!guards.mustManage()) {
    return;
  }

  try {
    // Update the device via API
    await api.devices.patchDevice({
      deviceId: device.value.id,
      devicePatchInput: {
        powered: !device.value.powered
      }
    });

    // Update the local device data with the new state
    device.value.powered = !device.value.powered
  } catch (error) {
    console.error('Failed to toggle device power:', error);
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de changer l\'état de l\'appareil.',
      life: 3000
    });
  }
}

function viewDeviceDetails(device: CompleteDevice) {
  router.push({
    name: 'device-detail',
    params: { deviceId: device.id }
  });
}
</script>

<template>
  <div class="device-card" :class="{ powered: device.powered }" @click="viewDeviceDetails(device)">
    <div class="card-header">
      <div class="device-name">
        <h3>{{ device.name }}</h3>
        <div class="device-type">{{ device.type.name }}</div>
      </div>
      <div class="power-status" @click="togglePower($event)">
        <span class="status-indicator"></span>
        <span class="status-text">{{ device.powered ? 'ON' : 'OFF' }}</span>
      </div>
    </div>

    <div class="card-content">
      <p class="device-description">{{ device.description }}</p>

      <div class="device-info">
        <Tag icon="pi pi-lightbulb" rounded severity="warn">{{ device.energyConsumption.toFixed(2) + ' Wh' }}</Tag>
        <Tag icon="pi pi-home" rounded
          :style="{ '--p-tag-primary-background': '#' + device.room!.color.toString(16).padStart(6, '0'), '--p-tag-primary-color': '#fff' }"
          v-if="device.room">{{ device.room.name }}</Tag>
          <Chip :label="deviceCategoryLabels[device.type.category]" />
        <Chip v-if="device.owner">
          <i class="pi pi-user"></i>
          <RouterLink :to="{ name: 'profile', params: { userId: device.owner!.id } }"
            style="text-decoration: none; color: inherit;" @click.stop>{{ device.owner!.firstName + ' ' +
              device.owner.lastName }}</RouterLink>
        </Chip>
      </div>

      <div class="del-request" v-if="device.deletionRequestedBy != null">
        <i class="pi pi-trash me-2"></i>Suppression demandée par 
        {{ device.deletionRequestedBy.firstName + ' ' + device.deletionRequestedBy.lastName }}
      </div>
    </div>
  </div>
</template>

<style lang="css" scoped>
.device-card {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  transition: all 0.3s ease;
  border-left: 4px solid #ccc;
  cursor: pointer;

  --p-chip-padding-y: 0.25rem;

  display: flex;
  flex-direction: column;

  &:has(.del-request) {
    padding-bottom: 0;
  }
}

.device-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.device-card.powered {
  border-left-color: rgb(16, 110, 101);
}

.del-request {
  background-color: rgb(190, 0, 0);
  color: white;
  border-radius: 8px 8px 0 0;
  padding: 0.5rem;

  margin-top: 1rem;
  text-align: center;
}

.card-content {
  flex: 1 0 auto;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.device-name h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.device-type {
  font-size: 0.8rem;
  opacity: 0.75;
}

.power-status {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0.5rem 0.75rem;
  border-radius: 16px;
  background: rgba(0, 0, 0, 0.05);
  transition: background-color 0.2s;
}

.power-status:hover {
  background: rgba(0, 0, 0, 0.1);
}

.status-indicator {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: #ccc;
  margin-right: 8px;
}

.powered .status-indicator {
  background-color: rgb(16, 110, 101);
  box-shadow: 0 0 8px rgba(16, 110, 101, 0.6);
}

.device-description {
  color: #666;
  margin-bottom: 1rem;
  font-size: 0.9rem;

  flex: 1 0 auto;
}

.device-info {
  display: flex;
  flex-direction: row;
  gap: 0.75rem;
  margin-top: 1rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  font-size: 0.9rem;
}

.label {
  color: #666;
  font-size: 0.8rem;
  margin-bottom: 0.25rem;
}

.value {
  font-weight: 500;
  color: #333;
}
</style>