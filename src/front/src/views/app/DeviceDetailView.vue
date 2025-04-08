<script lang="ts" setup>
import { ref, computed, onMounted, toRaw } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ToggleSwitch, useToast } from 'primevue';
import api, { CompleteDevice, DevicePatchInput, findErrData, isOk } from '@/api';
import { attributeTypeLabels, deviceCategoryLabels, formatAttribute } from '@/labels';
import DeviceForm from '@/components/DeviceForm.vue';
import FullscreenSpinner from '@/components/FullscreenSpinner.vue';
import { useGuards } from '@/guards';
import { useAuthStore } from '@/stores/auth';
import DeviceLogs from '@/components/DeviceLogs.vue';

const auth = useAuthStore();
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
const deleteModalVisible = ref(false);
const deleteModalPromise = ref(null);
const logKey = ref(0);

// Direct: can delete directly
// RequestedByOther: deletion requested by other user
// RequestedByMe: deletion requested by me
// NotRequested: deletion not requested
type DelState = "DIRECT" | "REQUESTED_BY_OTHER" | "REQUESTED_BY_ME" | "NOT_REQUESTED";

const deletionState = computed<DelState>(() => {
  if (auth.canAdminister) {
    return "DIRECT";
  } else if (device.value.deletionRequestedBy == null) {
    return "NOT_REQUESTED"
  } else {
    return device.value.deletionRequestedBy.id === auth.userId ? "REQUESTED_BY_ME" : "REQUESTED_BY_OTHER";
  }
});

const emit = defineEmits<{
  'device-deleted': [number]
  'device-edited': [CompleteDevice]
}>();

// Edit form model
const editForm = ref<any>(null);

// Logs settings
const logsSettings = ref({
  offPeriods: false
});

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
    logKey.value += 1; // Force re-render of logs component
    emit('device-edited', result);
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

async function onDeleteClick() {
  const d = device.value;
  if (d == null) return;

  switch (deletionState.value) {
    case "DIRECT":
      deleteModalVisible.value = true;
      break;
    case "REQUESTED_BY_ME":
      await removeDeletionRequest();
      break;
    case "REQUESTED_BY_OTHER":
      break;
    case "NOT_REQUESTED":
      await api.devices.patchDevice({
        deviceId: deviceId.value,
        devicePatchInput: {
          deletionRequestedById: auth.userId
        }
      });
      d.deletionRequestedBy = auth.user.profile;
      emit('device-edited', d);

      toast.add({
        severity: 'success',
        summary: 'Demande de suppression envoyée',
        detail: 'La demande de suppression a été envoyée. Un administrateur la traitera bientôt.',
        life: 5000
      });
      break;
  }
}

async function removeDeletionRequest() {
  const d = device.value;
  if (d == null) return;

  await api.devices.patchDevice({
    deviceId: deviceId.value,
    devicePatchInput: {
      deletionRequestedById: null
    }
  });
  d.deletionRequestedBy = null;

  emit('device-edited', d);

  toast.add({
    severity: 'success',
    summary: 'Suppression annulée',
    detail: 'La demande de suppression a bien été annulée.',
    life: 5000
  });
}

function deleteDevice() {
  if (deleteModalPromise.value) return;

  async function inner() {
    try {
      await api.devices.deleteDevice({ deviceId: deviceId.value });
      toast.add({
        severity: 'success',
        summary: 'Appareil supprimé',
        detail: 'L\'appareil a été supprimé avec succès',
        life: 3000
      });
      emit('device-deleted', deviceId.value);
      router.back();
    } catch (e) {
      const data = findErrData(e);
      const errMsg = data?.message || 'Une erreur est survenue lors de la suppression de l\'appareil';
      toast.add({
        severity: 'error',
        summary: 'Erreur lors de la suppression',
        detail: errMsg,
        life: 5000
      })
    } finally {
      deleteModalVisible.value = false;
    }
  }

  deleteModalPromise.value = inner();
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

  logKey.value += 1; // Force re-render of logs component

  emit('device-edited', result);
}

// Return to devices list
function backToList() {
  router.back();
}

// Initialize component
await fetchDevice();
await api.userEvents.reportDeviceCheck();
</script>

