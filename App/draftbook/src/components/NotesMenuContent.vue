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
  IonTitle,
  IonToolbar,
  IonLabel,
  IonItem,
  IonList,
  IonAccordion,
  IonAccordionGroup,
  IonInput, IonItemOption, IonItemOptions, IonItemSliding
} from "@ionic/vue";
import {add, addCircle, caretBack, caretForward, chevronExpand, pencil, settings, trash} from "ionicons/icons";
import {API_URL} from "@/localConfig";
import {useAuth0} from "@auth0/auth0-vue";
import {ref} from "vue";

const {notes, workId} =
    defineProps(['notes', 'workId']);
const emit = defineEmits(['toggleMenu', 'doToast', 'reloadNotes']);

//auth
const {getAccessTokenSilently} = useAuth0();

let isManageCategoryAlertOpen:any = ref(<boolean>false)
let manageCategoryAlert = ref();
let focusedCategory = ref("null");
const chapterNotesName = 'Chapter Notes';
let createNoteInputRef:any = ref([]);
let noteSlidingRef = new Array(notes.length);

//alert elements
const finalizeCategoryButton = [{
  text: 'Create',
  role: 'confirm',
  handler: (alert:any) => {
    let name = alert.categoryName
    createCategory(name);
  },
}];
const finalizeCategoryInputs = [
  {
    name: 'categoryName',
    placeholder: 'Name',
    attributes: {
      maxlength: 256
    },
  }
];
const manageCategoryButtons = [
  {
    text: 'Delete',
    role: 'confirm',
    handler: (alert:any) => {
      let categoryId = manageCategoryAlert.value.$el.getAttribute(("data-categoryid"));
      deleteCategory(categoryId);
    },
  },
  {
    text: 'Rename',
    role: 'confirm',
    handler: (alert:any) => {
      let name = alert.categoryName
      let categoryId = manageCategoryAlert.value.$el.getAttribute(("data-categoryid"));
      renameCategory(categoryId, name);
    },
  }
];
const manageCategoryInputs = [
  {
    name: 'categoryName',
    placeholder: 'Name',
    attributes: {
      maxlength: 256,
      value: focusedCategory.value
    },
  }
];

/**
 * Create a new noteCategory for the current work.
 * @param name The name of the category to create.
 */
async function createCategory(name:string) {
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/notes/category/create", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({
      category_name: name,
      work_id: workId
    })
  });
  if (!response.ok) {
    emit("doToast", "Failed to create note category.");
    return;
  }
  emit("doToast", "Note category created successfully.")
  emit("reloadNotes");
}

/**
 * Renames an existing category.
 * @param id The id of the category to rename.
 * @param name The new name of the category.
 */
async function renameCategory(id:string, name:string) {
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/notes/category/rename", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({
      category_name: name,
      category_id: id,
      work_id: workId
    })
  });
  if (!response.ok) {
    //if we had a name conflict with the chapter notes
    if (response.status == 409) {
      emit("doToast", "Failed to rename: conflict with chapter-specific notes.");
    } else {
      emit("doToast", "Failed to rename note category.");
    }
    return;
  }
  emit("doToast", "Note category renamed successfully.")
  emit("reloadNotes");
}

/**
 * Deletes a category.
 * @param id The id of the category to delete.
 */
async function deleteCategory(id:string) {
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/notes/category/delete", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({
      category_id: id,
      work_id: workId
    })
  });
  if (!response.ok) {
    //if we had a name conflict with the chapter notes
    if (response.status == 409) {
      emit("doToast", "The chapter notes category cannot be deleted.");
    } else {
      emit("doToast", "Failed to delete note category.");
    }
    return;
  }
  emit("doToast", "Note category deleted successfully.")
  emit("reloadNotes");
}

/**
 * Open the alert menu for managing a category.
 * @param id The id of the category to manage.
 * @param name The current name of the category to manage.
 */
async function openManageChapterMenu(id:any, name:string) {
  isManageCategoryAlertOpen.value = true;
  let alertElem = manageCategoryAlert.value.$el;
  alertElem.querySelector(".alert-input").value = name;
  alertElem.setAttribute("data-categoryid", id);
}

/**
 * Toggles the note sliding option menu at position index.
 * @param catIndex The index of the category of the note option.
 * @param noteIndex The index of the note in its category.
 */
async function toggleNoteOptionMenu(catIndex:any, noteIndex:any) {
  let slider = noteSlidingRef[catIndex][noteIndex].$el;
  let openDistance = await slider.getSlidingRatio();
  if (openDistance != 0) {
    slider.close();
  } else {
    slider.open(undefined);
  }
}

//todo implement
async function openRenameNoteMenu() {
  console.log("todo implement");
}

