<script lang="ts" setup>
import { ref, computed, onMounted, toRaw } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ToggleSwitch, useToast } from 'primevue';
import api, { CompleteDevice, DevicePatchInput, findErrData, isOk } from '@/api';
import { attributeTypeLabels, deviceCategoryLabels, formatAttribute } from '@/labels';
import DeviceForm from '@/components/DeviceForm.vue';
import FullscreenSpinner from '@/components/FullscreenSpinner.vue';
import { useGuards } from '@/guards';

const guards = useGuards();
const route = useRoute();
const router = useRouter();
const toast = useToast();

// Get deviceId from route parameters
const deviceId = computed(() => parseInt(route.params.deviceId as string));

// State variables
const device = ref<CompleteDevice | null>(null);
const isEditing = ref(false);
const error = ref<any>(null);

// Edit form model
const editForm = ref<any>(null);

// Fetch device data
async function fetchDevice() {
  error.value = null;

  try {
    const response = await api.devices.getDeviceById({ deviceId: deviceId.value });
    device.value = response;
  } catch (err) {
    console.error('Failed to fetch device:', err);
    error.value = err;
  }
}

// Initialize or reset edit form with current device values
function resetEditForm() {
  if (!device.value) return;

  editForm.value = {
    name: device.value.name,
    description: device.value.description || '',
    powered: device.value.powered,
    energyConsumption: device.value.energyConsumption,
    typeId: device.value.type.id,
    attributes: structuredClone(toRaw(device.value.attributes)), // Clone the map so edits don't affect our device
    roomId: device.value.room?.id,
    userId: device.value.owner?.id
  };
}

// Toggle power status
async function togglePower() {
  if (!device.value || !guards.mustManage()) return;

  try {
    const result = await api.devices.patchDevice({
      deviceId: deviceId.value,
      devicePatchInput: {
        powered: !device.value.powered
      }
    });

    device.value.powered = !device.value.powered;
    toast.add({
      severity: 'success',
      summary: 'État changé',
      detail: `Appareil ${device.value.powered ? 'allumé' : 'éteint'} avec succès`,
      life: 3000
    });
  } catch (err) {
    console.error('Failed to toggle power:', err);
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de changer l\'état de l\'appareil',
      life: 3000
    });
  }
}

// Start editing mode
function startEdit() {
  if (!guards.mustManage()) {
    return;
  }

  resetEditForm();
  isEditing.value = true;
}

// Cancel editing and reset form
function cancelEdit() {
  isEditing.value = false;
}

// Handle successful save from DeviceForm component
function handleSaveSuccess(result: CompleteDevice) {
  device.value = result;
  isEditing.value = false;
}

// Return to devices list
function backToList() {
  router.push({ name: 'devices' });
}

// Initialize component
await fetchDevice();
</script>

