<script setup lang="ts">

import {
  IonAlert,
  IonButton,
  IonButtons,
  IonContent,
  IonFab,
  IonFabButton,
  IonHeader,
  IonIcon,
  IonItem,
  IonItemOption,
  IonItemOptions,
  IonItemSliding,
  IonLabel,
  IonList,
  IonMenu,
  IonPage,
  IonSplitPane,
  IonTitle,
  IonToolbar,
  menuController,
  onIonViewWillEnter
} from "@ionic/vue";
import {useRoute} from "vue-router";
import {ref, watch} from "vue";
import {useAuth0} from "@auth0/auth0-vue";
import {API_URL} from "@/localConfig";
import router from "@/router";
import {
  add,
  apps,
  caretBack,
  caretForward,
  chevronBack,
  chevronExpand,
  chevronForward,
  menu,
  open,
  pencil,
  trash
} from "ionicons/icons";
import {addIcons} from "ionicons";
import {presentToast} from "@/UtilFunctions";
import ChapterContentEditor from "@/components/ChapterContentEditor.vue";
import ChapterMenuContent from "@/components/ChapterMenuContent.vue";
import NotesMenuContent from "@/components/NotesMenuContent.vue";

addIcons({menu, apps, chevronBack, chevronForward, trash, open, caretBack, chevronExpand, pencil, add, caretForward});

const route = useRoute()
let workId:string = getIdFromParams(route.params.id)
let work:any = ref({title: 'Loading...'});
let chapters:any = ref([]);
let notes:any = ref([]);
let splitPaneBreakpoint = "(min-width: 992px)";
let splitPaneVisible:boolean = window.matchMedia(splitPaneBreakpoint).matches;
let currentChapter = ref(-1);
const chapterMenuId = "chapter-menu";
const noteMenuId = "notes-menu";

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
        reloadNotes();
      }
    }
)

onIonViewWillEnter(() => {
  reloadWork(workId)
  reloadChapters();
  reloadNotes();
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
}

/**
 * Reloads our list of existing note categories
 */
async function reloadNotes() {
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/notes/structure", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({work_id: workId})
  });
  if (!response.ok) {
    presentToast("Failed to retrieve notes.");
    return;
  }
  notes.value = await response.json();
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
 * Marks the current chapter when selected.
 * @param id The chapter to select.
 */
async function selectChapter(id:any) {
  currentChapter.value = id;
  if (!splitPaneVisible) {
    toggleMenu(chapterMenuId);
  }
}

</script>

<template>
  <ion-page>
    <ion-split-pane :when="splitPaneBreakpoint" content-id="main" @ionSplitPaneVisible="updateOnSplitPaneChange">

      <ion-menu menu-id="chapter-menu" content-id="main" type="overlay">
        <ChapterMenuContent :chapters="chapters" :work-id="workId"
                            @do-toast="presentToast"
                            @select-chapter="selectChapter"
                            @reload-chapters="reloadChapters"
                            @reload-notes="reloadNotes"
                            @toggle-menu="toggleMenu(chapterMenuId)">
        </ChapterMenuContent>
      </ion-menu>

      <ion-menu menu-id="notes-menu" content-id="main" side="end" type="overlay">
        <NotesMenuContent :notes="notes" :work-id="workId"
                          @toggle-menu="toggleMenu(noteMenuId)"
                          @do-toast="presentToast"
                          @reload-notes="reloadNotes"
        ></NotesMenuContent>
      </ion-menu>

      <ion-page id="main">
        <ion-header>
          <ion-toolbar>
            <ion-title>{{work['title']}}</ion-title>
            <ion-buttons slot="start">
              <ion-button fill="clear" @click="toggleMenu(chapterMenuId)">
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
          <ChapterContentEditor :chapter-id="currentChapter" :work-id="workId" @do-toast="presentToast">

          </ChapterContentEditor>
        </ion-content>
      </ion-page>
    </ion-split-pane>
  </ion-page>
</template>

<style scoped>

</style>