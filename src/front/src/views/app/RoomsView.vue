<script lang="ts" setup>
import api, { CompleteRoom } from '@/api';
import FullscreenSpinner from '@/components/FullscreenSpinner.vue';
import RoomForm, { RoomFormModel, defaultRoomFormModel } from '@/components/RoomForm.vue';
import { useGuards } from '@/guards';
import { computed, ref, watch } from 'vue';

const guards = useGuards();

const rooms = ref<CompleteRoom[]>([]);
const filters = ref({
    name: null as string | null,
});
const promise = ref<ReturnType<typeof api.rooms.getRooms>>(null);
const searchTimer = ref<ReturnType<typeof setTimeout>>(null);

const loading = computed(() => promise.value != null);

// Form management
const showForm = ref(false);
const formModel = ref<RoomFormModel>(defaultRoomFormModel());
const editingRoomId = ref<number | null>(null);
const dialogHeader = computed(() => editingRoomId.value ? 'Modifier la salle' : 'Nouvelle salle');

function load() {
    let thisProm: any;
    async function fetchFromAPI() {
        try {
            const f = filters.value;
            const response = await api.rooms.getRooms({
                name: f.name,
            });
            if (thisProm === promise.value) {
                rooms.value = response.rooms;
            }
        } catch (e) {
            console.error('Failed to load rooms:', e);
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

    formModel.value = defaultRoomFormModel();
    editingRoomId.value = null;
    showForm.value = true;
}

function openEditForm(room: CompleteRoom) {
    if (!guards.mustManage()) {
        return;
    }

    formModel.value = {
        name: room.name,
        color: room.color,
        ownerId: room.owner?.id || null
    };
    editingRoomId.value = room.id;
    showForm.value = true;
}

function closeForm() {
    showForm.value = false;
}

function handleFormSuccess() {
    closeForm();
    load(); // Refresh the room list
}

watch(filters, () => {
    // Trigger refresh when changing filters.
    clearTimeout(searchTimer.value);
    searchTimer.value = setTimeout(load, 150);
}, { deep: true });
load(); // Initial load
</script>

<template>
    <div class="rooms-view">
        <div class="container-lg pb-3">
            <h1 class="sub-page-title pb-3">Salles</h1>

            <div class="search-section">
                <IconField class="flex-grow-1">
                    <InputIcon class="pi pi-search" />
                    <InputText v-model="filters.name" fluid placeholder="Rechercher une salle" />
                </IconField>
                <Button label="Créer" icon="pi pi-plus" class="create-button" @click="openCreateForm" />
            </div>

            <div class="rooms-list">
                <div v-if="loading" class="loading-container">
                    <FullscreenSpinner />
                </div>
                <div v-else-if="rooms.length === 0" class="no-results">
                    Aucune salle trouvée.
                </div>
                <div v-else class="room-cards">
                    <div v-for="room in rooms" :key="room.id" class="room-card"
                        :style="{ borderLeft: `6px solid #${room.color.toString(16).padStart(6, '0')}` }"
                        @click="openEditForm(room)">
                        <div class="room-info">
                            <h3 class="room-name">{{ room.name }}</h3>
                            <p v-if="room.owner" class="room-owner">
                                Occupant: {{ room.owner.firstName }} {{ room.owner.lastName }}
                            </p>
                            <p v-else class="room-owner empty">Aucun occupant</p>
                        </div>
                        <div class="room-actions">
                            <Button icon="pi pi-chevron-right" class="p-button-text" />
                        </div>
                    </div>
                </div>
            </div>

            <!-- Room Form Dialog -->
            <Dialog v-model:visible="showForm" :header="dialogHeader" modal :style="{ 'width': 'min(97vw, 450px)' }"
                :closable="!loading" :closeOnEscape="!loading">
                <RoomForm v-model="formModel" :roomId="editingRoomId" hide-header @save-success="handleFormSuccess"
                    @cancel="closeForm" />
            </Dialog>
        </div>
    </div>
</template>

<style lang="css" scoped>
.rooms-view {
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow-y: auto;
}

.search-section {
    display: flex;
    gap: 1rem;
    margin-bottom: 1.5rem;
    align-items: center;
}

.room-cards {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.room-card {
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

.room-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.room-info {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
}

.room-name {
    margin: 0;
    font-size: 1.2rem;
}

.room-owner {
    margin: 0;
    color: #666;
    font-size: 0.9rem;
}

.room-owner.empty {
    color: #999;
    font-style: italic;
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