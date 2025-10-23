<script setup lang="ts">

import {IonIcon} from "@ionic/vue";
import {addIcons} from 'ionicons';
import {add} from 'ionicons/icons';

addIcons({add});

</script>

<template>
  <ion-fab-button>
    <ion-icon name="add" color="tertiary"></ion-icon>
  </ion-fab-button>
</template>

<style scoped>

</style>