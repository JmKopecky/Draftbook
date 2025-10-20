import { createRouter, createWebHistory } from '@ionic/vue-router';
import {NavigationGuardNext, RouteLocationNormalized, RouteRecordRaw} from 'vue-router';
import TabsPage from '../views/TabsPage.vue'
import AuthPage from '../views/AuthPage.vue'
import DashboardPage from "@/views/DashboardPage.vue";
import App from '../App.vue';
import auth0 from "@/Auth0";

const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        redirect: '/auth'
    },
    {
        path: '/auth',
        component: AuthPage,
    },
    {
        path: '/dashboard',
        component: DashboardPage,
        meta: { forceAuth: true }
    }

]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

//before each route to a different page, check auth status
router.beforeEach(async (to: RouteLocationNormalized) => {
    //if this route or any children require authentication
    if (to.matched.some((r) => r.meta.forceAuth)) {
        //if not authenticated, redirect to the authentication page.
        const authenticated = auth0.isAuthenticated;
        if (!authenticated.value) {
            return '/auth';
        }
    }
});

export default router
