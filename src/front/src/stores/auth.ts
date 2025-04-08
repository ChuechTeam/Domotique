import { defineStore } from "pinia";
import { computed, ref } from "vue";
import api, {
    ErrorResponse,
    findErrDataOrThrow,
    findResponseOrThrow,
    LoginInput,
    CompleteUser,
    RegisterInput,
    PatchProfileInput,
    UserProfile
} from "@/api";
import { ToastServiceMethods, useToast } from "primevue";

/**
 * The store for user authentication: contains the logged-in user, and allows for many operations
 * to be done with it. Stuff like changing email, password, etc.
 */
export const useAuthStore = defineStore('auth', () => {
    // Dependencies
    const toast = useToast();

    // State
    const user = ref<CompleteUser | null>(null);
    const loadingPromise = ref<null | Promise<ErrorResponse | void>>(null);

    // Getters as computed properties
    const isLoggedIn = computed(() => user.value != null);
    const isProbablyLoggedIn = computed(() => user.value != null || localStorage.getItem("wasLoggedIn") != null);
    const userId = computed(() => user.value?.profile?.id ?? null);
    const level = computed(() => user.value?.profile?.level ?? "BEGINNER");
    const role = computed(() => user.value?.profile?.role ?? "RESIDENT");
    const canAdminister = computed(() => user.value?.profile?.level === "EXPERT");
    const canManage = computed(() => user.value?.profile?.level === "ADVANCED" || user.value?.profile?.level === "EXPERT");

    // Actions as functions
    /**
     * Set the user to the given user. Necessary when login/register/logout; no so much for other actions
     * @param newUser the user to set
     * @param toast optional toast service to show notifications
     */
    function setUser(newUser: CompleteUser | null) {
        if (user.value != null && newUser != null && user.value.profile.points != newUser.profile.points && toast != null) {
            const diff = newUser.profile.points - user.value.profile.points;
            if (diff > 0) {
                toast.add({
                    severity: "info",
                    summary: "Points gagnés !",
                    detail: `Vous venez de remporter ${diff} points !`,
                    life: 5000
                });
            }
        }

        user.value = newUser;

        // Update the local storage to remember if the user was logged in
        if (newUser === null) {
            localStorage.removeItem("wasLoggedIn");
        } else {
            localStorage.setItem("wasLoggedIn", "true");
        }
    }

    /**
     * Fetch the authenticated user from the API. If we're already loading the user, return the pending promise
     * @returns an error if something went wrong
     */
    function fetchUser(): Promise<ErrorResponse | void> {
        // Don't fetch the user if we're already loading; instead, give the promise that's currently
        // loading the user
        if (loadingPromise.value != null) {
            return loadingPromise.value;
        }

        // Put our loading logic in an inner function so we can reuse that task later
        const task = async () => {
            try {
                // Query the authenticated user from the API
                setUser(await api.users.me());
            } catch (e) {
                // We couldn't get the user! We're likely NOT signed in.
                const resp = findResponseOrThrow(e);

                // We're unauthorized; we're not signed in so remove the user
                if (resp.status === 401) {
                    setUser(null);
                }

                // Return the error.
                return resp.errData;
            } finally {
                // Clear this promise
                loadingPromise.value = null;
            }
        };

        // Run it in the background, and wait for it
        loadingPromise.value = task();
        return loadingPromise.value;
    }

    /**
     * Log in the user with the given credentials.
     * @param credentials the credentials (email, password)
     * @returns an error if something went wrong
     */
    async function login(credentials: LoginInput): Promise<ErrorResponse | void> {
        try {
            setUser(await api.users.login({
                loginInput: credentials
            }));
        } catch (e) {
            return findErrDataOrThrow(e);
        }
    }

    /**
     * Log out the user from the app.
     * @returns an error if something went wrong
     */
    async function logout(): Promise<ErrorResponse | void> {
        try {
            await api.users.logout();
            setUser(null);
        } catch (e) {
            return findErrDataOrThrow(e);
        }
    }

    /**
     * Register a new user with the given registration information.
     * @param reg the registration information
     */
    async function register(reg: RegisterInput): Promise<ErrorResponse | undefined> {
        try {
            setUser(await api.users.register({
                registerInput: reg
            }));
        } catch (e) {
            return findErrDataOrThrow(e);
        }
    }

    /**
     * Update the profile of the currently logged in user.
     * @param input the profile data to change
     */
    async function updateProfile(input: PatchProfileInput): Promise<ErrorResponse | UserProfile> {
        if (!isLoggedIn.value) {
            throw new Error("Cannot update profile while not being logged in!");
        }

        try {
            const updatedProfile = await api.users.updateProfile({
                userId: "me",
                patchProfileInput: input
            });
            if (user.value) {
                user.value.profile = updatedProfile;
            }
            return updatedProfile;
        } catch (e) {
            return findErrDataOrThrow(e);
        }
    }

    /**
     * Update the password of the currently logged-in user.
     * @param oldPassword the old password
     * @param newPassword the new password (must be good!)
     */
    async function updatePassword(oldPassword: string, newPassword: string): Promise<ErrorResponse | undefined> {
        if (!isLoggedIn.value) {
            throw new Error("Cannot update password while not being logged in!");
        }

        try {
            await api.users.changePassword({
                userId: "me",
                changePasswordInput: {
                    oldPassword,
                    newPassword
                }
            });
        } catch (e) {
            return findErrDataOrThrow(e);
        }
    }

    /**
     * Delete the current user account
     * @param password the user's password for confirmation
     */
    async function deleteUser(password: string): Promise<ErrorResponse | undefined> {
        if (!isLoggedIn.value) {
            throw new Error("Cannot delete account while not being logged in!");
        }

        try {
            await api.users.deleteUser({
                userId: "me",
                deleteUserInput: {
                    password
                }
            });
            setUser(null);
        } catch (e) {
            return findErrDataOrThrow(e);
        }
    }

    return {
        // State
        user,
        loadingPromise,
        
        // Getters
        isLoggedIn,
        isProbablyLoggedIn,
        userId,
        level,
        role,
        canAdminister,
        canManage,
        
        // Actions
        setUser,
        fetchUser,
        login,
        logout,
        register,
        updateProfile,
        updatePassword,
        deleteUser
    };
});

