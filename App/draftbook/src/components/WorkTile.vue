<script setup lang="ts">
import {IonButton, IonCard, IonCardContent, IonCardHeader, IonCardTitle, IonIcon, IonInput} from "@ionic/vue";
import {checkmarkCircle, close, pencil, trash} from "ionicons/icons";
import {addIcons} from "ionicons";
import router from "@/router";
import {useAuth0} from "@auth0/auth0-vue";
import {API_URL} from "@/localConfig";
import {ref} from "vue";

addIcons({pencil, trash});

//auth
const {getAccessTokenSilently} = useAuth0();

//emits
const emit = defineEmits(['doToast', 'refresh', 'manageWork']);

//props
const props = defineProps(['work']);
const chapters = props.work["chapterCount"];

let pendingDeletion = ref(<boolean>false);
let deletionCheckInput = ref();

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
  }
  emit('refresh');
}

/**
 * Convert this tile to pending deletion mode, asking the user to confirm.
 */
async function toggleDeletion() {
  pendingDeletion.value = !pendingDeletion.value;
}

async function tryDeletion() {
  let code = deletionCheckInput.value.$el.value;
  let title = props.work['title'];
  if (code === title) {
    await deleteWork();
  } else {
    emit('doToast', "Incorrect title - deletion failed.");
  }
  toggleDeletion();
}

</script>

<template>
  <ion-card v-if="!pendingDeletion">
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
    <ion-button fill="clear" @click="toggleDeletion">
      <ion-icon name="trash"></ion-icon>
    </ion-button>
  </ion-card>
  <ion-card v-if="pendingDeletion">
    <ion-card-header>
      <ion-card-title>{{props.work['title']}}</ion-card-title>
    </ion-card-header>
    <ion-card-content>
      Are you sure you want to delete this work?

      <ion-input label="Confirm deletion: "
                 placeholder="Type the title of this work to delete."
                 :ref="element => deletionCheckInput = element"></ion-input>
    </ion-card-content>

    <ion-button @click="tryDeletion">
      <ion-icon :icon="checkmarkCircle"></ion-icon>
    </ion-button>
    <ion-button @click="toggleDeletion">
      <ion-icon :icon="close"></ion-icon>
    </ion-button>
  </ion-card>
</template>

<style scoped>

</style>