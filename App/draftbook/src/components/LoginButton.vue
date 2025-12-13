<script setup lang="ts">
import {IonButton} from "@ionic/vue";
import { Browser } from '@capacitor/browser';
import {useAuth0} from '@auth0/auth0-vue';

const { loginWithRedirect } = useAuth0();

function login() {
  loginWithRedirect({
    appState: {
      target: "/dashboard"
    },
    openUrl: async (url) => {
      await Browser.open({
        url,
        windowName: "_self"
      });
    }
  });
}
</script>


<template>
  <ion-button @click="login">Log In</ion-button>
</template>


<style scoped>

</style>