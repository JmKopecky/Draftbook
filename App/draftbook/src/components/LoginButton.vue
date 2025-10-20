<script setup lang="ts">
import { useAuth0 } from '@auth0/auth0-vue';

const { loginWithRedirect } = useAuth0();

function login() {
  loginWithRedirect();
}
</script>


<template>
  <ion-button @click="login">Log In</ion-button>
</template>


<style scoped>

</style>