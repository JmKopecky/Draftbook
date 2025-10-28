<script setup lang="ts">
import {IonIcon} from "@ionic/vue";
import {pencil} from "ionicons/icons";
import {addIcons} from "ionicons";
import router from "@/router";
addIcons({pencil});

const props = defineProps(['work']);
const chapters = props.work["chapterCount"];

/**
 * Redirect to the page for the specific work.
 */
function accessWork() {
  const id = props.work["id"];
  router.push('/work/' + id);
}
</script>

<template>
  <ion-card>
    <ion-card-header>
      <ion-card-title>{{props.work['title']}}</ion-card-title>
    </ion-card-header>
    <ion-card-content>
      {{chapters != 1 ? chapters + ' Chapters' : chapters + ' chapter'}}
    </ion-card-content>

    <ion-button fill="clear" @click="accessWork">
      <ion-icon name="pencil"></ion-icon>
    </ion-button>
    <ion-button fill="clear" @click="$emit('manageWork', props.work)">Manage</ion-button>
  </ion-card>
</template>

<style scoped>

</style>