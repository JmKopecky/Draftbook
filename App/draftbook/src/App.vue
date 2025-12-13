<template>
  <ion-app>
    <ion-router-outlet />
  </ion-app>
</template>

<script setup lang="ts">
import {IonApp, IonRouterOutlet} from '@ionic/vue';
import { App as CapacitorApp } from '@capacitor/app';
import {Browser} from "@capacitor/browser";
import {useAuth0} from "@auth0/auth0-vue";
import {useRouter} from "vue-router";

const { handleRedirectCallback } = useAuth0();

const router = useRouter();

let notRegistered = true;
if (notRegistered) {
  notRegistered = false;

  CapacitorApp.addListener('appUrlOpen', async ({ url }) => {
    // Only handle URLs that use your custom scheme
    if (!url.startsWith('dev.jkopecky.draftbook://')) return;

    // Process the Auth0 callback
    const result = await handleRedirectCallback(url);

    // Close the browser tab
    await Browser.close();

    // Navigate to the appState target, or fallback to home
    const target = result?.appState?.target ?? '/';
    router.replace(target);
  });
}

</script>
