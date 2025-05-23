import AuthView from '@/views/AuthView.vue'
import { createRouter, createWebHistory } from 'vue-router'
import TourTemplate from "@/views/tour/TourTemplate.vue";
import AppTemplate from "@/views/app/AppTemplate.vue";
import DashboardView from "@/views/app/DashboardView.vue";
import { useAuthStore } from "@/stores/auth.js";
import EmailConfirmView from "@/views/app/EmailConfirmView.vue";
import ProfileDetailView from "@/views/app/ProfileDetailView.vue";
import RegisterView from '@/views/RegisterView.vue';
import HomeView from '@/views/tour/HomeView.vue';
import ProfileEditModal from "@/views/app/ProfileEditModal.vue";
import CredentialsEditModal from "@/views/app/CredentialsEditModal.vue";
import TechView from '@/views/app/TechView.vue';
import DevicesView from '@/views/app/DevicesView.vue';
import DeviceDetailView from '@/views/app/DeviceDetailView.vue';
import NewDeviceView from '@/views/app/NewDeviceView.vue';
import RoomsView from '@/views/app/RoomsView.vue';
import DeviceTypesView from '@/views/app/DeviceTypesView.vue';
import { useGuards } from '@/guards';
import ProfileDeleteModal from '@/views/app/ProfileDeleteModal.vue';
import ProfilesView from '@/views/app/ProfilesView.vue';
import AdminView from '@/views/app/AdminView.vue';
import AdminActionsView from '@/views/app/AdminActionsView.vue';
import AdminLoginsView from '@/views/app/AdminLoginsView.vue';
import ThemeView from '@/views/app/ThemeView.vue';
import HealthThemeView from '@/views/app/HealthThemeView.vue';
import EnergyThemeView from '@/views/app/EnergyThemeView.vue';
import SportThemeView from '@/views/app/SportThemeView.vue';
import AdminCodesView from '@/views/app/AdminCodesView.vue';

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/auth',
            name: 'auth',
            component: AuthView
          },
        // --- FREE TOUR ROUTES ---
        {
            path: '/',
            name: "tour",
            component: TourTemplate, // <-- Will host all our sub-routes in <RouterView />
            children: [
                {
                    path: '',
                    name: 'home',
                    component: HomeView,
                },
                {
                    path: '/register',
                    name: 'register',
                    component: RegisterView
                },
                // --- LOGIN ROUTES ---
                {
                    path: '/login',
                    name: 'login',
                    component: () => import('@/views/AuthView.vue'),
                },  
            ], 
        },
       

        // --- LOGGED-IN ROUTES ---
        {
            path: '/',
            name: "app",
            component: AppTemplate, // <-- Will host all our sub-routes in <RouterView />
            children: [
                {
                    path: 'dashboard',
                    name: 'dashboard',
                    component: DashboardView,
                },
                {
                    path: '/email-confirm',
                    name: 'email-confirm',
                    component: EmailConfirmView,

                    async beforeEnter(to, from) {
                        const auth = useAuthStore();

                        // If our email is already confirmed, then go back to the dashboard.
                        if (auth.user == null || auth.user.secret.emailConfirmed) {
                            return "/dashboard";
                        }
                    }
                },
                {
                    path: "/profiles",
                    name: "profiles",
                    component: ProfilesView,
                    children: [
                        {
                            path: ":userId(\\d+)",
                            name: "profile",
                            component: ProfileDetailView,
                            props: true,
                            children: [
                                {
                                    path: "edit",
                                    name: "profile-edit",
                                    component: ProfileEditModal,
                                    props: true,

                                    // Prevent users from accessing the edit page for other users if they are not admins.
                                    beforeEnter(to, from) {
                                        const guards = useGuards();
                                        const auth = useAuthStore();

                                        console.log(to, auth.userId);

                                        if (auth.userId.toString() === to.params.userId) {
                                            return;
                                        } else if (!guards.mustHaveAdminRights()) {
                                            return from?.fullPath ?? "/dashboard";
                                        }
                                    }
                                },
                                {
                                    path: "creds",
                                    name: "profile-creds",
                                    component: CredentialsEditModal,
                                    props: true,

                                    // Prevent users from accessing the password edit page for other users if they are not admins.
                                    beforeEnter(to, from) {
                                        const guards = useGuards();
                                        const auth = useAuthStore();

                                        if (auth.userId.toString() === to.params.userId) {
                                            return;
                                        } else if (!guards.mustHaveAdminRights()) {
                                            return from?.fullPath ?? "/dashboard";
                                        }
                                    }
                                },
                                {
                                    path: "delete",
                                    name: "profile-delete",
                                    component: ProfileDeleteModal,
                                    props: true,

                                    // Prevent users from accessing the profile delete page for other users if they are not admins.
                                    beforeEnter(to, from) {
                                        const guards = useGuards();
                                        const auth = useAuthStore();

                                        if (auth.userId.toString() === to.params.userId) {
                                            return;
                                        } else if (!guards.mustHaveAdminRights()) {
                                            return from?.fullPath ?? "/dashboard";
                                        }
                                    }
                                }
                            ],
                            meta: {
                                generateKey() {
                                    return router.currentRoute.value.params.userId.toString();
                                }
                            }
                        }
                    ],
                    beforeEnter(to, from) {
                        const auth = useAuthStore();

                        // Prevent users without authorization from querying all users.
                        if (to.query?.allUsers == "true" && auth.canAdminister) {
                            return from?.fullPath ?? "/dashboard";
                        }
                    }
                },
                {
                    path: "/tech",
                    name: "tech",
                    component: TechView,
                    props: true,
                    children: [
                        {
                            path: "devices",
                            name: "devices",
                            component: DevicesView,
                            children: [{
                                path: ":deviceId(\\d+)",
                                name: "device-detail",
                                component: DeviceDetailView,
                                props: true,
                            }, {
                                path: "new",
                                name: "device-new",
                                component: NewDeviceView,

                                // Prevent users from creating new devices if they are not admins.
                                beforeEnter(to, from) {
                                    const guards = useGuards();
                                    if (!guards.mustManage()) {
                                        return from?.fullPath ?? "/dashboard";
                                    }
                                }
                            }]
                        },
                        {
                            path: "rooms",
                            name: "rooms",
                            component: RoomsView
                        },
                        {
                            path: "types",
                            name: "types",
                            component: DeviceTypesView
                        }
                    ]
                },
                {
                    path: "/admin",
                    name: "admin",
                    component: AdminView,
                    props: true,
                    children: [
                        {
                            path: "actions",
                            name: "admin-actions",
                            component: AdminActionsView
                        },
                        {
                            path: "logins",
                            name: "admin-logins",
                            component: AdminLoginsView
                        },
                        {
                            path: "codes",
                            name: "admin-codes",
                            component: AdminCodesView
                        }
                    ],
                    // Prevent users from accessing admin section if they don't have admin rights
                    beforeEnter(to, from) {
                        const guards = useGuards();
                        if (!guards.mustHaveAdminRights()) {
                            return from?.fullPath ?? "/dashboard";
                        }
                    }
                },
                {
                    path: "/themes",
                    name: "themes",
                    component: ThemeView,
                    props: true,
                    children: [
                        {
                            path: "health",
                            name: "health-theme",
                            component: HealthThemeView
                        },
                        {
                            path: "energy",
                            name: "energy-theme",
                            component: EnergyThemeView
                        },
                        {
                            path: "sport",
                            name: "sport-theme",
                            component: SportThemeView
                        }
                    ]
                }
            ]
        },
    ],
})

