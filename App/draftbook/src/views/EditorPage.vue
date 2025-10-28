<script setup lang="ts">

import {IonPage} from "@ionic/vue";
import {useRoute} from "vue-router";
import {watch} from "vue";

const route = useRoute()
const workId = route.params.id;

watch(
    () => route.params.id,
    (newId, oldId) => {
      //when the user id changes, reload everything we need to.
    }
)
</script>

<template>
  <ion-page>
    <p>Hello, world!</p>
    <p>Work id is {{workId}}</p>
  </ion-page>
</template>

<style scoped>

</style>