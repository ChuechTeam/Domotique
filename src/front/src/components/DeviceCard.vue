<script lang="ts" setup>
import { CompleteDevice } from '@/api';
import { useRouter } from 'vue-router';

defineProps<{
  device: CompleteDevice;
}>();

const router = useRouter();

const emits = defineEmits<{
  (e: 'toggle-power', id: number): void;
}>();

function togglePower(device: CompleteDevice, event: Event) {
  event.stopPropagation(); // Prevent navigation when toggling power
  emits('toggle-power', device.id);
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
        <div class="device-type">{{ device.type.name  }}</div>
      </div>
      <div class="power-status" @click="togglePower(device, $event)">
        <span class="status-indicator"></span>
        <span class="status-text">{{ device.powered ? 'On' : 'Off' }}</span>
      </div>
    </div>
    
    <div class="card-content">
      <p v-if="device.description" class="device-description">{{ device.description }}</p>
      
      <div class="device-info">
        <Tag icon="pi pi-lightbulb" rounded severity="warn">{{ device.energyConsumption.toFixed(2) + ' Wh' }}</Tag>
        <Tag icon="pi pi-home" rounded severity="info" v-if="device.room">{{ device.room.name }}</Tag>
        <Chip v-if="device.owner" :label="device.owner!.firstName + ' ' + device.owner.lastName" />
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
}

.device-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.device-card.powered {
  border-left-color: rgb(16, 110, 101);
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