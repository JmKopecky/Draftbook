<script setup lang="ts">

import {IonIcon, IonPage, onIonViewWillEnter, toastController, menuController, IonSplitPane, IonButtons, IonButton, IonMenu, IonContent, IonToolbar, IonHeader, IonTitle, IonMenuToggle} from "@ionic/vue";
import {useRoute} from "vue-router";
import {ref, useTemplateRef, watch} from "vue";
import {useAuth0} from "@auth0/auth0-vue";
import {API_URL} from "@/localConfig";
import router from "@/router";
import {menu, apps, chevronBack, chevronForward} from "ionicons/icons";
import {addIcons} from "ionicons";
addIcons({menu, apps, chevronBack, chevronForward});

const route = useRoute()
let workId:string = getIdFromParams(route.params.id)
let work:any = ref({title: 'Loading...'});
let chapters:any = ref([]);
let splitPaneBreakpoint = "(min-width: 992px)";
let splitPaneVisible:boolean = window.matchMedia(splitPaneBreakpoint).matches;

//auth
const {getAccessTokenSilently} = useAuth0();

watch(
    () => route.params.id,
    (newId, oldId) => {
      //when the user id changes, reload everything we need to.
      reloadWork(getIdFromParams(newId));
      reloadChapters();
    }
)

onIonViewWillEnter(() => {
  reloadWork(workId)
  reloadChapters();
});

/**
 * A util function to parse the id params into one id.
 * @param id The id parameters from the url.
 */
function getIdFromParams(id:string | string[]) {
  return Array.isArray(id) ? id[0] : id;
}

/**
 * Reloads the stored work for the current page.
 * @param newWorkId The id of the work to load.
 */
async function reloadWork(newWorkId:string) {
  workId = newWorkId;
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/works/get", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({work_id: workId})
  });
  if (!response.ok) {
    presentToast("Failed to retrieve work");
    router.push('/dashboard');
    return;
  }
  work.value = await response.json();
  console.log(work);
}

/**
 * Reloads the chapter identifier list based on current work.
 */
async function reloadChapters() {
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/chapters/list/identifiers", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({work_id: workId})
  });
  if (!response.ok) {
    presentToast("Failed to retrieve chapters.");
    return;
  }
  chapters.value = await response.json();
  console.log(chapters);
}

/**
 * Toggles a navigation menu.
 * @param menuId The menu to toggle.
 */
async function toggleMenu(menuId:string) {
  if (splitPaneVisible) {
    if (await menuController.isEnabled(menuId)) {
      await menuController.enable(false, menuId)
    } else {
      await menuController.enable(true, menuId)
    }
  } else {
    if (await menuController.isOpen(menuId)) {
      await menuController.close(menuId);
    } else {
      await menuController.open(menuId);
    }
  }
}

/**
 * Present a toast notification to the user
 * @param message The message to display to the user.
 */
const presentToast = async (message:string) => {
  const toast = await toastController.create({
    message: message,
    duration: 4000,
    position: 'bottom',
  });

  await toast.present();
};

async function updateOnSplitPaneChange(event:any) {
  splitPaneVisible = event['detail']['visible'];
  //enable any menus that might have been disabled.
  let menus = await menuController.getMenus();
  for (const menu of menus) {
    let menuId = menu.getAttribute("menu-id");
    await menuController.enable(true, menuId);
  }
}

</script>

<!--Needs to have chapter navigation in the toolbar on the left.
Note panel access in the toolbar on the right.-->

<template>
  <ion-page>
    <ion-split-pane :when="splitPaneBreakpoint" content-id="main" @ionSplitPaneVisible="updateOnSplitPaneChange">

      <ion-menu menu-id="chapter-menu" content-id="main" type="overlay">
        <ion-header>
          <ion-toolbar>
            <ion-title>Chapters</ion-title>
            <ion-buttons slot="end">
              <ion-button fill="clear" @click="toggleMenu('chapter-menu')">
                <ion-icon name="chevron-back" size="large"></ion-icon>
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding"> Menu Content is 350px wide and has a blue dashed border </ion-content>
      </ion-menu>

      <ion-menu menu-id="notes-menu" content-id="main" side="end" type="overlay">
        <ion-header>
          <ion-toolbar>
            <ion-title>Notes</ion-title>
            <ion-buttons slot="start">
              <ion-button fill="clear" @click="toggleMenu('notes-menu')">
                <ion-icon name="chevron-forward" size="large"></ion-icon>
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding">

        </ion-content>
      </ion-menu>

      <ion-page id="main">
        <ion-header>
          <ion-toolbar>
            <ion-title>{{work['title']}}</ion-title>
            <ion-buttons slot="start">
              <ion-button fill="clear" @click="toggleMenu('chapter-menu')">
                <ion-icon name="menu" size="large"></ion-icon>
              </ion-button>
            </ion-buttons>
            <ion-buttons slot="end">
              <ion-button fill="clear" @click="toggleMenu('notes-menu')">
                <ion-icon name="apps" size="large"></ion-icon>
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>

        <ion-content>
          <p>Content!</p>
        </ion-content>
      </ion-page>
    </ion-split-pane>
  </ion-page>
</template>

<style scoped>

</style>