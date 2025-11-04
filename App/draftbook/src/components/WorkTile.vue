<script setup lang="ts">
import {IonIcon, IonCard, IonCardHeader, IonCardTitle, IonCardContent, IonButton} from "@ionic/vue";
import {pencil, trash} from "ionicons/icons";
import {addIcons} from "ionicons";
import router from "@/router";
import {useAuth0} from "@auth0/auth0-vue";
import {API_URL} from "@/localConfig";

addIcons({pencil, trash});

//auth
const {getAccessTokenSilently} = useAuth0();

//emits
const emit = defineEmits(['doToast', 'refresh', 'manageWork']);

//props
const props = defineProps(['work']);
const chapters = props.work["chapterCount"];

/**
 * Redirect to the page for the specific work.
 */
function accessWork() {
  const id = props.work["id"];
  router.push('/work/' + id);
}

/**
 * Delete the work specified
 */
async function deleteWork() {
  //perform a fetch request to delete the work.
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/works/delete", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({
      work_id: props.work['id'],
    })
  });

  if (!response.ok) {
    emit('doToast', "Failed to delete work.");
  } else {
    emit('doToast', "Deleted work successfully.");
    emit('refresh');
  }
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
    <ion-button fill="clear" @click="deleteWork">
      <ion-icon name="trash"></ion-icon>
    </ion-button>
  </ion-card>
</template>

<style scoped>

</style>