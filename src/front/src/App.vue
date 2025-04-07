<script setup>
import { RouterView, useRouter } from 'vue-router'
import { useAuthStore } from "@/stores/auth"
import { storeToRefs } from "pinia";
import { ref, watch } from "vue";
import { Toast, useToast } from 'primevue';
import api, { config as apiConfig } from './api';
import { usePrefsStore } from './stores/prefs';

const router = useRouter()
const auth = useAuthStore()
const prefs = usePrefsStore()
const toast = useToast()

// Begin loading the logged-in user in the background.
auth.fetchUser()

// Use storeToRefs to transform the store variables into "reactive" variables, so we can do stuff when that changes.
const { isLoggedIn } = storeToRefs(auth)

// When we, for some reason, get disconnected, and are in the "app" section; redirect to the home page.
// And, when we're connected and we're on the tour page (which is unusual), redirect to the dashboard.
watch(isLoggedIn, () => {
    if (isLoggedIn.value === false
        && router.currentRoute.value.matched.some(x => x.name === "app")) {
        router.push({ name: "home" })
    } else if (isLoggedIn.value === true
        && router.currentRoute.value.matched.some(x => x.name === "tour" || x.name === "login")) {
        router.push({ name: "dashboard" })
    }
})

const timeout = ref(null);
apiConfig.middleware.push({
    async post(ctx) {
        if (ctx.response.status === 401) {
            auth.setUser(null)
        } else if (ctx.response.headers.get("Domotique-Refresh-Points") === "true") {
            // Schedule a user reload when we know that our points will probably change.
            if (timeout.value) {
                clearTimeout(timeout.value)
            }
            timeout.value = setTimeout(() => auth.fetchUser(), 1000)
        }
        return ctx.response
    }
})
</script>

<template>
    <RouterView />
    <!-- Empty dialog is there cause styles wouldn't load properly else(???) -->
    <Dialog />
    <!-- Toasts! -->
    <Toast position="bottom-center" />
</template>

<style scoped></style>
