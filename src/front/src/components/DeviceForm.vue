<script lang="ts">
// This function creates the default structure for a device form
// Using a factory function ensures each form instance gets a fresh object
export const defaultFormModel = () => ({
    energyConsumption: 0 as number,
    powered: false as boolean,
    name: "" as string,
    description: null as string | null,
    typeId: null as number | null, // shouldn't be null
    attributes: {} as Record<string, any>, // Dynamic key-value pairs for device attributes
    userId: null as number | null,
    roomId: null as number | null,
});

// TypeScript type derived from the return type of the factory function
export type DeviceFormModel = ReturnType<typeof defaultFormModel>;
</script>

<script lang="ts" setup>
// setup script - Vue 3's Composition API with TypeScript
import { ref, useTemplateRef, watch } from 'vue'; // ref creates reactive variables, watch observes changes
import { useToast, InputText, Select } from 'primevue'; // PrimeVue UI component library
import api, { CompleteDeviceType, CompleteRoom, findErrData, UserProfile, ValidationErrorResponse } from '@/api';
import ValidationErrList from '@/components/ValidationErrList.vue';
import { attributeTypeLabels, attributeTypeContents, attributeTypeFormats } from '@/labels';
import DeviceTypeForm, { defaultDeviceTypeFormModel } from './DeviceTypeForm.vue';
import RoomForm, { defaultRoomFormModel } from './RoomForm.vue';

// Props from parent component - deviceId is optional (used for edit mode)
const props = defineProps<{ deviceId?: number }>();
const isNew = props.deviceId == null; // Check if we're creating a new device or editing existing one

const toast = useToast(); // Toast notifications service for user feedback

// defineModel() is a Vue 3 macro for two-way binding with parent component
const model = defineModel<DeviceFormModel>();

// Reactive state variables using ref()
const isSaving = ref(false); // Tracks if save operation is in progress
const validationErrors = ref<ValidationErrorResponse<DeviceFormModel> | null>(null); // Form validation errors
const editFormUsers = ref<UserProfile[]>([]); // Users displayed in the Select dropdown
const rooms = ref<any[]>([]); // Available rooms for selection
const deviceTypes = ref<CompleteDeviceType[]>([]); // Available device types for selection
const userInput = ref<any>(null); // Reference to user search input for focus management
const typeSelect = useTemplateRef<InstanceType<typeof Select>>("typeSelect"); // Reference to device type select dropdown
const roomSelect = useTemplateRef<InstanceType<typeof Select>>("roomSelect"); // Reference to room select dropdown
const selectedDeviceType = ref<CompleteDeviceType | null>(null); // Currently selected device type
const newDeviceTypeDialogVisible = ref(false); // Controls visibility of device type creation dialog
const newDeviceTypeDialogData = ref(null); // Data for new device type
const newRoomDialogVisible = ref(false); // Controls visibility of room creation dialog
const newRoomDialogData = ref(null); // Data for new room

// Events this component can emit to parent components
const emit = defineEmits(['update:modelValue', 'save-success', 'cancel']);

