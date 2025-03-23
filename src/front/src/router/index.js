import {createRouter, createWebHistory} from 'vue-router'
import TourTemplate from "@/views/tour/TourTemplate.vue";
import AppTemplate from "@/views/app/AppTemplate.vue";
import TourHome from "@/views/tour/TourHome.vue";
import DashboardView from "@/views/app/DashboardView.vue";
import {useAuthStore} from "@/stores/auth.js";
import EmailConfirmView from "@/views/app/EmailConfirmView.vue";
import ProfileView from "@/views/app/ProfileView.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        // --- FREE TOUR ROUTES ---
        {
            path: '/',
            name: "tour",
            component: TourTemplate, // <-- Will host all our sub-routes in <RouterView />
            children: [
                {
                    path: '/',
                    name: 'home',
                    component: TourHome,
                }
            ],

            beforeEnter: async (to, from) => {
                // We don't want to show the tour to users who are already logged in.
                const auth = useAuthStore();
                if (auth.isLoggedIn) {
                    return "/dashboard";
                }
            }
        },
        // --- LOGGED-IN ROUTES ---
        {
            path: '/',
            name: "app",
            component: AppTemplate, // <-- Will host all our sub-routes in <RouterView />
            children: [
                {
                    path: '/dashboard',
                    name: 'dashboard',
                    component: DashboardView,
                },
                {
                    path: '/email-confirm',
                    name: 'email-confirm',
                    component: EmailConfirmView,

                    beforeEnter: async (to, from) => {
                        const auth = useAuthStore();

                        // If our email is already confirmed, then go back to the dashboard.
                        if (auth.user == null || auth.user.secret.emailConfirmed) {
                            return "/dashboard";
                        }
                    }
                },
                {
                    path: "/profile",
                    name: "profile",
                    component: ProfileView
                }
            ],
            beforeEnter: async (to, from) => {
                const auth = useAuthStore();

                // Check that the user is logged in before accessing the dashboard
                if (!auth.isLoggedIn) {
                    // The user is not logged in! Double check for sure...
                    const err = await auth.fetchUser()
                    if (err != null) {
                        // Yup. Not authenticated. Redirect to '/'
                        return "/";
                    } else {
                        // Now that we have the user, we must make sure that they have confirmed their email.
                        // If it's not the case, redirect them to the email confirmation page.
                        if (!auth.user.secret.emailConfirmed && to.name !== "email-confirm") {
                            return "/email-confirm";
                        }

                        // Else, we're all good! Continue!
                    }
                }
            }
        },
    ],
})

// When the user is probably logged in (meaning they logged in to the website, then closed the tab, then opened it again)
// redirect them to the dashboard instead of the tour page.
let firstPage = true;
router.beforeEach((to, from) => {
    if (firstPage) {
        const auth = useAuthStore();

        // Is the user:
        // - probably logged in, and
        // - trying to access a page in the "tour" category?
        if (auth.isProbablyLoggedIn && to.matched.some(x => x.name === "tour")) {
            // Then redirect them to the dashboard instead.
            // Note that this will also trigger app's beforeEnter guard, so even if the user hasn't confirmed their email
            // it will redirect them to the email confirmation page.
            return "/dashboard";
        }

        // TODO: Handle ?confirmEmail=ok and ?confirmEmail=err

        firstPage = false;
    }
})

export default router
