import { useAuthStore } from "./stores/auth";
import { useGlobalDialogsStore } from "./stores/globalDialogs";

// A helpful tool to show dialogs when a user tries to access a page they don't have permission to
export function useGuards() {
    const authStore = useAuthStore();
    const dialogs = useGlobalDialogsStore();

    return {
        /**
         * Show a dialog box when the user doesn't have MANAGEMENT (module gestion) rights.
         * @returns true when the user has the rights; false when they don't and a dialog box has been opened
         */
        mustManage(): boolean {
            if (authStore.canManage) {
                return true;
            } else {
                dialogs.showLevelTooLow();
                return false;
            }
        },
        /**
         * Show a dialog box when the user doesn't have ADMINISTRATION (module administration) rights.
         * @returns true when the user has the rights; false when they don't and a dialog box has been opened
         */
        mustHaveAdminRights(): boolean {
            if (authStore.canAdminister) {
                return true;
            } else {
                dialogs.showLevelTooLow();
                return false;
            }
        }
    }
}