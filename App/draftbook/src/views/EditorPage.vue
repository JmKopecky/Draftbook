<script setup lang="ts">

import {IonButton, IonButtons, IonContent, IonHeader, IonIcon, IonMenu, IonPage, IonSplitPane, IonTitle, IonToolbar, menuController, onIonViewWillEnter, toastController, IonItem, IonLabel, IonItemOption, IonItemSliding, IonList, IonItemOptions, IonAlert, IonFabButton, IonFab} from "@ionic/vue";
import {useRoute} from "vue-router";
import {ref, useTemplateRef, watch} from "vue";
import {useAuth0} from "@auth0/auth0-vue";
import {API_URL} from "@/localConfig";
import router from "@/router";
import {
  apps,
  chevronBack,
  chevronForward,
  menu,
  trash,
  open,
  caretBack,
  chevronExpand,
  pencil,
  add,
  caretForward
} from "ionicons/icons";
import {addIcons} from "ionicons";
import {presentToast} from "@/UtilFunctions";

addIcons({menu, apps, chevronBack, chevronForward, trash, open, caretBack, chevronExpand, pencil, add, caretForward});

const route = useRoute()
let workId:string = getIdFromParams(route.params.id)
let work:any = ref({title: 'Loading...'});
let chapters:any = ref([]);
let splitPaneBreakpoint = "(min-width: 992px)";
let splitPaneVisible:boolean = window.matchMedia(splitPaneBreakpoint).matches;
let chapterSlidingRef:any = ref([]);
let isRenameAlertOpen:any = ref(<boolean>false)
let renameAlert = ref();

//alert buttons and inputs
const finalizeChapterButtons = [{
  text: 'Create',
  role: 'confirm',
  handler: (alert:any) => {
    let title = alert.chapterTitle;
    let chapterNumber = alert.chapterNumber;
    createChapter(title, chapterNumber);
  },
}];
const createChapterInputs = [
  {
    name: 'chapterTitle',
    placeholder: 'Title',
    attributes: {
      maxlength: 256
    },
  },
  {
    name: 'chapterNumber',
    placeholder: 'Chapter Number',
    attributes: {
      maxlength: 4,
      type: "number",
      min: 1,
      max: chapters.length + 1
    },
  }
  ];
const renameChapterButtons = [{
  text: 'Rename',
  role: 'confirm',
  handler: (alert:any) => {
    let title = alert.chapterTitle;
    renameChapter(title);
  },
}];
const renameChapterInputs = [
  {
    name: 'chapterTitle',
    placeholder: 'New title',
    attributes: {
      maxlength: 256
    },
  }
];

//auth
const {getAccessTokenSilently} = useAuth0();

//loading handlers
watch(
    () => route.params.id,
    (newId, oldId) => {
      if (newId !== null && newId !== undefined) {
        //when the user id changes, reload everything we need to.
        reloadWork(getIdFromParams(newId));
        reloadChapters();
      }
    }
)

onIonViewWillEnter(() => {
  reloadWork(workId)
  reloadChapters();
});

//the actual methods

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
 * Update our splitPaneVisible variable and enable all menus on split pane change.
 * @param event The event associated with the split pane change.
 */
async function updateOnSplitPaneChange(event:any) {
  splitPaneVisible = event['detail']['visible'];
  //enable any menus that might have been disabled.
  let menus = await menuController.getMenus();
  for (const menu of menus) {
    let menuId = menu.getAttribute("menu-id");
    await menuController.enable(true, menuId);
  }
}

/**
 * Send a request to the server to create the chapter.
 * @param title The title of the chapter to create.
 * @param chapterNumber The position number of the chapter to create.
 */
async function createChapter(title:string, chapterNumber:string) {
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/chapters/create", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({
      chapter_name: title,
      chapter_number: chapterNumber,
      work_id: workId
    })
  });
  if (!response.ok) {
    presentToast("Failed to create chapter.");
    return;
  }
  presentToast("Chapter created successfully.")
  await reloadChapters();
}

/**
 * Tells the server to delete the target chapter.
 * @param chapterId The chapter to delete.
 */
async function deleteChapter(chapterId:string) {
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/chapters/delete", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({
      chapter_id: chapterId,
      work_id: workId
    })
  });
  if (!response.ok) {
    presentToast("Failed to delete chapter.");
    return;
  }
  presentToast("Chapter deleted successfully.")
  await reloadChapters();
}

/**
 * Toggle the chapter option menu.
 * @param index The index of the menu to toggle.
 */
async function toggleChapterOptionMenu(index:any) {
  let slider = chapterSlidingRef.value[index].$el;
  let openDistance = await slider.getSlidingRatio();
  if (openDistance != 0) {
    slider.close();
  } else {
    slider.open(undefined);
  }
}

/**
 * Open the alert menu for renaming a chapter.
 * @param id The id of the chapter to rename.
 */
async function openRenameChapterMenu(id:any) {
  isRenameAlertOpen.value = true;
  let alertElem = renameAlert.value.$el;
  alertElem.setAttribute("data-chapterid", id);
}

async function renameChapter(title:string) {
  let chapterId = renameAlert.value.$el.getAttribute(("data-chapterid"));
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/chapters/rename", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({
      chapter_id: chapterId,
      work_id: workId,
      chapter_title: title
    })
  });
  if (!response.ok) {
    presentToast("Failed to rename chapter.");
    return;
  }
  presentToast("Chapter renamed successfully.")
  await reloadChapters();
}

</script>

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
        <ion-content class="ion-padding">

          <ion-fab slot="fixed" vertical="bottom" horizontal="end">
            <ion-fab-button id="create-chapter-button">
              <ion-icon :icon="add"></ion-icon>
            </ion-fab-button>
          </ion-fab>

          <ion-alert
              trigger="create-chapter-button"
              header="Create New Chapter"
              :buttons="finalizeChapterButtons"
              :inputs="createChapterInputs"
          ></ion-alert>

          <ion-alert
              ref="renameAlert"
              :isOpen="isRenameAlertOpen"
              header="Rename Chapter"
              :buttons="renameChapterButtons"
              :inputs="renameChapterInputs"
          ></ion-alert>

          <ion-list>
            <ion-item-sliding v-for="(chapter, index) in chapters" :key="chapter['number']" :ref="element => chapterSlidingRef[index] = element">
              <ion-item button :detail="false">
                <ion-label>{{ chapter['title'] }}</ion-label>
                <ion-button slot="end" shape="round" fill="clear" @click="toggleChapterOptionMenu(index)">
                  <ion-icon slot="icon-only" :icon="caretBack" size="large"></ion-icon>
                </ion-button>
              </ion-item>
              <ion-item-options slot="end">
                <ion-item-option color="none">
                  <ion-icon slot="icon-only" :icon="caretForward"></ion-icon>
                </ion-item-option>
                <ion-item-option color="tertiary">
                  <ion-icon slot="icon-only" :icon="chevronExpand"></ion-icon>
                </ion-item-option>
                <ion-item-option color="tertiary" @click="openRenameChapterMenu(chapter['id'])">
                  <ion-icon slot="icon-only" :icon="pencil"></ion-icon>
                </ion-item-option>
                <ion-item-option color="danger" @click="deleteChapter(chapter['id'])">
                  <ion-icon slot="icon-only" :icon="trash"></ion-icon>
                </ion-item-option>
              </ion-item-options>
            </ion-item-sliding>
          </ion-list>
        </ion-content>
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