import {createRouter, createWebHistory} from '@ionic/vue-router';
import {RouteRecordRaw} from 'vue-router';
import AuthPage from '../views/AuthPage.vue'
import DashboardPage from "@/views/DashboardPage.vue";
import {authGuard} from "@auth0/auth0-vue";
import CallbackPage from "@/views/CallbackPage.vue";

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
        path: '/authenticate',
        component: CallbackPage
    },
    {
        path: '/dashboard',
        component: DashboardPage,
        beforeEnter: authGuard
    }

]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

export default router
