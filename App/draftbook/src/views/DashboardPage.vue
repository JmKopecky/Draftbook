<script setup lang="ts">
import {IonAlert, IonIcon, IonPage, modalController, onIonViewWillEnter, toastController, IonHeader, IonToolbar, IonTitle, IonButtons, IonFab, IonFabButton, IonContent} from "@ionic/vue";
import {useAuth0} from '@auth0/auth0-vue';
import AccountButton from "@/components/AccountButton.vue";
import {API_URL} from "@/localConfig";
import {computed, ref} from "vue";
import WorkTile from "@/components/WorkTile.vue";
import {addIcons} from 'ionicons';
import {add} from 'ionicons/icons';
import ManageWorkModal from "@/components/ManageWorkModal.vue";
import {presentToast} from "@/UtilFunctions";

addIcons({add});

//alert buttons and inputs
const finalizeWorkButtons = [{
  text: 'Create',
  role: 'confirm',
  handler: (alert:any) => {
    let title = alert.workTitle;
    createWork(title);
  },
}];
const createWorkInputs = [
  {
    name: 'workTitle',
    placeholder: 'Title',
    attributes: {
      maxlength: 256
    },
  }];

//auth
const {getAccessTokenSilently} = useAuth0();

//refs
const works = ref();

/**
 * A computed parameter for determining if any works are shown.
 */
const worksExist = computed(() => {
  if (works.value !== undefined) {
    return works.value.length !== 0;
  }
  return false;
});

/**
 * Retrieves a refreshed list of the works for the current user.
 */
async function reloadWorks() {
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/works/list", {
    headers: {
      Authorization: 'Bearer ' + accessToken
    }
  });
  if (!response.ok) {
    presentToast("Failed to retrieve works.");
    return;
  }
  const data = await response.json();
  if (Array.isArray(data)) {
    if (data.length !== 0) {
      works.value = data;
    } else {
      presentToast("You currently have no works.");
    }
  } else {
    presentToast("Failed to parse retrieved works.");
  }
}

/**
 * Create the requested work
 */
async function createWork(title:string) {
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/works/create", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({work_name: title})
  });
  if (!response.ok) {
    presentToast("Failed to create work.");
    return;
  }
  presentToast("Work created successfully.")
  reloadWorks();
}

/**
 * Open the work manager modal.
 * @param work The work object to be passed to the modal.
 */
async function openWorkManager(work:object) {
  const modal = await modalController.create({
    component: ManageWorkModal,
    componentProps: {
      work: work
    }
  });

  modal.present();

  modal.onWillDismiss().then(() => {
    reloadWorks();
  });
}

onIonViewWillEnter(reloadWorks);

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

      <div id="works-tile-container" v-if="worksExist">
        <WorkTile v-for="work in works" :work="work"
                  @manage-work="openWorkManager"
                  @do-toast="presentToast"
                  @refresh="reloadWorks">
        </WorkTile>
      </div>

      <ion-fab slot="fixed" vertical="bottom" horizontal="end">
        <ion-fab-button id="create-work-button">
          <ion-icon name="add"></ion-icon>
        </ion-fab-button>
      </ion-fab>

      <ion-alert
          trigger="create-work-button"
          header="Create New Work"
          message="Your next big idea begins now."
          :buttons="finalizeWorkButtons"
          :inputs="createWorkInputs"
      ></ion-alert>
    </ion-content>
  </ion-page>
</template>

<style scoped>
</style>