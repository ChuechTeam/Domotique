<script lang="ts" setup>
import { RouteLocationAsPathGeneric, RouteLocationAsRelativeGeneric } from 'vue-router';

type NavItem = {
    name: string;
    to: string | RouteLocationAsPathGeneric | RouteLocationAsRelativeGeneric;
}

const { title, items } = defineProps<{
    title: string;
    items: NavItem[];
}>();

</script>

<template>
    <nav class="subnav">
        <div class="-layout container">
            <div class="-text">{{ title }}</div>
            <div class="-tabs">
                <RouterLink class="-tab" v-for="i in items" :to="i.to">{{ i.name }}</RouterLink>
            </div>
        </div>
    </nav>
</template>

<style>
@property --subnav-bg-left {
    syntax: '<color>';
    inherits: false;
    initial-value: rgb(4, 91, 172);
}
@property --subnav-bg-right {
    syntax: '<color>';
    inherits: false;
    initial-value: rgb(16, 110, 101);
}
@property --subnav-shadow-col {
    syntax: '<color>';
    inherits: false;
    initial-value: rgb(4, 91, 172);
}
</style>

<style scoped>
.subnav {
    background: linear-gradient(-75deg, var(--subnav-bg-right), var(--subnav-bg-left));

    box-shadow: 0px 2px 16px var(--subnav-shadow-col);
    color: white;
    z-index: 1;

    --nav-tr-duration: 0.2s;
    transition: --subnav-bg-left var(--nav-tr-duration) ease, --subnav-bg-right var(--nav-tr-duration) ease, --subnav-shadow-col var(--nav-tr-duration) ease;

    padding: 1rem 4rem;
    align-items: center;

    & .-layout {
        display: flex;
        flex-direction: row;
        gap: 32px;
    }

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

        min-width: 128px;
        text-align: center;

        &.router-link-active {
            font-weight: bold;

            background: var(--tcol);

            /* color: black; */
            /* background: rgb(195, 238, 234); */
        }
    }
}

@media (max-width: 1024px) {
    .subnav {
        padding: 0.75rem 0;
        text-align: center;
    }
    .subnav .-text {
        display: none;
    }
    .subnav .-layout {
        flex-direction: column;
        gap: 16px;
    }
    .subnav .-tabs {
        justify-content: center;
    }
    .subnav .-tab {
        flex-grow: 1;
        flex-basis: 0;
        min-width: unset;
    }
}
</style>