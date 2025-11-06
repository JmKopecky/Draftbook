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
    IonAccordionGroup
} from "@ionic/vue";
import {add, settings} from "ionicons/icons";
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
    text: 'Cancel',
    role: 'cancel'
  },
  {
    text: 'Delete',
    role: 'confirm',
    handler: (alert:any) => {
      let categoryId = manageCategoryAlert.value.$el.getAttribute(("data-categoryid"));
      deleteCategory(categoryId);
      isManageCategoryAlertOpen.value = false;
    },
  },
  {
    text: 'Rename',
    role: 'confirm',
    handler: (alert:any) => {
      let name = alert.categoryName
      let categoryId = manageCategoryAlert.value.$el.getAttribute(("data-categoryid"));
      renameCategory(categoryId, name);
      isManageCategoryAlertOpen.value = false;
    },
  }
];
const manageCategoryInputs = [
  {
    name: 'categoryName',
    placeholder: 'Name',
    attributes: {
      maxlength: 256
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
  console.log("todo implement: " + name + " | " + id);
}

/**
 * Deletes a category.
 * @param id The id of the category to delete.
 */
async function deleteCategory(id:string) {
  console.log("todo implement: " + id);
}

/**
 * Open the alert menu for managing a category.
 * @param id The id of the category to manage.
 */
async function openManageChapterMenu(id:any) {
  isManageCategoryAlertOpen.value = true;
  let alertElem = manageCategoryAlert.value.$el;
  alertElem.setAttribute("data-categoryid", id);
}

/**
 * Creates a new note.
 * @param id The id of the category this note should be for.
 */
async function createNote(id:string) {
  console.log("todo implement: " + id);
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
    ></ion-alert>

    <ion-accordion-group :multiple="true">
      <ion-accordion v-for="(noteCategory, index) in notes" :key="index">
        <ion-item slot="header">
          <ion-label>{{noteCategory['name']}}</ion-label>
          <ion-button slot="end" fill="clear" id="manage-category-button"
                      @click.stop="openManageChapterMenu(noteCategory['id'])">
            <ion-icon :icon="settings" size="large"></ion-icon>
          </ion-button>
        </ion-item>
        <ion-list slot="content">
          <ion-item v-for="(noteIdentifier, index) in noteCategory['noteIdentifiers']">
            <ion-label>{{noteIdentifier['descriptor']}}</ion-label>
          </ion-item>
        </ion-list>
      </ion-accordion>
    </ion-accordion-group>

  </ion-content>
</template>

<style scoped>

</style>