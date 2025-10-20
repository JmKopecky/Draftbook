<script setup lang="ts">

import {IonContent, IonHeader, IonPage, IonTitle, IonToolbar} from "@ionic/vue";
import ExploreContainer from "@/components/ExploreContainer.vue";
import LoginButton from "@/components/LoginButton.vue";
</script>

<template>
  <ion-page>
    <ion-content :fullscreen="true">
      <div id="auth-content-centerer">
        <ion-card>
          <ion-card-header>
            <ion-card-subtitle>Welcome to...</ion-card-subtitle>
            <ion-card-title>Draftbook!</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <h3>Let's get you set up.</h3>
            <login-button/>
          </ion-card-content>
        </ion-card>
      </div>
    </ion-content>
  </ion-page>
</template>

<style scoped>
#auth-content-centerer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1em;
  width: 100%;
  height: 100%;
}
ion-card {
  width: 100%;
  max-width: 400px;
  display: flex;
  flex-flow: column;
  align-items: center;
}
ion-card-content {
  display: flex;
  flex-flow: column;
  align-items: center;
}
</style>