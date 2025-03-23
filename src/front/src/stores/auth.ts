import {defineStore} from "pinia";
import api, {
    ErrorResponse,
    findErrDataOrThrow,
    findResponseOrThrow,
    LoginInput,
    CompleteUser,
    RegisterInput,
    UpdateProfileInput
} from "@/api";

/**
 * The store for user authentication: contains the logged-in user, and allows for many operations
 * to be done with it. Stuff like changing email, password, etc.
 */
export const useAuthStore = defineStore('auth', {
    state() {
        return {
            user: null as CompleteUser | null,
            loadingPromise: null as null | Promise<ErrorResponse | void>,
        }
    },
    getters: {
        /**
         * True when the user is logged in AND we have user data loaded.
         */
        isLoggedIn: s => s.user != null,
        /**
         * True when either:
         * - the user is already logged in
         * - last time the user visited the app, they were logged in
         */
        isProbablyLoggedIn: s => s.user != null || localStorage.getItem("wasLoggedIn") != null
    },
    actions: {
        /**
         * Fetch the authenticated user from the API. If we're already loading the user, return the pending promise
         * @returns an error if something went wrong
         */
        fetchUser(): Promise<ErrorResponse | void> {
            // Don't fetch the user if we're already loading; instead, give the promise that's currently
            // loading the user
            if (this.loadingPromise != null) {
                return this.loadingPromise;
            }

            // Put our loading logic in an inner function so we can reuse that task later
            const task = async () => {
                try {
                    // Query the authenticated user from the API
                    this.setUser(await api.users.me());
                } catch (e) {
                    // We couldn't get the user! We're likely NOT signed in.
                    const resp = findResponseOrThrow(e);

                    // We're unauthorized; we're not signed in so remove the user
                    if (resp.status === 401) {
                        this.setUser(null);
                    }

                    // Return the error.
                    return resp.errData;
                } finally {
                    // Clear this promise
                    this.loadingPromise = null;
                }
            }

            // Run it in the background, and wait for it
            this.loadingPromise = task();
            return this.loadingPromise;
        },

        /**
         * Log in the user with the given credentials.
         * @param credentials the credentials (email, password)
         * @returns an error if something went wrong
         */
        async login(credentials: LoginInput): Promise<ErrorResponse | void> {
            try {
                this.setUser(await api.users.login({
                    loginInput: credentials
                }));
            } catch (e) {
                return findErrDataOrThrow(e);
            }
        },

        /**
         * Log out the user from the app.
         * @returns an error if something went wrong
         */
        async logout(): Promise<ErrorResponse | void> {
            try {
                await api.users.logout();
                this.setUser(null);
            } catch (e) {
                return findErrDataOrThrow(e);
            }
        },

        /**
         * Register a new user with the given registration information.
         * @param reg the registration information
         */
        async register(reg: RegisterInput): Promise<ErrorResponse | void> {
            try {
                this.setUser(await api.users.register({
                    registerInput: reg
                }));
            } catch (e) {
                return findErrDataOrThrow(e);
            }
        },

        /**
         * Update the profile of the currently logged in user.
         * @param input the profile data to change
         */
        async updateProfile(input: UpdateProfileInput): Promise<ErrorResponse | void> {
            if (!this.isLoggedIn) {
                throw new Error("Cannot update profile while not being logged in!");
            }

            try {
                this.user.profile = await api.users.updateProfile({
                    updateProfileInput: input
                });
            } catch (e) {
                return findErrDataOrThrow(e);
            }
        },

        /**
         * Update the password of the currently logged-in user.
         * @param oldPassword the old password
         * @param newPassword the new password (must be good!)
         */
        async updatePassword(oldPassword: string, newPassword: string): Promise<ErrorResponse | void> {
            if (!this.isLoggedIn) {
                throw new Error("Cannot update password while not being logged in!");
            }

            try {
                await api.users.changePassword({
                    changePasswordInput: {
                        oldPassword,
                        newPassword
                    }
                });
            } catch (e) {
                return findErrDataOrThrow(e);
            }
        },

        /**
         * Set the user to the given user. Necessary when login/register/logout; no so much for other actions
         * @param user the user to set
         */
        setUser(user: CompleteUser | null) {
            this.user = user;

            // Update the local storage to remember if the user was logged in
            if (user === null) {
                localStorage.removeItem("wasLoggedIn");
            } else {
                localStorage.setItem("wasLoggedIn", "true");
            }
        }
    }
})

