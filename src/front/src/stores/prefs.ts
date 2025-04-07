import {defineStore} from "pinia";
import { ref, watch } from "vue";

export const usePrefsStore = defineStore('prefs', () => {
    const reducedMotion = ref(localStorage.getItem("reducedMotion") == "true");

    watch(reducedMotion, (newValue) => {
        localStorage.setItem("reducedMotion", newValue.toString());
        document.querySelector("html").classList.toggle("reduced-motion", newValue);
    }, { immediate: true });

    document.querySelector("html").classList.toggle("reduced-motion", reducedMotion.value);

    return { reducedMotion }
});