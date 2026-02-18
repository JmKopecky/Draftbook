<script setup lang="ts">
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonInput,
  IonItem,
  IonTitle,
  IonToolbar,
  modalController
} from "@ionic/vue";
import {ref} from "vue";
import {closeCircle} from "ionicons/icons";
import {addIcons} from "ionicons";
import {useAuth0} from "@auth0/auth0-vue";
import {API_URL} from "@/localConfig";

addIcons({closeCircle});

//auth
const {getAccessTokenSilently} = useAuth0();

const props = defineProps(['work']);
const title = ref(props.work['title']);
const subtitle = ref(props.work['subtitle']);

const emit = defineEmits(['doToast']);

const cancel = () => modalController.dismiss(null, 'cancel');

/**
 * On confirming the edits, send the data to the server.
 */
async function confirm() {
  //perform a fetch request to update.
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/works/update", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({
      work_id: props.work['id'],
      work_title: title.value,
      work_subtitle: subtitle.value
    })
  });

  if (!response.ok) {
    emit('doToast', "Failed to update work.");
  } else {
    emit('doToast', "Updated work successfully.");
  }

  modalController.dismiss(null, 'confirm');
}



</script>

<template>
  <ion-header>
    <ion-toolbar>
      <ion-title>{{props.work['title']}}</ion-title>
      <ion-buttons slot="end">
        <ion-button color="medium" @click="cancel">
          <ion-icon name="close-circle" color="danger" size="large"></ion-icon>
        </ion-button>
        <ion-button @click="confirm">Confirm</ion-button>
      </ion-buttons>
    </ion-toolbar>
  </ion-header>
  <ion-content class="ion-padding">
    <ion-item>
      <ion-input label-placement="stacked"
                 label="Work title"
                 placeholder="Lorem ipsum dolor sit amet..."
                 :value="title"
                 @input="(event:Event) => {title = (<HTMLInputElement>event.target).value}">
      </ion-input>
    </ion-item>
    <ion-item>
      <ion-input label-placement="stacked"
                 label="Subtitle"
                 placeholder="Lorem ipsum dolor sit amet..."
                 :value="subtitle"
                 @input="(event:Event) => {subtitle = (<HTMLInputElement>event.target).value}">
      </ion-input>
  </ion-item>
  </ion-content>
</template>

<style scoped>

</style>