<template>
  <div class="device-detail container">
    <!-- Error state -->
    <div v-if="error" class="error-container">
      <Message severity="error">Une erreur est survenue lors du chargement de l'appareil.</Message>
      <Button label="Retour à la liste" icon="pi pi-arrow-left" @click="backToList" class="mt-3" />
    </div>

    <!-- Device not found -->
    <div v-else-if="!device" class="not-found-container">
      <Message severity="info">Appareil introuvable</Message>
      <Button label="Retour à la liste" icon="pi pi-arrow-left" @click="backToList" class="mt-3" />
    </div>

    <!-- Device detail view -->
    <div v-else class="device-content">
      <!-- Top actions bar -->
      <div class="action-bar">
        <Button icon="pi pi-arrow-left" label="Retour" @click="backToList" />
        <div class="action-buttons" v-if="!isEditing">
          <Button icon="pi pi-power-off" :class="device.powered ? 'p-button-success' : 'p-button-secondary'"
            @click="togglePower" />
          <Button icon="pi pi-pencil" label="Modifier" @click="startEdit" />
        </div>
      </div>

      <!-- View mode -->
      <div v-if="!isEditing">
        <h1 class="device-title">{{ device.name }}</h1>
        <div class="device-view">
          <Card class="device-header">
            <template #title>Modèle</template>
            <template #content>
              <i class="pi pi-box device-image"></i>
              <div class="device-type-name">{{ device.type.name }}</div>
              <div class="device-type-cat">{{ deviceCategoryLabels[device.type.category] }}</div>
            </template>
          </Card>

          <Card>
            <template #title>Détails</template>
            <template #content>
              <div class="device-info">
                <h3 class="device-info-st">Description</h3>
                <p :class="{ 'device-desc-val': true, '-empty': !device.description }">
                  {{ device.description || 'Aucune description' }}
                </p>

                <h3 class="device-info-st">État</h3>
                <div class="info-row">
                  <div class="info-label">Consommation</div>
                  <div class="info-value">{{ device.energyConsumption.toFixed(2) }} Wh</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Salle</div>
                  <div class="info-value" v-if="device.room">
                    {{ device.room.name }}
                  </div>
                  <div class="info-value opacity-75" v-else>Aucune salle</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Propriétaire</div>
                  <div class="info-value" v-if="device.owner">
                    {{ device.owner.firstName }} {{ device.owner.lastName }}
                  </div>
                  <div class="info-value opacity-75" v-else>Aucun propriétaire</div>
                </div>
              </div>
            </template>
          </Card>

          <Card>
            <template #title>Données</template>
            <template #content>
              <div class="attributes-container" v-if="Object.keys(device.attributes).length > 0">
                <div class="attribute-row" v-for="(value, key) in device.attributes" :key="key">
                  <div class="attribute-key">{{ attributeTypeLabels[key] }}</div>
                  <div class="attribute-value" v-if="value != null">{{ formatAttribute(key as any, value) }}</div>
                  <div class="attribute-value text-secondary" v-else>
                    <i class="pi pi-lock"></i> Privé
                  </div>
                </div>
              </div>
              <div v-else class="no-attributes">
                Aucun attribut disponible
              </div>
            </template>
          </Card>
        </div>

        <div>
          <h2>Historique d'activité</h2>
          <p>Bientôt j'espère</p>
        </div>

        <div :class="{ 'powered-overlay': true, '-on': device.powered }"></div>
      </div>

      <!-- Edit mode - using the DeviceForm component TODO: put it in a route -->
      <div v-else class="device-edit">
        <Suspense>
          <DeviceForm v-model="editForm" :device-id="deviceId" @save-success="handleSaveSuccess" @cancel="cancelEdit" />

          <template #fallback>
            <FullscreenSpinner />
          </template>
        </Suspense>
      </div>
    </div>
  </div>
</template>

<style lang="css" scoped>
.device-detail {
  padding: 1rem;
}

.device-title {
  padding: 1rem 0;
  margin: 1rem 0;
  text-align: center;
  border-bottom: 1px solid rgba(4, 91, 172, 0.1);
}

.loading-container,
.error-container,
.not-found-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  text-align: center;
}

.device-view {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;

  margin-bottom: 1rem;

  &>* {
    flex: 1 0 0;
    min-width: min(50vw, 400px);
  }
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.device-image {
  text-align: center;
  display: block;
}

.device-image::before {
  font-size: 9rem;
}

.device-type-name {
  font-size: 1.125rem;
  font-weight: bold;
  text-align: center;
  margin-top: 0.5rem;
}

.device-type-cat {
  text-align: center;
}

.device-desc-val.-empty {
  opacity: 0.8;
  margin: 0;
  margin-bottom: 0.5rem;
}

.device-title {
  display: flex;
  flex-direction: column;
}

.device-title h2 {
  margin: 0;
  font-size: 1.8rem;
}

.device-info-st {
  font-size: 1.125rem;
  margin: 0.5rem 0;
}

.device-subtitle {
  font-size: 1.1rem;
  color: #666;
}

.device-info {
  display: flex;
  flex-direction: column;
}

.info-row {
  display: flex;
  padding: 0.5rem 0;
  border-bottom: 1px solid #eee;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  flex: 0 0 60%;
  font-weight: 500;
}

.info-value {
  flex: 0 0 40%;
}

.attributes-container {
  display: flex;
  flex-direction: column;
}

.attribute-row {
  display: flex;
  padding: 0.5rem 0;
  border-bottom: 1px solid #eee;
}

.attribute-row:last-child {
  border-bottom: none;
}

.attribute-key {
  flex: 0 0 60%;
  font-weight: 500;
}

.attribute-value {
  flex: 0 0 40%;
  word-break: break-word;
}

.no-attributes {
  color: #666;
  font-style: italic;
}

/* Make a custom property so we can animate it using transition */
@property --on-pos {
  syntax: "<percentage>";
  initial-value: 0%;
  inherits: false;
}

.powered-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: -1;
  transition: --on-pos 0.5s ease;

  --on-pos: 0%;
  background: radial-gradient(circle at 50% 115%,
      rgba(227, 255, 165, 60%) 0%,
      rgba(255, 255, 255, 0) var(--on-pos));

  &.-on {
    --on-pos: 60%;
  }
}
</style>