// Fetch available rooms and device types for dropdown options
async function fetchOptions() {
    try {
        // Promise.all runs both API calls in parallel for better performance
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

// Search users by name for the dropdown
async function updateUsersSelect(val: string | undefined) {
    if (val) {
        // Searches users based on input text
        editFormUsers.value = (await api.users.searchUsers({
            fullName: val
        })).profiles;
    }
}

// Shows dialog to create a new device type
function showDeviceTypeDialog() {
    newDeviceTypeDialogData.value = defaultDeviceTypeFormModel();
    newDeviceTypeDialogVisible.value = true;
    typeSelect.value.hide(); // Hide the <Select /> cause it's weird to have it open in the background
}

// Shows dialog to create a new room
function showRoomDialog() {
    newRoomDialogData.value = defaultRoomFormModel();
    newRoomDialogVisible.value = true;
    roomSelect.value.hide(); // Hide the <Select /> cause it's weird to have it open in the background
}

// Callback when a new device type is created
function useCreatedDeviceType(dt: CompleteDeviceType) {
    // Add the new device type to the list and select it
    deviceTypes.value.push(dt);
    model.value.typeId = dt.id;
    newDeviceTypeDialogVisible.value = false;
}

// Callback when a new room is created
function useCreatedRoom(r: CompleteRoom) {
    // Add the new room to the list and select it
    rooms.value.push(r);
    model.value.roomId = r.id;
    newRoomDialogVisible.value = false;
}

// Save device changes - called on form submit
async function saveDevice() {
    if (!model.value) return;

    // Validate required fields before saving
    if (model.value.typeId == null) {
        validationErrors.value = {
            message: "Le modèle est requis",
            data: {
                typeId: ["Le modèle est requis"]
            },
            code: "VALIDATION_ERROR"
        }
        return;
    }

    isSaving.value = true;
    validationErrors.value = null;

    try {
        if (isNew) {
            // Create new device via API
            const result = await api.devices.createDevice({
                deviceInput: model.value
            });
            toast.add({
                severity: 'success',
                summary: 'Appareil créé',
                detail: 'L\'appareil a été créé avec succès',
                life: 3000
            });
            emit('save-success', result);
        }
        else {
            // Update existing device via API
            const result = await api.devices.patchDevice({
                deviceId: props.deviceId,
                devicePatchInput: model.value
            });
            toast.add({
                severity: 'success',
                summary: 'Appareil modifié',
                detail: 'Les modifications ont été enregistrées avec succès',
                life: 3000
            });
            emit('save-success', result);
        }
    } catch (err) {
        console.error('Failed to save device:', err);
        // Extract and format validation errors from API response
        const ed = findErrData(err);
        validationErrors.value = ed as any ?? {
            message: 'Une erreur est survenue lors de la sauvegarde',
            data: {},
            code: '_'
        };
    } finally {
        isSaving.value = false; // Reset saving state regardless of outcome
    }
}

// Cancel editing and notify parent component
function cancelEdit() {
    emit('cancel');
}

// Watch for userInput element to be available, then focus it
// This improves UX by automatically focusing the search field
watch(userInput, el => {
    if (el) {
        el.$el.focus();
    }
});

// Component initialization code (compatible with Vue's Suspense feature)

await fetchOptions(); // Load rooms and device types before rendering UI

// If we have a user ID in the model, fetch user details to display in dropdown
if (model.value?.userId) {
    try {
        const user = await api.users.findUser({ userId: model.value.userId });
        if (user) {
            // Add user to dropdown options so it's visible when form loads
            editFormUsers.value = [user];
        }
    } catch (err) {
        console.error('Failed to fetch owner:', err);
    }
}

// Watch for changes to the selected device type
watch(() => model.value.typeId, t => {
    if (t) {
        // Find the complete device type object from our loaded types
        const typeObj = deviceTypes.value.find(x => x.id === t);
        selectedDeviceType.value = typeObj ?? null;

        // Update the attribute fields to match the selected device type
        if (typeObj) {
            // Remove attributes that don't exist in this device type
            for (const at of Object.keys(model.value.attributes)) {
                if (typeObj.attributes.includes(at as any)) {
                    continue;
                } else {
                    delete model.value.attributes[at];
                }
            }
            // Add missing attributes with appropriate default values based on type
            for (const at of typeObj.attributes) {
                const content = attributeTypeContents[at];
                if (!(at in model.value.attributes)) {
                    model.value.attributes[at] = content === Number ? 0 : content === String ? "" : false;
                }
            }
        }
    }
}, { immediate: true }) // immediate:true runs the watch handler immediately on component creation
</script>

<template>
    <div>
        <!-- Conditional heading based on whether we're creating or editing -->
        <h2 v-if="isNew" class="mb-3">Nouvel appareil</h2>
        <h2 v-else class="mb-3">Modifier l'appareil</h2>
        
        <!-- Display global validation error messages -->
        <div v-if="validationErrors?.message" class="mb-3">
            <Message severity="error">{{ validationErrors.message }}</Message>
        </div>

        <form @submit.prevent="saveDevice"> <!-- prevent default form submission -->
            <div class="form-section">
                <div class="form-group">
                    <!-- FloatLabel creates a floating label effect when input has content -->
                    <FloatLabel variant="in">
                        <InputText v-model="model.name" fluid id="name"
                            :invalid="validationErrors?.data?.name?.length > 0" />
                        <label for="name">Nom</label>
                        <!-- Component to display field-specific validation errors -->
                        <ValidationErrList :errors="validationErrors?.data?.name" />
                    </FloatLabel>
                </div>

                <!-- Additional form fields follow similar patterns -->
                <div class="form-group">
                    <FloatLabel variant="in">
                        <Textarea v-model="model.description" id="description"
                            :invalid="validationErrors?.data?.description?.length > 0" fluid />
                        <label for="description">Description</label>
                        <ValidationErrList :errors="validationErrors?.data?.description" />
                    </FloatLabel>
                </div>

                <div class="form-row">
                    <!-- Device type dropdown with option to create new types -->
                    <div class="form-group">
                        <FloatLabel variant="in">
                            <Select v-model="model.typeId" :options="deviceTypes" optionLabel="name" optionValue="id" ref="typeSelect"
                                :invalid="validationErrors?.data?.typeId?.length > 0" fluid filter>
                                <!-- Footer slot adds a "Create" button at bottom of dropdown -->
                                <template #footer>
                                    <div class="m-2">
                                        <Button variant="text" severity="secondary" label="Créer" icon="pi pi-plus"
                                            fluid @click="showDeviceTypeDialog()" />
                                    </div>
                                </template>
                            </Select>
                            <label>Modèle</label>
                            <ValidationErrList :errors="validationErrors?.data?.typeId" />
                        </FloatLabel>
                    </div>

                    <!-- Room dropdown with create option -->
                    <div class="form-group">
                        <FloatLabel variant="in">
                            <Select v-model="model.roomId" :options="rooms" optionLabel="name" optionValue="id" ref="roomSelect"
                                :invalid="validationErrors?.data?.roomId?.length > 0" fluid show-clear filter>
                                <template #footer>
                                    <div class="m-2">
                                        <Button variant="text" severity="secondary" label="Créer" icon="pi pi-plus"
                                            fluid @click="showRoomDialog" />
                                    </div>
                                </template>
                            </Select>
                            <label>Salle</label>
                            <ValidationErrList :errors="validationErrors?.data?.roomId" />
                        </FloatLabel>
                    </div>

                    <!-- User select with search functionality -->
                    <div class="form-group">
                        <FloatLabel variant="in">
                            <Select v-model="model.userId" :options="editFormUsers"
                                :optionLabel="x => x.firstName + ' ' + x.lastName" :optionValue="x => x.id" fluid
                                show-clear>
                                <!-- Search box in the header of the dropdown -->
                                <template #header>
                                    <div class="p-2">
                                        <InputText placeholder="Rechercher un propriétaire" class="w-full" fluid
                                            @update:modelValue="updateUsersSelect" ref="userInput" id="userInput" />
                                    </div>
                                </template>
                            </Select>
                            <label>Propriétaire</label>
                            <ValidationErrList :errors="validationErrors?.data?.userId" />
                        </FloatLabel>
                    </div>
                </div>

                <div class="form-row">
                    <!-- Energy consumption input with unit suffix -->
                    <div class="form-group">
                        <FloatLabel variant="in">
                            <InputNumber v-model="model.energyConsumption" id="energy" fluid
                                :invalid="validationErrors?.data?.energyConsumption?.length > 0" suffix=" Wh"
                                :min="0" />
                            <label for="energy">Consommation d'énergie (Wh)</label>
                            <ValidationErrList :errors="validationErrors?.data?.energyConsumption" />
                        </FloatLabel>
                    </div>

                    <!-- Power toggle switch with custom styling -->
                    <div class="form-group align-self-center flex-grow-0">
                        <ToggleSwitch v-model="model.powered" class="mx-auto d-block"
                            style="--p-toggleswitch-width: 80px; --p-toggleswitch-height: 40px; --p-toggleswitch-handle-size: 30px;" />
                    </div>
                </div>
                
                <!-- Dynamic attributes section based on selected device type -->
                <div v-if="Object.keys(model.attributes).length > 0">
                    <h3>Données</h3>
                    <div class="attributes form-row">
                        <!-- Dynamically render appropriate inputs for each attribute type -->
                        <IftaLabel class="attr" v-for="(val, type) of model.attributes" :key="type">
                            <InputNumber v-if="attributeTypeContents[type] === Number" v-model="model.attributes[type]"
                                v-bind="attributeTypeFormats[type]" fluid />
                            <!-- TODO: Implement inputs for string/bool attribute types -->
                            <label>{{ attributeTypeLabels[type] }}</label>
                            <ValidationErrList :errors="validationErrors?.data?.attributes?.[type]" />
                        </IftaLabel>
                    </div>
                </div>
            </div>

            <!-- Form action buttons -->
            <div class="form-actions">
                <Button type="button" label="Annuler" class="p-button-outlined" @click="cancelEdit"
                    :disabled="isSaving" />
                <Button type="submit" label="Enregistrer" icon="pi pi-check" :loading="isSaving" />
            </div>
        </form>

        <!-- Dialogs for creating new device types and rooms -->
        <Dialog header="Créer un modèle" v-model:visible="newDeviceTypeDialogVisible" modal :style="{ width: '450px' }">
            <DeviceTypeForm hide-header v-model="newDeviceTypeDialogData" @save-success="useCreatedDeviceType"
                @cancel="newDeviceTypeDialogVisible = false" />
        </Dialog>
        <Dialog header="Créer une salle" v-model:visible="newRoomDialogVisible" modal :style="{ width: '450px' }">
            <RoomForm hide-header v-model="newRoomDialogData" @save-success="useCreatedRoom"
                @cancel="newRoomDialogVisible = false" />
        </Dialog>
    </div>

</template>

<style lang="css" scoped>
/* CSS is scoped to this component only thanks to "scoped" attribute */
.form-section {
    display: flex;
    flex-direction: column;
    gap: 1.5rem; /* Space between form elements */
    margin-bottom: 1.5rem;
}

.form-row {
    display: flex;
    flex-wrap: wrap; /* Allows items to wrap on small screens */
    gap: 1rem;
}

.form-group {
    flex: 1; /* Each group takes equal space */
    min-width: 160px; /* Ensures minimum readable width */
}

.form-actions {
    display: flex;
    justify-content: flex-end; /* Aligns buttons to the right */
    gap: 1rem;
    padding-top: 1rem;
}

.attr {
    flex: 1 0 0;
    min-width: 320px;
    max-width: 50%; /* Limits width on large screens */
}

/* Responsive design for mobile screens */
@media (max-width: 768px) {
    .attr {
        min-width: 75%; /* Takes more width on small screens */
        max-width: unset;
    }
}
</style>