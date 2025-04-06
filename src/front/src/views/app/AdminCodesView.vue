<script lang="ts" setup>
import api, { InviteCode } from '@/api';
import FullscreenSpinner from '@/components/FullscreenSpinner.vue';
import { ref, computed, watch } from 'vue';
import { useToast } from 'primevue';
import { Dialog } from 'primevue';
import { InviteCodeInput } from '@/api/gen/models/InviteCodeInput';
import { roleLabels, roles } from '@/labels';

const inviteCodes = ref<InviteCode[]>([]);
const loading = ref(false);
const toast = useToast();
const showCreateDialog = ref(false);
const newInviteCode = ref<InviteCodeInput>({
    role: "RESIDENT",
    usagesLeft: 5
});

async function loadInviteCodes() {
    loading.value = true;
    try {
        const response = await api.inviteCodes.getInviteCodes();
        inviteCodes.value = response.inviteCodes;
    } catch (error) {
        console.error('Failed to load invite codes:', error);
    } finally {
        loading.value = false;
    }
}

async function deleteInviteCode(id: string) {
    try {
        await api.inviteCodes.deleteInviteCode({ id });
        toast.add({
            severity: 'success',
            summary: 'Code supprimé',
            detail: 'Le code d\'invitation a été supprimé avec succès.',
            life: 3000
        });
        loadInviteCodes();
    } catch (error) {
        console.error('Failed to delete invite code:', error);
        toast.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Impossible de supprimer le code d\'invitation.',
            life: 3000
        });
    }
}

async function createInviteCode() {
    try {
        await api.inviteCodes.createInviteCode({ inviteCodeInput: newInviteCode.value });
        toast.add({
            severity: 'success',
            summary: 'Code créé',
            detail: 'Le code d\'invitation a été créé avec succès.',
            life: 3000
        });
        showCreateDialog.value = false;
        loadInviteCodes();
    } catch (error) {
        console.error('Failed to create invite code:', error);
        toast.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Impossible de créer le code d\'invitation.',
            life: 3000
        });
    }
}

loadInviteCodes();
</script>

<template>
    <div class="admin-codes-view">
        <div class="container-lg">
            <h1 class="sub-page-title">Codes d'invitation</h1>

            <div class="actions">
                <Button label="Créer" icon="pi pi-plus" class="create-button" @click="showCreateDialog = true" />
            </div>

            <Dialog v-model:visible="showCreateDialog" header="Créer un code d'invitation" modal>
                <form @submit.prevent="createInviteCode" style="display: flex; gap: 8px; flex-direction: column;">
                    <IftaLabel>
                        <Select v-model="newInviteCode.role" :options="roles" :option-label="x => roleLabels[x]" fluid />
                        <label for="role">Rôle</label>
                    </IftaLabel>
                    <IftaLabel>
                        <InputNumber v-model="newInviteCode.usagesLeft" :min="1" fluid />
                        <label for="usagesLeft">Usages restants</label>
                    </IftaLabel>
                    <div class="form-actions">
                        <Button label="Annuler" class="p-button-outlined" @click="showCreateDialog = false" />
                        <Button type="submit" label="Créer" icon="pi pi-check" />
                    </div>
                </form>
            </Dialog>

            <div v-if="loading" class="loading-container">
                <FullscreenSpinner />
            </div>

            <div v-else-if="inviteCodes.length === 0" class="no-results">
                Aucun code d'invitation trouvé.
            </div>

            <div v-else class="codes-list">
                <div v-for="code in inviteCodes" :key="code.id" class="code-card">
                    <div class="code-info">
                        <h3 class="code-id">{{ code.id }}</h3>
                        <p class="code-role">Rôle: {{ code.role }}</p>
                        <p class="code-creator" v-if="code.creatorId">Créé par l'utilisateur ID: {{ code.creatorId }}
                        </p>
                        <p class="code-date">Créé le: {{ new Date(code.createdAt).toLocaleString('fr-FR') }}</p>
                    </div>
                    <div class="code-actions">
                        <Button icon="pi pi-trash" class="p-button-danger" @click="deleteInviteCode(code.id)" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style lang="css" scoped>
.admin-codes-view {
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow-y: auto;
}

.codes-list {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.code-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
    transition: transform 0.2s, box-shadow 0.2s;
}

.code-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.code-info {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
}

.code-id {
    margin: 0;
    font-size: 1.2rem;
}

.code-role,
.code-creator,
.code-date {
    margin: 0;
    color: #666;
    font-size: 0.9rem;
}

.loading-container {
    min-height: 200px;
    display: flex;
    justify-content: center;
    align-items: center;
}

.no-results {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 200px;
    color: #666;
    font-style: italic;
}

.actions {
    margin-bottom: 1rem;
    display: flex;
    justify-content: flex-end;
}

.form-group {
    margin-bottom: 1rem;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
}
</style>