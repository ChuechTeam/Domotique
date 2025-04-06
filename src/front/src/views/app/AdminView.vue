<script lang="ts" setup>
import SubNavbar from '@/components/SubNavbar.vue';
import { onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const links = [
    { name: 'Actions', to: { name: 'admin-actions' } },
    { name: 'Connexions', to: { name: 'admin-logins' } },
    { name: 'Codes', to: { name: 'admin-codes' } },
];
const dir = ref(1);
const router = useRouter();

function inArea(route: any, area: string) {
    return route.matched.some(x => x.name === area);
}
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
        <SubNavbar title="Administration" :items="links" class="nav" />
        <div style="position: relative; flex-grow: 1;">
            <RouterView v-slot="{ Component }">
                <Transition name="slide">
                    <component :is="Component" class="view overflow-auto flex-grow-1 h-100" />
                </Transition>
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

.nav {
    --subnav-bg-left: rgb(150, 8, 8);
    --subnav-bg-right: rgb(145, 60, 20);
    --subnav-shadow-col: rgb(128, 47, 10);
}

.view {
    scrollbar-gutter: stable both-edges;
    scrollbar-width: thin;
    flex-grow: 1;

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