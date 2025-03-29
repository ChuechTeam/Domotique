<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ToggleSwitch, useToast } from 'primevue';
import api, { CompleteDevice, DevicePatchInput, isOk } from '@/api';
import ValidationErrList from '@/components/ValidationErrList.vue';

const route = useRoute();
const router = useRouter();
const toast = useToast();

// Get deviceId from route parameters
const deviceId = computed(() => parseInt(route.params.deviceId as string));

// State variables
const device = ref<CompleteDevice | null>(null);
const isLoading = ref(true);
const isEditing = ref(false);
const isSaving = ref(false);
const error = ref<any>(null);
const validationErrors = ref<any>(null);

// Edit form model
const editForm = ref<DevicePatchInput | null>(null);

// Available rooms and device types for selection
const rooms = ref<any[]>([]);
const deviceTypes = ref<any[]>([]);

// Fetch device data
async function fetchDevice() {
  isLoading.value = true;
  error.value = null;
  
  try {
    const response = await api.devices.getDeviceById({ deviceId: deviceId.value });
    device.value = response;
    // Initialize edit form with current values
    resetEditForm();
  } catch (err) {
    console.error('Failed to fetch device:', err);
    error.value = err;
  } finally {
    isLoading.value = false;
  }
}

// Fetch available rooms and device types for dropdown options
async function fetchOptions() {
  try {
    const [roomsResponse, typesResponse] = await Promise.all([
      api.rooms.getRooms({}),
      api.deviceTypes.getDeviceTypes({})
    ]);
    
    rooms.value = roomsResponse.rooms || [];
    deviceTypes.value = typesResponse.deviceTypes || [];
  } catch (err) {
    console.error('Failed to fetch options:', err);
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
    attributes: device.value.attributes,
    roomId: device.value.room?.id,
    userId: device.value.owner?.id
  };
}