/**
 * Deletes a note.
 * @param noteId The id of the note to delete.
 */
async function deleteNote(noteId:any) {
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/notes/delete", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({
      note_id: noteId,
      work_id: workId
    })
  });
  if (!response.ok) {
    emit("doToast", "Failed to delete note.");
    return;
  }
  emit("doToast", "Note deleted successfully.")
  emit("reloadNotes");
}

/**
 * Creates a new note.
 * @param name The name of the note to create.
 * @param catId The id of the category this note should be for.
 */
async function createNote(name:any, catId:any) {
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/notes/create", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({
      category_id: catId,
      work_id: workId,
      note_name: name
    })
  });
  if (!response.ok) {
    emit("doToast", "Failed to create note.");
    return;
  }
  emit("doToast", "Note created successfully.")
  emit("reloadNotes");
}


</script>

<template>
  <ion-header>
    <ion-toolbar>
      <ion-title>Notes</ion-title>
      <ion-buttons slot="start">
        <ion-button fill="clear" @click="emit('toggleMenu')">
          <ion-icon name="chevron-forward" size="large"></ion-icon>
        </ion-button>
      </ion-buttons>
    </ion-toolbar>
  </ion-header>
  <ion-content class="ion-padding">

    <ion-fab slot="fixed" vertical="bottom" horizontal="start">
      <ion-fab-button id="create-category-button">
        <ion-icon :icon="add"></ion-icon>
      </ion-fab-button>
    </ion-fab>

    <ion-alert
        trigger="create-category-button"
        header="Create New Note Category"
        :buttons="finalizeCategoryButton"
        :inputs="finalizeCategoryInputs"
    ></ion-alert>

    <ion-alert
        ref="manageCategoryAlert"
        :isOpen="isManageCategoryAlertOpen"
        header="Manage Category"
        :buttons="manageCategoryButtons"
        :inputs="manageCategoryInputs"
        @didDismiss="() => isManageCategoryAlertOpen = false"
    ></ion-alert>

    <ion-accordion-group :multiple="true">
      <ion-accordion v-for="(noteCategory, catIndex) in notes" :key="catIndex">
        <ion-item slot="header">
          <ion-label>{{noteCategory['name']}}</ion-label>
          <ion-buttons slot="start" v-if="noteCategory['name'] !== chapterNotesName">
            <ion-button fill="clear" id="manage-category-button"
                        @click.stop="openManageChapterMenu(noteCategory['id'], noteCategory['name'])">
              <ion-icon :icon="settings"></ion-icon>
            </ion-button>
          </ion-buttons>
        </ion-item>
        <ion-list slot="content">
          <ion-item-sliding v-for="(noteIdentifier, noteIndex) in noteCategory['noteIdentifiers']"
                            :ref="(el:any) => {
                              if (noteSlidingRef[catIndex] === undefined) {
                                noteSlidingRef[catIndex] = [];
                              }
                              noteSlidingRef[catIndex].splice(noteIndex, 0, el);
                            }">
            <ion-item button :detail="false">
              <ion-label>{{noteIdentifier['descriptor']}}</ion-label>
              <ion-button slot="end" shape="round" fill="clear"
                          @click.stop="toggleNoteOptionMenu(catIndex, noteIndex)"
                          v-if="noteCategory['name'] !== chapterNotesName">
                <ion-icon slot="icon-only" :icon="caretBack" size="large"></ion-icon>
              </ion-button>
            </ion-item>
            <ion-item-options slot="end">
              <ion-item-option color="none" @click.stop="toggleNoteOptionMenu(catIndex, noteIndex)">
                <ion-icon slot="icon-only" :icon="caretForward"></ion-icon>
              </ion-item-option>
              <ion-item-option color="tertiary" @click.stop="openRenameNoteMenu()">
                <ion-icon slot="icon-only" :icon="pencil"></ion-icon>
              </ion-item-option>
              <ion-item-option color="danger" @click.stop="deleteNote(noteIdentifier['id'])">
                <ion-icon slot="icon-only" :icon="trash"></ion-icon>
              </ion-item-option>
            </ion-item-options>
          </ion-item-sliding>
        </ion-list>
        <ion-item slot="content" v-if="noteCategory['name'] !== chapterNotesName">
          <ion-input label="New Note" placeholder="Name..."
                     :ref="(element:any) => createNoteInputRef[catIndex] = element"></ion-input>
          <ion-button slot="end" fill="clear"
                      @click.stop="() => createNote(
                          createNoteInputRef[catIndex].$el.value,
                          noteCategory['id'])">
            <ion-icon :icon="addCircle" size="large" color="light"></ion-icon>
          </ion-button>
        </ion-item>
      </ion-accordion>
    </ion-accordion-group>

  </ion-content>
</template>

<style scoped>

</style>