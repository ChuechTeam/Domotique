<script setup lang="ts">
import { UserProfile } from '@/api';
import LevelBar from '@/components/LevelBar.vue';
import { genderLabels, levelLabels, roleLabels } from '@/labels';
import { useAuthStore } from '@/stores/auth';
import { usePrefsStore } from '@/stores/prefs';
import { storeToRefs } from 'pinia';
import { computed, ref } from 'vue';

const { profile } = defineProps<{
    profile: UserProfile
}>();

const auth = useAuthStore()
const prefsRefs = storeToRefs(usePrefsStore());
const settingsDialogOpen = ref(false);

const genderIcon = computed(() => {
    if (!profile) return null;
    switch (profile.gender) {
        case "MALE":
            return "pi pi-mars";
        case "FEMALE":
            return "pi pi-venus";
        case "UNDISCLOSED":
            return "pi pi-asterisk";
    }
})

// Generate random background color based on user ID
function getAvatarColor(id) {
    // Simple hash function based on user ID
    const hash = Math.abs(id * 31) % 360;
    return `hsl(${hash}, 70%, 70%)`;
}
</script>

<template>
    <header class="header">
        <Avatar :label="profile.firstName[0] + profile.lastName[0]"
            :style="{ 'background-color': getAvatarColor(profile.id) }" shape="circle" class="avatar"></Avatar>
        <div class="infos">
            <div class="detail" style="grid-area: name;">
                <div class="full-name">{{ profile.firstName + ' ' + profile.lastName }}</div>
                <div class="tags">
                    <Tag :value="roleLabels[profile.role]" severity="info" icon="pi pi-crown" />
                    <Tag :value="levelLabels[profile.level]" severity="success" icon="pi pi-trophy" class="ms-2" />
                    <Tag :value="genderLabels[profile.gender]" severity="secondary" :icon="genderIcon" class="ms-2"
                        style="--p-tag-secondary-background: rgb(222, 222, 222)" />
                </div>
            </div>
            <div class="ms-auto details-actions">
                <Button severity="info" icon="pi pi-cog" v-if="auth.userId === profile.id"
                    @click="settingsDialogOpen = true" />
                <Button severity="danger" style="grid-area: actions;" label="Déconnexion" icon="pi pi-sign-out"
                    @click="auth.logout" v-if="auth.userId == profile.id" class="logout" />
            </div>

            <LevelBar :value="profile.points" class="pe-2" style="grid-area: level;" />
        </div>
        <!-- Settings dialog -->
        <Dialog v-model:visible="settingsDialogOpen" modal header="Paramètres" style="width: min(97vw, 400px)">
            <div class="d-flex gap-3 mb-3">
                <Checkbox input-id="reducedMotionCheck" v-model="prefsRefs.reducedMotion" binary size="large" />
                <label for="reducedMotionCheck">Réduire les animations</label>
            </div>
            <Button fluid label="Fermer" @click="settingsDialogOpen = false" />
        </Dialog>
    </header>
</template>

<style lang="css" scoped>
.avatar {
    width: 10rem;
    height: 10rem;
    font-size: 5rem;
}

.header {
    background-color: #f6f6f6;
    /* background-color: white; */
    /* box-shadow: 0 0 12px rgba(0, 0, 0, 0.1); */

    border-bottom: none;

    border-radius: 16px 16px 0 0;

    margin-bottom: 1rem;

    display: flex;

    gap: 40px;

    justify-content: center;
}


.full-name {
    font-size: 2.5em;
}


.prof-item {
    padding-left: 16px;
    padding-right: 16px;
}

.infos {
    flex-grow: 1;

    display: grid;

    grid-template: "name actions" "level level";
    align-items: center;
}

.details-actions {
    display: flex;
    gap: 1rem;
}

@media (max-width: 1024px) {
    .logout {
        flex-grow: 1;
    }

    .header {
        flex-direction: column;

        & .avatar {
            align-self: center;
        }
    }

    .infos {
        flex-direction: column;
        display: flex;
        gap: 12px;
        align-items: start;
    }

    .details-actions {
        width: 100%;
    }

}
</style>