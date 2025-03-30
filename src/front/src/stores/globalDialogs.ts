import {defineStore} from "pinia";

export const useGlobalDialogsStore = defineStore('levelInfo', {
    state() {
        return {
            levelTooLow: false
        }
    },
    actions: {
        showLevelTooLow() {
            this.levelTooLow = true;
        },
        hideLevelTooLow() {
            this.levelTooLow = false;
        }
    }
});