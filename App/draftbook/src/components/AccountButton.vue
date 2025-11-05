<script setup lang="ts">
import {IonButton, IonIcon} from "@ionic/vue";
import {addIcons} from 'ionicons';
import {person} from 'ionicons/icons';

addIcons({person});

function viewAccount() {
  console.log("todo implement");
}
</script>

<template>


  <ion-button @click="viewAccount" fill="solid" shape="round">
    <ion-icon name="person" aria-label="Access account page"></ion-icon>
  </ion-button>
</template>

<style scoped>

ion-button {
  border-radius: 50%;
}

</style>