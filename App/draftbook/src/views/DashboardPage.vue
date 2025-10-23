<script setup lang="ts">
import {IonPage} from "@ionic/vue";
import {useAuth0} from '@auth0/auth0-vue';

const { user, isAuthenticated, getAccessTokenSilently} = useAuth0();

</script>

<template>
  <ion-page>
    <!--
    <ion-header>
      <ion-toolbar>
        <ion-title>Dashboard</ion-title>
        <button>Account</button>
      </ion-toolbar>
    </ion-header>
    <ion-content>
      <p>Stuff</p>
      <ion-fab slot="fixed" vertical="bottom" horizontal="end">
        <ion-fab-button>
          <ion-icon name="add"></ion-icon>
        </ion-fab-button>
      </ion-fab>
    </ion-content>
    -->
  </ion-page>
</template>

<style scoped>

</style>