function inArea(route, area) {
    return route.matched.some(x => x.name === area);
}

// When the user is probably logged in (meaning they logged in to the website, then closed the tab, then opened it again)
// redirect them to the dashboard instead of the tour page.
let firstPage = true;
router.beforeEach(async (to, from) => {
    const auth = useAuthStore();

    if (firstPage) {
        // Is the user:
        // - probably logged in, and
        // - trying to access a page in the "tour" category?
        if (auth.isProbablyLoggedIn && inArea(to, "tour")) {
            // Then redirect them to the dashboard instead.
            // Note that this will also trigger app's beforeEnter guard, so even if the user hasn't confirmed their email
            // it will redirect them to the email confirmation page.
            return "/dashboard";
        }

        // TODO: Handle ?confirmEmail=ok and ?confirmEmail=err

        firstPage = false;
    }

    // Check that the user is logged in before accessing the dashboard
    if (inArea(to, "app")) {
        if (!auth.isLoggedIn) {
            // The user is not logged in! Double check for sure...
            const err = await auth.fetchUser()
            if (err != null || auth.user == null) {
                // Yup. Not authenticated. Redirect to '/'
                return "/";
            }
        }

        // Now that we have the user, we must make sure that they have confirmed their email.
        // If it's not the case, redirect them to the email confirmation page.
        if (!auth.user.secret.emailConfirmed && to.name !== "email-confirm") {
            return "/email-confirm";
        }

        // Else, we're all good! Continue! Let's process some logic for other routes in the app

        if (to.name === "tech") {
            // tech isn't a real route, but a parent of devices and rooms.
            return "/tech/devices";
        } else if (to.name === "admin") {
            // Same for admin
            return "/admin/actions";
        } else if (to.name === "themes") {
            // Same for themes
            return "/themes/health";
        }
    } else if ((inArea(to, "tour") || inArea(to, "login")) && auth.isLoggedIn) {
        // If the user is logged in, redirect them to the dashboard instead.
        return "/dashboard";
    }
})

export default router
