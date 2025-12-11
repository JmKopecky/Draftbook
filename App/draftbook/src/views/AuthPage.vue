<script setup lang="ts">

import {IonCard, IonCardContent, IonCardHeader, IonCardSubtitle, IonCardTitle, IonContent, IonPage} from "@ionic/vue";
import LoginButton from "@/components/LoginButton.vue";
</script>

<template>
  <ion-page>
    <ion-content :fullscreen="true">
      <div id="auth-content-centerer">
        <ion-card>
          <ion-card-header>
            <ion-card-title>Welcome to Draftbook</ion-card-title>
            <ion-card-subtitle>Your new tool for getting writing done.</ion-card-subtitle>
          </ion-card-header>
          <ion-card-content>
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
  text-align: center;
}
ion-card {
  width: fit-content;
  max-width: 90%;
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