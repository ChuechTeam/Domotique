import {defineStore} from "pinia";
import api, {Level} from "@/api";

// Map: Level ---> number of points required to reach that level
export type PointsReq = {[key in Level]: number};

const useLevelInfoStore = defineStore('levelInfo', {
    state() {
        return {
            info: null as PointsReq | null,
            promise: null as Promise<PointsReq> | null
        }
    },
    actions: {
        /**
         * Fetch the level info from the API
         * @returns an error if something went wrong
         */
        async fetchLevelInfo(): Promise<PointsReq> {
            if (this.info) {
                return this.info;
            } else if (this.promise) {
                return this.promise;
            } else {
                const task = async () => {
                    try {
                        const pr: PointsReq = (await api.users.getLevelInfo()).pointsRequired as PointsReq;
                        this.info = pr;
                        return pr;
                    } finally {
                        this.promise = null;
                    }
                }

                this.promise = task();
                return this.promise;
            }
        },
    }
});