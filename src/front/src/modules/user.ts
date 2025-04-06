import api, { ErrorResponse, findErrDataOrThrow, PatchProfileInput, UserProfile, ValidationErrorResponse } from "@/api";
import { useAuthStore } from "@/stores/auth";

export function useUserModule() {
    const auth = useAuthStore();

    return {
        /**
         * Updates the profile of the given user.
         * * @param userId the ID of the user to update
         * * @param profile the new profile data
         * * @returns the updated profile of the user, or an error response if the update failed
         */
        async updateProfile(userId: number, profile: PatchProfileInput): Promise<ValidationErrorResponse<PatchProfileInput> | UserProfile> {
            if (userId == auth.userId) {
                // We're updating the profile of the currently logged-in user, so use the store instead.
                return await auth.updateProfile(profile);
            } else {
                try {
                    return await api.users.updateProfile({
                        userId: userId.toString(),
                        patchProfileInput: profile
                    });
                } catch (e) {
                    return findErrDataOrThrow(e);
                }
            }
        },

        async updatePassword(userId: number, oldPassword: string | undefined, newPassword: string): Promise<ErrorResponse | undefined> {
            try {
                await api.users.changePassword({
                    userId: userId.toString(),
                    changePasswordInput: {
                        oldPassword,
                        newPassword
                    }
                })
            } catch (e) {
                return findErrDataOrThrow(e);
            }
        },

        async deleteAccount(userId: number, password?: string): Promise<ErrorResponse | undefined> {
            if (userId == auth.userId) {
                if (password == null) {
                    throw new Error("Password is empty!");
                }
                return await auth.deleteUser(password)
            } else {
                try {
                    await api.users.deleteUser({
                        userId: userId.toString(),
                        deleteUserInput: {
                            password
                        }
                    });
                } catch (e) {
                    return findErrDataOrThrow(e);
                }
            }
        }
    };
}