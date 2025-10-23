<script setup lang="ts">
import {IonPage, IonIcon} from "@ionic/vue";
import {useAuth0} from '@auth0/auth0-vue';
import AccountButton from "@/components/AccountButton.vue";
import CreateWorkButton from "@/components/CreateWorkButton.vue";
import {API_URL} from "@/localConfig";

const {getAccessTokenSilently} = useAuth0();

async function reloadWorks() {
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/works/list", {
    headers: {
      Authorization: 'Bearer ' + accessToken
    }
  });
  const data = await response.json();
  console.log(data);
}

</script>

<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Dashboard</ion-title>
        <ion-buttons slot="end">
          <AccountButton></AccountButton>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>
    <ion-content>

      <button @click="reloadWorks">Test</button>

      <ion-fab slot="fixed" vertical="bottom" horizontal="end">
        <CreateWorkButton></CreateWorkButton>
      </ion-fab>
    </ion-content>
  </ion-page>
</template>

<style scoped>
</style>