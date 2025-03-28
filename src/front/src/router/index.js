import { createRouter, createWebHistory } from 'vue-router'
import TourTemplate from "@/views/tour/TourTemplate.vue";
import AppTemplate from "@/views/app/AppTemplate.vue";
import TourHome from "@/views/tour/TourHome.vue";
import DashboardView from "@/views/app/DashboardView.vue";
import { useAuthStore } from "@/stores/auth.js";
import EmailConfirmView from "@/views/app/EmailConfirmView.vue";
import ProfileView from "@/views/app/ProfileView.vue";
import HomeView from '@/views/tour/HomeView.vue';

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
                    component: HomeView,
                }
            ],
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

                    async beforeEnter(to, from) {
                        const auth = useAuthStore();

                        // If our email is already confirmed, then go back to the dashboard.
                        if (auth.user == null || auth.user.secret.emailConfirmed) {
                            return "/dashboard";
                        }
                    }
                },
                {
                    path: "/profile/:userId(\\d+)",
                    name: "profile",
                    component: ProfileView,
                    props: true
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
            if (err != null) {
                // Yup. Not authenticated. Redirect to '/'
                return "/";
            }
        }

        // Now that we have the user, we must make sure that they have confirmed their email.
        // If it's not the case, redirect them to the email confirmation page.
        if (!auth.user.secret.emailConfirmed && to.name !== "email-confirm") {
            return "/email-confirm";
        }

        // Else, we're all good! Continue!
    } else if (inArea(to, "tour") && auth.isLoggedIn) {
        // If the user is logged in, redirect them to the dashboard instead.
        return "/dashboard";
    }
})

export default router
