<script setup lang="ts">

import {expand, save} from "ionicons/icons";
import {QuillEditor} from "@vueup/vue-quill";
import {IonButton, IonIcon} from "@ionic/vue";
import {ref, watchEffect} from "vue";
import {useAuth0} from "@auth0/auth0-vue";
import {API_URL} from "@/localConfig";

let quillOptions = {
  placeholder: "Take a note..."
}
let quillEditor = ref();

//auth
const {getAccessTokenSilently} = useAuth0();

const {noteId, workId} = defineProps(['noteId', 'workId']);
watchEffect(() => {
  refreshContent();
})
const emit = defineEmits(['doToast', 'toggleFocus']);

/**
 * Refresh our note content based on the content in the server
 */
async function refreshContent() {
  if (noteId === -1) {
    return;
  }
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/notes/get/content", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({
      work_id: workId,
      note_id: noteId
    })
  });
  if (!response.ok) {
    emit("doToast", "Failed to retrieve note content.");
    return;
  }
  let resultingContent = await response.text();
  quillEditor.value.setHTML(resultingContent);
}

/**
 * Save the content to the chapter.
 */
async function saveContent() {
  if (noteId == -1) return;
  let content = quillEditor.value.getHTML();
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/notes/save/html", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({
      work_id: workId,
      note_id: noteId,
      note_content: content
    })
  });
  if (!response.ok) {
    emit("doToast", "Failed to save note content.");
    return;
  }
}

async function toggleFocus() {
  await saveContent();
  emit("toggleFocus", "note");
}

</script>

<template>
  <QuillEditor theme="snow" toolbar="#toolbar" :options="quillOptions" ref="quillEditor">
    <template #toolbar>
      <div id="toolbar">
        <div id="toolbar-first">
          <span class="ql-formats">
            <button class="ql-bold"></button>
          <button class="ql-italic"></button>
          <button class="ql-underline"></button>
          <button class="ql-strike"></button>
          </span>
          <span class="ql-formats">
            <button class="ql-script" value="sub"></button>
          <button class="ql-script" value="super"></button>
          </span>
          <span class="ql-formats">
            <button class="ql-list" value="ordered"></button>
            <button class="ql-list" value="bullet"></button>
            <button class="ql-indent" value="-1"></button>
            <button class="ql-indent" value="+1"></button>
          </span>
          <span class="ql-formats">
            <button class="ql-clean"></button>
          </span>
        </div>

        <div id="toolbar-second">
          <ion-button id="focus-button" fill="clear" @click="toggleFocus">
            <ion-icon slot="icon-only" :icon="expand"></ion-icon>
          </ion-button>
          <ion-button id="save-button" fill="clear" @click="saveContent">
            <ion-icon slot="icon-only" :icon="save"></ion-icon>
          </ion-button>
        </div>
      </div>
    </template>
  </QuillEditor>
</template>

<style scoped>
#toolbar {
  display: flex;
  flex-flow: row;
  align-items: center;
  justify-content: space-between;
  #toolbar-first {
    flex: 1;
  }
}
</style>