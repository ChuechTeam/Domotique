<script lang="ts" setup>
import api, { ActionLogFlags, ActionLogOperation, ActionLogTarget, CompleteActionLog } from '@/api';
import { actionLogOperationLabels, actionLogOperations, actionLogTargetLabels, actionLogTargets } from '@/labels';
import { ref, onMounted, watch } from 'vue';

const actions = ref<CompleteActionLog[]>([]);
const loadingProm = ref<Promise<any> | null>(null);

const filters = ref({
    operation: null as ActionLogOperation | null,
    target: null as ActionLogTarget | null,
});

function load() {
    let thisProm: any;
    async function doIt() {
        try {
            const response = await api.actionLogs.getActionLogs({
                operation: filters.value.operation,
                target: filters.value.target,
            });
            if (thisProm === loadingProm.value) {
                actions.value = response.logs;
            }
        } catch (e) {
            console.error('Failed to load actions:', e);
        } finally {
            loadingProm.value = null;
        }
    }

    thisProm = loadingProm.value = doIt();
}

load();
watch(filters, load, { deep: true });

const typeLabels: Record<ActionLogTarget, string> = {
    DEVICE: 'l\'appareil',
    DEVICE_TYPE: 'le modèle',
    USER: 'l\'utilisateur',
    ROOM: 'la salle',
}

const opClasses: Record<string, string> = {
    CREATE: '-create',
    DELETE: '-delete',
    UPDATE: '-update',
}
function classesFor(action: CompleteActionLog) {
    const obj = {};
    obj[opClasses[action.operation]] = true;
    return obj;
}

// Format date in French
const formatDate = (d: Date) => {
    return new Intl.DateTimeFormat('fr-FR', {
        dateStyle: 'full',
        timeStyle: 'medium'
    }).format(d);
};

const flagLabels: Record<ActionLogFlags, string> = {
    "POWER_OFF": "L'appareil a été éteint",
    "POWER_ON": "L'appareil a été allumé",
    "PASSWORD_CHANGED": "Le mot de passe a été changé",
    "DELETE_REQUESTED": "Une demande de suppression a été faite",
    "DELETE_REQUEST_DELETED": "La demande de suppression a été retirée",
    "EMAIL_CONFIRMED": "L'email a été confirmé"
}
const flagIcons: Record<ActionLogFlags, string> = {
    "POWER_OFF": "pi pi-power-off",
    "POWER_ON": "pi pi-power-off",
    "EMAIL_CONFIRMED": "pi pi-at",
    "DELETE_REQUESTED": "pi pi-trash",
    "DELETE_REQUEST_DELETED": "pi pi-trash",
    "PASSWORD_CHANGED": "pi pi-key"
}
</script>

<template>
    <div class="w-100">
        <div class="container-lg">
            <h1 class="sub-page-title">Historique des actions</h1>

            <div class="controls mb-3 d-flex gap-3 flex-column flex-lg-row">
                <IftaLabel class="flex-grow-1" style="flex-basis: 0;">
                    <Select v-model="filters.operation" :options="actionLogOperations" :option-label="x => actionLogOperationLabels[x]" 
                        placeholder="Toutes les opérations" fluid show-clear/>
                    <label>Opération</label>
                </IftaLabel>
                <IftaLabel class="flex-grow-1" style="flex-basis: 0;">
                    <Select v-model="filters.target" :options="actionLogTargets" :option-label="x => actionLogTargetLabels[x]" 
                        placeholder="Tous les éléments" fluid show-clear/>
                    <label>Élément modifié</label>
                </IftaLabel>
            </div>

            <div class="d-flex flex-column gap-3">
                <div v-for="a in actions" class="action" :class="classesFor(a)">
                    <div class="act-title"><span v-if=a.user class="fw-bold">{{ a.user.firstName + ' ' + a.user.lastName
                            }}</span> a
                        <span>{{ a.operation === "CREATE" ? ' ajouté' : a.operation === "DELETE" ? ' supprimé' :
                            'modifié'
                        }}</span> <span>{{ typeLabels[a.targetType] }}</span> <span class="fw-bold">{{ a.targetName
                                ?? a.targetId
                            }}</span>
                    </div>
                    <ul v-if="a.flags.length > 0" class="flag-list">
                        <li v-for="f in a.flags"><span :class="flagIcons[f]"></span> {{ flagLabels[f] }}</li>
                    </ul>
                    <div class="act-date">
                        <i class="pi pi-clock me-1"></i>{{ formatDate(a.time) }}
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style lang="css" scoped>
.loading {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 200px;
}

.content {
    margin-top: 1rem;
}

.action {
    padding: 0.75rem;

    border-left: 4px solid black;
    border-radius: 4px;

    &.-create {
        border-left-color: rgb(4, 91, 172);
        background-color: color-mix(in oklab, rgb(4, 91, 172), white 95%);
    }

    &.-delete {
        border-left-color: rgb(190, 0, 0);
        background-color: color-mix(in oklab, rgb(190, 0, 0), white 95%);
    }

    &.-update {
        border-left-color: rgb(16, 110, 101);
        background-color: color-mix(in oklab, rgb(16, 110, 101), white 95%);
    }
}

.flag-list {
    margin: 0.25rem;
    list-style: none;
}

.act-date {
    opacity: 0.75;
    font-size: 0.8em;
}

.act-date>i {
    font-size: 0.85em;
    vertical-align: baseline;
}
</style>