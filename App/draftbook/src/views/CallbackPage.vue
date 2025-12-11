<script setup lang="ts">

import {IonContent, IonPage, IonSpinner} from "@ionic/vue";
</script>

<template>
  <ion-page>
    <ion-content id="spinner-container">
      <ion-spinner name="circular" color="primary"></ion-spinner>
    </ion-content>
  </ion-page>
</template>

<style scoped>
ion-spinner {
  width: 100px;
  height: 100px;
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
</style>