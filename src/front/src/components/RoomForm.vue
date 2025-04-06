<script lang="ts">

export const defaultRoomFormModel = () => ({
    name: "" as string,
    color: 0x2196F3 as number, // Default to a nice blue color
    ownerId: null as number | null,
});

export type RoomFormModel = ReturnType<typeof defaultRoomFormModel>;
</script>

<script lang="ts" setup>
import { computed, ref, useTemplateRef, watch } from 'vue';
import { Popover, useToast } from 'primevue';
import api, { UserProfile, ValidationErrorResponse, findErrData } from '@/api';
import ValidationErrList from '@/components/ValidationErrList.vue';

const props = defineProps<{ roomId?: number, hideHeader?: boolean }>();
const isNew = props.roomId == null;

const toast = useToast();

const model = defineModel<RoomFormModel>();
const color = computed<string>({
    get: () => model.value.color.toString(16).padStart(6, '0'),
    set: v => model.value.color = parseInt(v, 16)
});

const showHeader = computed(() => props.hideHeader !== true);

const isSaving = ref(false);
const validationErrors = ref<ValidationErrorResponse<RoomFormModel> | null>(null);

// Users displayed in the Select dropdown for users
const editFormUsers = ref<UserProfile[]>([]);

// User <Select/> input to do focus
const userInput = ref<any>(null);
// Popover to delete the room
const deletePopover = useTemplateRef<InstanceType<typeof Popover>>("deletePopover");
const deletePromise = ref<Promise<any> | null>(null);
const deleteErr = ref<string | null>(null);

const emit = defineEmits(['update:modelValue', 'save-success', 'cancel']);

async function updateUsersSelect(val: string | undefined) {
    if (val) {
        // Search users by name
        try {
            editFormUsers.value = (await api.users.searchUsers({
                fullName: val
            })).profiles;
        } catch (err) {
            console.error('Failed to search users:', err);
        }
    }
}

// Save room changes
async function saveRoom() {
    if (!model.value) return;

    isSaving.value = true;
    validationErrors.value = null;

    try {
        if (isNew) {
            const result = await api.rooms.createRoom({
                roomInput: {
                    name: model.value.name,
                    color: model.value.color,
                    ownerId: model.value.ownerId
                }
            });
            toast.add({
                severity: 'success',
                summary: 'Salle créée',
                detail: 'La salle a été créée avec succès',
                life: 3000
            });
            emit('save-success', result);
        } else {
            const result = await api.rooms.updateRoom({
                roomId: props.roomId!,
                roomInput: {
                    name: model.value.name,
                    color: model.value.color,
                    ownerId: model.value.ownerId
                }
            });
            toast.add({
                severity: 'success',
                summary: 'Salle modifiée',
                detail: 'Les modifications ont été enregistrées avec succès',
                life: 3000
            });
            emit('save-success', result);
        }
    } catch (err) {
        console.error('Failed to save room:', err);
        const ed = findErrData(err);
        validationErrors.value = ed as any ?? {
            message: 'Une erreur est survenue lors de la sauvegarde',
            data: {},
            code: '_'
        };
    } finally {
        isSaving.value = false;
    }
}

function deleteRoom() {
    if (!model.value || deletePromise.value) return;

    deleteErr.value = null;
    deletePromise.value = null;

    deletePromise.value = api.rooms.deleteRoom({ roomId: props.roomId! })
        .then(() => {
            toast.add({
                severity: 'success',
                summary: 'Salle supprimée',
                detail: 'La salle a été supprimée avec succès',
                life: 3000
            });
            emit('save-success', null);
        })
        .catch(err => {
            console.error('Failed to delete room:', err);
            deleteErr.value = findErrData(err)?.message ?? 'Une erreur est survenue lors de la suppression';
        })
        .finally(() => {
            deletePromise.value = null;
        });
}

function cancelEdit() {
    emit('cancel');
}

// Focus the user <Select/> search box when it pops up
watch(userInput, el => {
    if (el) {
        el.$el.focus();
    }
});

// Initialize component (with Suspense)
// If we have an owner, add it to the dropdown options
if (model.value?.ownerId) {
    try {
        const user = await api.users.findUser({ userId: model.value.ownerId });
        if (user) {
            // So the <Select/> box isn't empty 
            editFormUsers.value = [user];
        }
    } catch (err) {
        console.error('Failed to fetch owner:', err);
    }
}
</script>

<template>
    <div>
        <h2 v-if="isNew && showHeader" class="mb-3">Nouvelle salle</h2>
        <h2 v-else-if="showHeader" class="mb-3">Modifier la salle</h2>
        <!-- Validation error message -->
        <div v-if="validationErrors?.message" class="mb-3">
            <Message severity="error">{{ validationErrors.message }}</Message>
        </div>

        <form @submit.prevent="saveRoom">
            <div class="form-section">
                <div class="form-group">
                    <FloatLabel variant="in">
                        <InputText v-model="model.name" fluid id="name"
                            :invalid="validationErrors?.data?.name?.length > 0" />
                        <label for="name">Nom</label>
                        <ValidationErrList :errors="validationErrors?.data?.name" />
                    </FloatLabel>
                </div>

                <div class="form-group px-1">
                    <label class="d-block mb-2">Couleur</label>
                    <ColorPicker v-model="color" format="hex" class="w-100"
                        style="--p-colorpicker-preview-width: 100%" />
                    <ValidationErrList :errors="validationErrors?.data?.color" />
                </div>

                <div class="form-group">
                    <FloatLabel variant="in">
                        <Select v-model="model.ownerId" :options="editFormUsers"
                            :optionLabel="x => x.firstName + ' ' + x.lastName" :optionValue="x => x.id" fluid
                            show-clear>
                            <template #header>
                                <div class="p-2">
                                    <InputText placeholder="Rechercher un occupant" class="w-full" fluid
                                        @update:modelValue="updateUsersSelect" ref="userInput" id="userInput" />
                                </div>
                            </template>
                        </Select>
                        <label>Occupant</label>
                        <ValidationErrList :errors="validationErrors?.data?.ownerId" />
                    </FloatLabel>
                </div>
            </div>

            <div class="form-actions">
                <Button type="button" label="Supprimer" icon="pi pi-trash" class="me-auto" severity="danger"
                    @click="deletePopover.toggle($event)" v-if="!isNew" />
                <Popover ref="deletePopover">
                    <p>Voulez-vous vraiment supprimer cette salle ?</p>
                    <Message v-if="deleteErr" severity="error">{{ deleteErr }}</Message>
                    <div class="d-flex gap-2 mt-2">
                        <Button label="Annuler" severity="secondary" class="px-4" @click="deletePopover.hide()" />
                        <Button label="Supprimer la salle" fluid severity="danger" @click="deleteRoom"
                            :disabled="deletePromise != null" v-if="!isNew" />
                    </div>
                </Popover>
                <Button type="button" label="Annuler" class="p-button-outlined" @click="cancelEdit"
                    :disabled="isSaving" />
                <Button type="submit" label="Enregistrer" icon="pi pi-check" :loading="isSaving" />
            </div>
        </form>
    </div>
</template>

<style lang="css" scoped>
/* Form styling */
.form-section {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
    margin-bottom: 1.5rem;
}

.form-group {
    flex: 1;
    min-width: 160px;
}

.color-preview {
    width: 40px;
    height: 40px;
    border-radius: 4px;
    border: 1px solid #ddd;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    padding-top: 1rem;
}

@media (max-width: 768px) {
    .color-picker-container {
        flex-direction: column;
        align-items: flex-start;
    }
}
</style>