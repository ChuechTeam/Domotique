<script lang="ts" setup>
import api, { CompleteDevice, GetDevicesRequest } from '@/api';
import { ref, watch } from 'vue';

const devices = ref<CompleteDevice[]>([]);
const filters = ref({
    name: null as string | null,
    powered: null as boolean | null,
});
const promise = ref<ReturnType<typeof api.devices.getDevices>>(null);
const searchTimer = ref<ReturnType<typeof setTimeout>>(null);

function load() {
    let thisProm: any;
    async function fetchFromAPI() {
        try {
            const f = filters.value;
            const response = await api.devices.getDevices({
                name: f.name,
                powered: f.powered
            });
            if (thisProm === promise.value) {
                devices.value = response.devices;
            }
        } catch (e) {
            // todo
        } finally {
            if (thisProm === promise.value) {
                promise.value = null;
            }
        }
    }
    thisProm = fetchFromAPI()
    promise.value = thisProm;
}

watch(filters, () => {
    clearTimeout(searchTimer.value);
    searchTimer.value = setTimeout(load, 150);
}, { deep: true });
load(); // Initial load

</script>
<template>
    <div class="root">
        <header class="header">
            <div class="-layout container">
                <div class="-text">Technologie</div>
                <div class="-tabs">
                    <RouterLink class="-tab" to="/tech/devices">Appareils</RouterLink>
                    <RouterLink class="-tab" to="/tech/rooms">Salles</RouterLink>
                    <RouterLink class="-tab" to="/tech/types">Modèles</RouterLink>
                </div>
            </div>
        </header>
        <RouterView class="overflow-auto" />
    </div>
</template>
<style lang="css" scoped>
.root {
    display: flex;
    flex-direction: column;
    height: 100%;
}

.header {
    background: linear-gradient(-75deg, rgb(16, 110, 101), rgb(4, 91, 172));
    box-shadow: 0px 2px 16px rgb(4, 91, 172);
    color: white;
    z-index: 1;

    padding: 1rem 4rem;
    margin: 0 12px;
    border-radius: 12px;

    & .-layout {
        display: flex;
        flex-direction: row;
        gap: 32px;
    }

    align-items: center;

    & .-text {
        font-size: 1.5rem;
        font-weight: bold;
    }

    & .-tabs {
        max-width: 1600px;
        width: 100%;
        margin-left: auto;
        display: flex;

        gap: 16px;
    }

    & .-tab {
        color: white;

        --tcol: rgba(255, 255, 255, 10%);
        border: 1px solid var(--tcol);

        text-decoration: none;
        border-radius: 8px;
        padding: 5px 8px;

        &.router-link-active {
            font-weight: bold;

            background: var(--tcol);

            /* color: black; */
            /* background: rgb(195, 238, 234); */
        }
    }
}

@media (max-width: 768px) {
    .header {
        padding: 1rem;
        text-align: center;
        padding-left: 0;
        transform: translateY(12px);
        padding-bottom: calc(1rem + 12px);
    }
    .header .-text {
        display: none;
    }
    .header .-layout {
        flex-direction: column;
        gap: 16px;
    }
    .header .-tabs {
        justify-content: center;
    }
    .root {
        flex-direction: column-reverse;
    }
}
</style>