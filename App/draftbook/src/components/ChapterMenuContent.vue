<script setup lang="ts">

import {add, caretBack, caretForward, chevronExpand, menu, pencil, trash} from "ionicons/icons";
import {
  IonAlert,
  IonButton,
  IonButtons,
  IonContent,
  IonFab, IonFabButton,
  IonHeader,
  IonIcon,
  IonItem,
  IonItemOption, IonItemOptions,
  IonItemSliding, IonLabel,
  IonList, IonMenu,
  IonTitle, IonToolbar, menuController, onIonViewWillEnter
} from "@ionic/vue";
import {addIcons} from "ionicons";
import {useAuth0} from "@auth0/auth0-vue";
import {ref} from "vue";
import {API_URL} from "@/localConfig";

addIcons({add, caretBack, caretForward, chevronExpand, pencil, trash});

const {chapters, workId} =
    defineProps(['chapters', 'workId']);
const emit = defineEmits(['doToast', 'selectChapter', 'reloadChapters', 'toggleMenu']);

let isRenameAlertOpen:any = ref(<boolean>false)
let renameAlert = ref();
let chapterSlidingRef:any = ref([]);

//auth
const {getAccessTokenSilently} = useAuth0();

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
    isRenameAlertOpen.value = false;
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

/**
 * Send a request to the server to create the chapter.
 * @param title The title of the chapter to create.
 * @param chapterNumber The position number of the chapter to create.
 */
async function createChapter(title:string, chapterNumber:string) {
  console.log(workId);
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
    emit("doToast", "Failed to create chapter.");
    return;
  }
  emit("doToast", "Chapter created successfully.")
  emit("reloadChapters");
}

/**
 * Rename the chapter in the alert.
 * @param title The new chapter title.
 */
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
    emit("doToast", "Failed to rename chapter.");
    return;
  }
  emit("doToast", "Chapter renamed successfully.")
  emit("reloadChapters");
}

/**
 * Open the alert menu for renaming a chapter.
 * @param id The id of the chapter to rename.
 */
async function openRenameChapterMenu(id:any) {
  console.log(id);
  isRenameAlertOpen.value = true;
  let alertElem = renameAlert.value.$el;
  alertElem.setAttribute("data-chapterid", id);
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
    emit("doToast", "Failed to delete chapter.");
    return;
  }
  emit("doToast", "Chapter deleted successfully.")
  emit("reloadChapters");
}

</script>

<template>
  <ion-header>
    <ion-toolbar>
      <ion-title>Chapters</ion-title>
      <ion-buttons slot="end">
        <ion-button fill="clear" @click="emit('toggleMenu')">
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
      <ion-item-sliding v-for="(chapter, index) in chapters"
                        :key="chapter['number']"
                        :ref="element => chapterSlidingRef[index] = element"
                        @click="emit('selectChapter', chapter['id'])">
        <ion-item button :detail="false">
          <ion-label>{{ chapter['title'] }}</ion-label>
          <ion-button slot="end" shape="round" fill="clear" @click.stop="toggleChapterOptionMenu(index)">
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
          <ion-item-option color="tertiary" @click.stop="openRenameChapterMenu(chapter['id'])">
            <ion-icon slot="icon-only" :icon="pencil"></ion-icon>
          </ion-item-option>
          <ion-item-option color="danger" @click.stop="deleteChapter(chapter['id'])">
            <ion-icon slot="icon-only" :icon="trash"></ion-icon>
          </ion-item-option>
        </ion-item-options>
      </ion-item-sliding>
    </ion-list>
  </ion-content>
</template>

<style scoped>

</style>