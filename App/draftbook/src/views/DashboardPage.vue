<script setup lang="ts">
import {IonContent, IonHeader, IonPage, IonTitle, IonToolbar} from "@ionic/vue";
import { useAuth0 } from '@auth0/auth0-vue';
import {getAssetPath} from "ionicons/components";

const { user, isAuthenticated, getAccessTokenSilently} = useAuth0();

</script>

<template>
  <ion-page>
    <ion-content :fullscreen="true">
      <h1>Dashboard!</h1>
    </ion-content>
  </ion-page>
</template>

<style scoped>

</style>