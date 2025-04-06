<script lang="ts">

export const defaultDeviceTypeFormModel = () => ({
    name: "" as string,
    attributes: [] as string[],
    category: "OTHER" as string
});

export type DeviceTypeFormModel = ReturnType<typeof defaultDeviceTypeFormModel>;
</script>

<script lang="ts" setup>
import { computed, ref, useTemplateRef } from 'vue';
import { Popover, useToast } from 'primevue';
import api, { ValidationErrorResponse, findErrData, DeviceCategory, CompleteDeviceType } from '@/api';
import ValidationErrList from '@/components/ValidationErrList.vue';
import { attributeTypes, attributeTypeLabels, deviceCategories, deviceCategoryLabels } from '@/labels';

const props = defineProps<{ typeId?: number, hideHeader?: boolean }>();
const isNew = props.typeId == null;

const toast = useToast();

const model = defineModel<DeviceTypeFormModel>();
const showHeader = computed(() => props.hideHeader !== true);

const isSaving = ref(false);
const validationErrors = ref<ValidationErrorResponse<DeviceTypeFormModel> | null>(null);
const attrPopover = useTemplateRef<InstanceType<typeof Popover>>("attrPopover");
// Popover to delete the device type
const deletePopover = useTemplateRef<InstanceType<typeof Popover>>("deletePopover");
const deletePromise = ref<Promise<any> | null>(null);
const deleteErr = ref<string | null>(null);

const emit = defineEmits<{
    'update:modelValue': [value: DeviceTypeFormModel];
    'save-success': [value: CompleteDeviceType];
    'cancel': [];
}>();

// Save device type changes
async function saveDeviceType() {
    if (!model.value) return;

    isSaving.value = true;
    validationErrors.value = null;

    try {
        if (isNew) {
            const result = await api.deviceTypes.createDeviceType({
                deviceTypeInput: {
                    name: model.value.name,
                    attributes: model.value.attributes as any[],
                    category: model.value.category as DeviceCategory
                }
            });
            toast.add({
                severity: 'success',
                summary: 'Modèle créé',
                detail: 'Le modèle a été créé avec succès',
                life: 3000
            });
            emit('save-success', result);
        } else {
            const result = await api.deviceTypes.updateDeviceType({
                deviceTypeId: props.typeId!,
                deviceTypeInput: {
                    name: model.value.name,
                    attributes: model.value.attributes as any[],
                    category: model.value.category as DeviceCategory
                }
            });
            toast.add({
                severity: 'success',
                summary: 'Modèle modifié',
                detail: 'Les modifications ont été enregistrées avec succès',
                life: 3000
            });
            emit('save-success', result);
        }
    } catch (err) {
        console.error('Failed to save device type:', err);
        const ed = findErrData(err);
        validationErrors.value = ed as any ?? {
            message: 'Une erreur bizarre est survenue lors de la sauvegarde',
            data: {},
            code: '_'
        };
    } finally {
        isSaving.value = false;
    }
}

// Delete device type
function deleteDeviceType() {
    if (!model.value || deletePromise.value || isNew) return;

    deleteErr.value = null;
    deletePromise.value = null;

    deletePromise.value = api.deviceTypes.deleteDeviceType({ deviceTypeId: props.typeId! })
        .then(() => {
            toast.add({
                severity: 'success',
                summary: 'Modèle supprimé',
                detail: 'Le modèle a été supprimé avec succès',
                life: 3000
            });
            emit('save-success', null);
        })
        .catch(err => {
            console.error('Failed to delete device type:', err);
            deleteErr.value = findErrData(err)?.message ?? 'Une erreur est survenue lors de la suppression';
        })
        .finally(() => {
            deletePromise.value = null;
        });
}

function cancelEdit() {
    emit('cancel');
}

// Initialize component (with Suspense)
// If we have a typeId, load the device type
if (props.typeId) {
    try {
        const deviceType = await api.deviceTypes.getDeviceTypeById({ deviceTypeId: props.typeId });
        if (deviceType) {
            model.value = {
                name: deviceType.name,
                attributes: deviceType.attributes as any[],
                category: deviceType.category as string
            };
        }
    } catch (err) {
        console.error('Failed to fetch device type:', err);
    }
}
</script>

<template>
    <div>
        <h2 v-if="isNew && showHeader" class="mb-3">Nouveau modèle</h2>
        <h2 v-else-if="showHeader" class="mb-3">Modifier le modèle</h2>
        <!-- Validation error message -->
        <div v-if="validationErrors?.message" class="mb-3">
            <Message severity="error" class="mt-1">{{ validationErrors.message }}</Message>
        </div>

        <form @submit.prevent="saveDeviceType">
            <div class="form-section">
                <div class="form-group">
                    <FloatLabel variant="in">
                        <InputText v-model="model.name" fluid id="name"
                            :invalid="validationErrors?.data?.name?.length > 0" />
                        <label for="name">Nom</label>
                        <ValidationErrList :errors="validationErrors?.data?.name" />
                    </FloatLabel>
                </div>

                <div class="form-group">
                    <FloatLabel variant="in">
                        <Select v-model="model.category" :options="deviceCategories"
                            :option-label="x => deviceCategoryLabels[x]" fluid
                            :invalid="validationErrors?.data?.category?.length > 0" />
                        <label>Catégorie</label>
                        <ValidationErrList :errors="validationErrors?.data?.category" />
                    </FloatLabel>
                </div>

                <div class="form-group">
                    <label class="d-block mb-2">Attributs</label>
                    <div class="attributes-container">
                        <div class="selected-attributes mb-2">
                            <Chip v-for="attr in model.attributes" :key="attr" :label="attributeTypeLabels[attr]"
                                removable @remove="model.attributes = model.attributes.filter(a => a !== attr)"
                                class="mb-1 me-1" />
                            <Button rounded icon="pi pi-plus" severity="contrast" @click="x => attrPopover.show(x)" />
                            <Popover ref="attrPopover" pt:content="p-2">
                                <div class="addable-attributes">
                                    <div class="-item"
                                        v-for="a in attributeTypes.filter(a => !model.attributes.includes(a))" @click="() => {
                                            model.attributes = [...model.attributes, a];
                                            attrPopover.hide();
                                        }">
                                        {{ attributeTypeLabels[a] }}
                                    </div>
                                </div>
                            </Popover>
                        </div>
                        <ValidationErrList :errors="validationErrors?.data?.attributes" />
                    </div>
                </div>
            </div>

            <div class="form-actions">
                <Button v-if="!isNew" type="button" label="Supprimer" icon="pi pi-trash" class="me-auto" severity="danger"
                    @click="deletePopover.toggle($event)" />
                <Popover ref="deletePopover">
                    <p>Voulez-vous vraiment supprimer ce modèle ?</p>
                    <Message v-if="deleteErr" severity="error">{{ deleteErr }}</Message>
                    <div class="d-flex gap-2 mt-2">
                        <Button label="Annuler" severity="secondary" class="px-4" @click="deletePopover.hide()" />
                        <Button label="Supprimer le modèle" fluid severity="danger" @click="deleteDeviceType"
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

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    padding-top: 1rem;
}

.selected-attributes {
    display: flex;
    flex-wrap: wrap;
}

.addable-attributes {
    display: flex;
    flex-direction: column;
    max-height: 280px;
    min-width: 240px;
    overflow: auto;

    & .-item {
        padding: 0.5rem 0.75rem;
        border-radius: 8px;
        transition: all 0.3s ease;

        &:hover {
            background-color: var(--p-select-option-focus-background);
        }

        cursor: pointer;
    }
}
</style>