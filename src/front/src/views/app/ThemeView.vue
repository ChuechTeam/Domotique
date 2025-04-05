<script lang="ts" setup>
import FullscreenSpinner from '@/components/FullscreenSpinner.vue';
import SubNavbar from '@/components/SubNavbar.vue';
import { computed, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const links = [
    { name: 'Santé', to: { name: 'health-theme' } },
    { name: 'Énergie', to: { name: 'energy-theme' } },
    { name: 'Sport', to: { name: 'sport-theme' } }
];

const dir = ref(1);

function inArea(route: any, area: string) {
    return route.matched.some(x => x.name === area);
}
const router = useRouter();

const routeClass = computed(() => 'route-' + (router.currentRoute.value.name as string));

// Set the right direction (left or right) based on the route change
const removeListener = router.beforeResolve((to, from) => {
    const fromIdx = links.findIndex(l => inArea(from, l.to.name));
    const toIdx = links.findIndex(l => inArea(to, l.to.name));
    if (fromIdx !== -1 && toIdx !== -1) {
        if (fromIdx < toIdx) {
            // link on the right
            dir.value = 1;
        } else {
            // link on the left
            dir.value = -1;
        }
    }
})
onUnmounted(removeListener);
</script>
<template>
    <div class="root no-padding no-overflow">
        <SubNavbar title="Thèmes" :items="links" :class="['nav', routeClass]" />
        <div style="position: relative; flex-grow: 1;">
            <RouterView v-slot="{ Component }">
                <Suspense>
                    <template #default>
                        <Transition name="slide">
                            <component :is="Component" class="view overflow-auto flex-grow-1 h-100" />
                        </Transition>
                    </template>
                    <template #fallback>
                        <FullscreenSpinner />
                    </template>
                </Suspense>
            </RouterView>
        </div>
    </div>
</template>
<style lang="css" scoped>
.root {
    display: flex;
    flex-direction: column;
    height: 100%;
    width: 100%;
    overflow: none;

    --anim-duration: 0.33s;
    --anim-direction: v-bind(dir);
}

.nav.route-health-theme {
    --subnav-bg-left: rgb(65, 145, 89);
    --subnav-bg-right: rgb(8, 150, 80);
    --subnav-shadow-col: rgb(52, 136, 77);
}

.nav.route-sport-theme {
    --subnav-bg-left: rgb(38, 22, 126);
    --subnav-bg-right: rgb(101, 57, 172);
    --subnav-shadow-col: rgb(76, 30, 150);
}

.nav.route-energy-theme {
    --subnav-bg-left: rgb(172, 103, 14);
    --subnav-bg-right: rgb(189, 164, 27);
    --subnav-shadow-col: rgb(172, 103, 14);
}

.view {
    scrollbar-gutter: stable both-edges;
    scrollbar-width: thin;

    position: absolute;
    left: 0;
    right: 0;
}

@media (max-width: 1024px) {
    .root {
        flex-direction: column-reverse;
    }
}
</style>