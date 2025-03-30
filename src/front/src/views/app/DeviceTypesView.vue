<script lang="ts" setup>
import api, { CompleteDeviceType } from '@/api';
import FullscreenSpinner from '@/components/FullscreenSpinner.vue';
import DeviceTypeForm, { DeviceTypeFormModel, defaultDeviceTypeFormModel } from '@/components/DeviceTypeForm.vue';
import { useGuards } from '@/guards';
import { attributeTypeLabels, deviceCategoryLabels } from '@/labels';
import { computed, ref, watch } from 'vue';

const guards = useGuards();

const deviceTypes = ref<CompleteDeviceType[]>([]);
const filters = ref({
    name: null as string | null,
});
const promise = ref<ReturnType<typeof api.deviceTypes.getDeviceTypes>>(null);
const searchTimer = ref<ReturnType<typeof setTimeout>>(null);

const loading = computed(() => promise.value != null);

// Form management
const showForm = ref(false);
const formModel = ref<DeviceTypeFormModel>(defaultDeviceTypeFormModel());
const editingTypeId = ref<number | undefined>(undefined);
const dialogHeader = computed(() => editingTypeId.value ? 'Modifier le modèle' : 'Nouveau modèle');

function load() {
    let thisProm: any;
    async function fetchFromAPI() {
        try {
            const f = filters.value;
            const response = await api.deviceTypes.getDeviceTypes({
                name: f.name,
            });
            if (thisProm === promise.value) {
                deviceTypes.value = response.deviceTypes;
            }
        } catch (e) {
            console.error('Failed to load device types:', e);
        } finally {
            if (thisProm === promise.value) {
                promise.value = null;
            }
        }
    }
    thisProm = fetchFromAPI()
    promise.value = thisProm;
}

function openCreateForm() {
    if (!guards.mustManage()) {
        return;
    }

    formModel.value = defaultDeviceTypeFormModel();
    editingTypeId.value = undefined;
    showForm.value = true;
}

function openEditForm(deviceType: CompleteDeviceType) {
    if (!guards.mustManage()) {
        return;
    }

    formModel.value = {
        name: deviceType.name,
        attributes: [...deviceType.attributes] as string[],
        category: deviceType.category
    };
    editingTypeId.value = deviceType.id;
    showForm.value = true;
}

function closeForm() {
    showForm.value = false;
}

function handleFormSuccess() {
    closeForm();
    load(); // Refresh the device types list
}

watch(filters, () => {
    // Trigger refresh when changing filters.
    clearTimeout(searchTimer.value);
    searchTimer.value = setTimeout(load, 150);
}, { deep: true });
load(); // Initial load
</script>

<template>
    <div class="device-types-view">
        <div class="container-lg"> <!-- Need this div so scroll is placed to the edge of the screen -->
            <h1 class="header">Modèles</h1>

            <div class="search-section">
                <InputText v-model="filters.name" class="search-bar" placeholder="Rechercher un modèle" />
                <Button label="Créer" icon="pi pi-plus" class="create-button" @click="openCreateForm" />
            </div>

            <div class="device-types-list">
                <div v-if="loading" class="loading-container">
                    <FullscreenSpinner />
                </div>
                <div v-else-if="deviceTypes.length === 0" class="no-results">
                    Aucun modèle trouvé.
                </div>
                <div v-else class="device-type-cards">
                    <div v-for="deviceType in deviceTypes" :key="deviceType.id" class="device-type-card"
                        @click="openEditForm(deviceType)">
                        <div class="device-type-info">
                            <h3 class="device-type-name">{{ deviceType.name }}</h3>
                            <p class="device-type-category">
                                {{ deviceCategoryLabels[deviceType.category] }}
                            </p>
                            <div class="device-type-attributes">
                                <Chip v-for="attr in deviceType.attributes" :key="attr"
                                    :label="attributeTypeLabels[attr]" class="attribute-chip" />
                                <p v-if="deviceType.attributes.length === 0" class="no-attributes">
                                    Aucun attribut
                                </p>
                            </div>
                        </div>
                        <div class="device-type-actions">
                            <Button icon="pi pi-chevron-right" class="p-button-text" />
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Device Type Form Dialog -->
        <Dialog v-model:visible="showForm" :header="dialogHeader" modal :style="{ width: '450px' }" :closable="!loading"
            :closeOnEscape="!loading">
            <DeviceTypeForm v-model="formModel" :typeId="editingTypeId" hide-header @save-success="handleFormSuccess"
                @cancel="closeForm" />
        </Dialog>
    </div>
</template>

<style lang="css" scoped>
.device-types-view {
    display: flex;
    flex-direction: column;
    padding: 1rem;
    height: 100%;

}

.header {
    margin: 2rem 0;
    text-align: center;
    font-size: 3rem;
}

.search-section {
    display: flex;
    gap: 1rem;
    margin-bottom: 1.5rem;
    align-items: center;
}

.search-bar {
    flex-grow: 1;
}

.device-type-cards {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.device-type-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem 1.5rem;
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;
}

.device-type-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.device-type-info {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.device-type-name {
    margin: 0;
    font-size: 1.2rem;
}

.device-type-category {
    margin: 0;
    color: #666;
    font-size: 0.9rem;
}

.device-type-attributes {
    display: flex;
    flex-wrap: wrap;
    gap: 0.25rem;
}

.attribute-chip {
    font-size: 0.8rem;
    padding: 0.25rem 0.5rem;
}

.no-attributes {
    margin: 0;
    color: #999;
    font-style: italic;
    font-size: 0.8rem;
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

.loading-container {
    min-height: 200px;
    position: relative;
}

@media (max-width: 768px) {
    .search-section {
        flex-direction: column;
        align-items: stretch;
    }

    .search-bar,
    .create-button {
        width: 100%;
    }

    .create-button {
        display: flex;
    }

    .create-button :deep(.p-button) {
        width: 100%;
    }
}
</style>