<template>
  <div class="device-detail">
    <div class="container p-3">
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
          <Button icon="pi pi-arrow-left" class="back-button" label="Retour" @click="backToList" />
          <div class="action-buttons" v-if="!isEditing">
            <Button icon="pi pi-power-off" class="power-button"
              :class="device.powered ? 'p-button-success' : 'p-button-secondary'" @click="togglePower" />
            <Button :icon="deletionState == 'DIRECT' || deletionState == 'NOT_REQUESTED' ? 'pi pi-trash' :
              deletionState == 'REQUESTED_BY_ME' ? 'pi pi-times' : 'pi pi-user-check'" :label="({
              DIRECT: 'Supprimer',
              REQUESTED_BY_ME: 'Annuler la demande',
              REQUESTED_BY_OTHER: 'Demande envoyée',
              NOT_REQUESTED: 'Demander la suppression'
            } as Record<DelState, string>)[deletionState]" :disabled="deletionState === 'REQUESTED_BY_OTHER'"
              severity="danger" v-if="auth.canManage" @click="onDeleteClick()" />
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


          <div v-if="device.deletionRequestedBy != null" severity="error" class="del-request">
            <p> Demande de suppression envoyée par <RouterLink
                :to="{ name: 'profile', params: { userId: device.deletionRequestedBy.id } }">
                {{ device.deletionRequestedBy.firstName }} {{ device.deletionRequestedBy.lastName }}</RouterLink>
            </p>

            <button class="del-ignore" v-if="auth.canAdminister" @click="removeDeletionRequest">
              <div class="pi pi-times me-1"></div>
              <div>Ignorer</div>
            </button>
          </div>

          <div v-if="auth.canManage">
            <Suspense>
              <DeviceLogs :device-id="deviceId" :key="logKey" v-model="logsSettings" />

              <template #fallback>
                <FullscreenSpinner />
              </template>
            </Suspense>
          </div>
        </div>

        <!-- Edit mode - using the DeviceForm component TODO: put it in a route -->
        <div v-else class="device-edit">
          <Suspense>
            <DeviceForm v-model="editForm" :device-id="deviceId" @save-success="handleSaveSuccess"
              @cancel="cancelEdit" />

            <template #fallback>
              <FullscreenSpinner />
            </template>
          </Suspense>
        </div>
      </div>

      <!-- Delete Modal -->
      <Dialog modal v-model:visible="deleteModalVisible" header="Supprimer l'appareil" style="max-width: 450px;">
        <p>Êtes-vous sûr de vouloir supprimer cet appareil ?</p>
        <Button severity="danger" icon="pi pi-trash" label="Supprimer" fluid @click="deleteDevice" />
      </Dialog>

    </div>
    
    <!-- Cool overlay -->
    <div :class="{ 'powered-overlay': true, '-on': device.powered }"></div>
  </div>
</template>

<style lang="css" scoped>
.device-detail {
  width: 100%;
  min-height: 100%;

  position: relative;
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

.del-request {
  margin: 1.0rem 0;
  text-align: center;
  background-color: rgb(190, 0, 0);
  border: 1px solid rgb(190, 0, 0);
  color: white;

  overflow: hidden;

  border-radius: 16px;

  & a {
    color: rgb(255, 220, 208);
    text-decoration: none;
  }

  & p {
    padding: 1.25rem;
    font-size: 1.5em;
    margin: 0;
  }
}

.del-ignore {
  width: 100%;
  border: 0;
  border-top: 1px solid white;
  background-color: rgb(191, 55, 55);
  color: white;

  padding: 0.5rem;
  font-size: 1.25em;
  /* border-radius: 0 0 16px 16px; */

  display: flex;
  align-items: baseline;
  justify-content: center;

  transition: all 0.2s ease;

  &:hover {
    /* border: 1px solid rgb(190, 0, 0); */
    background-color: white;
    color: black;
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

.power-button {
  min-width: 128px;
}

@media (max-width: 768px) {
  .action-bar {
    flex-direction: column;
    gap: 0.5rem;

    &>* {
      width: 100%;
    }
  }

  .action-buttons {
    flex-wrap: wrap;

    &>* {
      flex: 1 0 auto;
    }
  }

  .power-button {
    flex: 1;
    min-width: 64px;
  }
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