// Toggle power status
async function togglePower() {
  if (!device.value) return;
  
  try {
    const patchData: DevicePatchInput = {
      ...editForm.value!,
      powered: !device.value.powered
    };
    
    const result = await api.devices.patchDevice({
      deviceId: deviceId.value,
      devicePatchInput: patchData
    });
    
    if (isOk(result)) {
      device.value.powered = !device.value.powered;
      resetEditForm();
      toast.add({
        severity: 'success',
        summary: 'État changé',
        detail: `Appareil ${device.value.powered ? 'allumé' : 'éteint'} avec succès`,
        life: 3000
      });
    }
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
  resetEditForm();
  isEditing.value = true;
}

// Cancel editing and reset form
function cancelEdit() {
  isEditing.value = false;
  resetEditForm();
  validationErrors.value = null;
}

// Save device changes
async function saveDevice() {
  if (!editForm.value) return;
  
  isSaving.value = true;
  validationErrors.value = null;
  
  try {
    const result = await api.devices.patchDevice({
      deviceId: deviceId.value,
      devicePatchInput: editForm.value
    });
    
    if (isOk(result)) {
      // Update local device data with response
      device.value = result;
      isEditing.value = false;
      toast.add({
        severity: 'success',
        summary: 'Appareil modifié',
        detail: 'Les modifications ont été enregistrées avec succès',
        life: 3000
      });
    } else {
      // Handle validation errors
      validationErrors.value = result;
    }
  } catch (err) {
    console.error('Failed to save device:', err);
    validationErrors.value = {
      message: 'Une erreur est survenue lors de la sauvegarde',
      data: {}
    };
  } finally {
    isSaving.value = false;
  }
}

// Return to devices list
function backToList() {
  router.push({ name: 'devices' });
}

// Initialize component
onMounted(async () => {
  await Promise.all([fetchDevice(), fetchOptions()]);
});
</script>

<template>
  <div class="device-detail">
    <!-- Loading state -->
    <div v-if="isLoading" class="loading-container">
      <ProgressSpinner />
      <p>Chargement de l'appareil...</p>
    </div>
    
    <!-- Error state -->
    <div v-else-if="error" class="error-container">
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
      <div v-if="!isEditing" class="device-view">
        <div class="device-header">
          <div class="device-title">
            <h2>{{ device.name }}</h2>
            <div class="device-subtitle">{{ device.type.name }}</div>
          </div>
          <Tag :severity="device.powered ? 'success' : 'secondary'" rounded>
            {{ device.powered ? 'Allumé' : 'Éteint' }}
          </Tag>
        </div>
        
        <Card class="mb-3">
          <template #title>Informations générales</template>
          <template #content>
            <div class="device-info">
              <div class="info-row">
                <div class="info-label">Description</div>
                <div class="info-value">{{ device.description || 'Aucune description' }}</div>
              </div>
              <div class="info-row">
                <div class="info-label">Consommation d'énergie</div>
                <div class="info-value">{{ device.energyConsumption.toFixed(2) }} Wh</div>
              </div>
              <div class="info-row" v-if="device.room">
                <div class="info-label">Pièce</div>
                <div class="info-value">{{ device.room.name }}</div>
              </div>
              <div class="info-row" v-if="device.owner">
                <div class="info-label">Propriétaire</div>
                <div class="info-value">{{ device.owner.firstName }} {{ device.owner.lastName }}</div>
              </div>
            </div>
          </template>
        </Card>
        
        <Card>
          <template #title>Attributs</template>
          <template #content>
            <div class="attributes-container" v-if="Object.keys(device.attributes).length > 0">
              <div class="attribute-row" v-for="(value, key) in device.attributes" :key="key">
                <div class="attribute-key">{{ key }}</div>
                <div class="attribute-value">{{ JSON.stringify(value) }}</div>
              </div>
            </div>
            <div v-else class="no-attributes">
              Aucun attribut disponible
            </div>
          </template>
        </Card>
      </div>
      
      <!-- Edit mode -->
      <div v-else class="device-edit">
        <Card>
          <template #title>Modifier l'appareil</template>
          <template #content>
            <!-- Validation error message -->
            <div v-if="validationErrors?.message" class="mb-3">
              <Message severity="error">{{ validationErrors.message }}</Message>
            </div>
            
            <form @submit.prevent="saveDevice">
              <div class="form-section">
                <div class="form-group full-width">
                  <FloatLabel variant="in">
                    <InputText v-model="editForm.name" id="name" 
                      :invalid="validationErrors?.data?.name?.length > 0" />
                    <label for="name">Nom</label>
                    <ValidationErrList :errors="validationErrors?.data?.name" />
                  </FloatLabel>
                </div>
                
                <div class="form-group full-width">
                  <FloatLabel variant="in">
                    <Textarea v-model="editForm.description" id="description"
                      :invalid="validationErrors?.data?.description?.length > 0" />
                    <label for="description">Description</label>
                    <ValidationErrList :errors="validationErrors?.data?.description" />
                  </FloatLabel>
                </div>
                
                <div class="form-row">
                  <div class="form-group">
                    <FloatLabel variant="in">
                      <Select v-model="editForm.typeId" :options="deviceTypes" 
                        optionLabel="name" optionValue="id" 
                        :invalid="validationErrors?.data?.typeId?.length > 0" />
                      <label>Type d'appareil</label>
                      <ValidationErrList :errors="validationErrors?.data?.typeId" />
                    </FloatLabel>
                  </div>
                  
                  <div class="form-group">
                    <FloatLabel variant="in">
                      <Select v-model="editForm.roomId" :options="rooms" 
                        optionLabel="name" optionValue="id" 
                        :invalid="validationErrors?.data?.roomId?.length > 0" />
                      <label>Pièce</label>
                      <ValidationErrList :errors="validationErrors?.data?.roomId" />
                    </FloatLabel>
                  </div>
                </div>
                
                <div class="form-row">
                  <div class="form-group">
                    <FloatLabel variant="in">
                      <InputNumber v-model="editForm.energyConsumption" id="energy" 
                        :invalid="validationErrors?.data?.energyConsumption?.length > 0" />
                      <label for="energy">Consommation d'énergie (Wh)</label>
                      <ValidationErrList :errors="validationErrors?.data?.energyConsumption" />
                    </FloatLabel>
                  </div>
                  
                  <div class="form-group switch-group">
                    <div class="switch-label">État</div>
                    <ToggleSwitch v-model="editForm.powered" />
                    <div class="switch-value">{{ editForm.powered ? 'Allumé' : 'Éteint' }}</div>
                  </div>
                </div>
                
                <!-- Note: Attribute editing is not implemented in this initial version -->
              </div>
              
              <div class="form-actions">
                <Button type="button" label="Annuler" class="p-button-outlined" @click="cancelEdit" 
                  :disabled="isSaving" />
                <Button type="submit" label="Enregistrer" icon="pi pi-check" :loading="isSaving" />
              </div>
            </form>
          </template>
        </Card>
      </div>
    </div>
  </div>
</template>

<style lang="css" scoped>
.device-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem;
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

.device-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
}

.device-title {
  display: flex;
  flex-direction: column;
}

.device-title h2 {
  margin: 0;
  font-size: 1.8rem;
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
  flex: 0 0 40%;
  font-weight: 500;
}

.info-value {
  flex: 0 0 60%;
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
  flex: 0 0 40%;
  font-weight: 500;
}

.attribute-value {
  flex: 0 0 60%;
  word-break: break-word;
}

.no-attributes {
  color: #666;
  font-style: italic;
}

/* Form styling */
.form-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.form-row {
  display: flex;
  gap: 1rem;
}

.form-group {
  flex: 1;
}

.full-width {
  width: 100%;
}

.switch-group {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding-top: 1rem;
}

.switch-label {
  min-width: 50px;
}

.switch-value {
  min-width: 60px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding-top: 1rem;
}
</style>