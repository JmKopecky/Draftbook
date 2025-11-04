<script setup lang="ts">
import {IonButton} from "@ionic/vue";
import {useAuth0} from '@auth0/auth0-vue';

const { loginWithRedirect } = useAuth0();

function login() {
  loginWithRedirect({
    appState: {
      target: "/dashboard"
    },
  });
}
</script>


<template>
  <ion-button @click="login">Log In</ion-button>
</template>


<style scoped>

</style>