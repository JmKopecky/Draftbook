<script setup lang="ts">
import {IonButton, IonIcon, IonSpinner} from "@ionic/vue";
import {QuillEditor} from '@vueup/vue-quill'
import '@/theme/customQuillSnow.css'
import {addIcons} from "ionicons";
import {checkmarkCircle, expand, save} from "ionicons/icons";
import {ref, watchEffect} from "vue";
import {useAuth0} from "@auth0/auth0-vue";
import {API_URL} from "@/localConfig";
import {makePatches, stringifyPatches} from "@sanity/diff-match-patch";

addIcons({save, expand});

const secondsBetweenAutosave = 3;

let quillOptions = {
  placeholder: "Your ideas begin here..."
}
let quillEditor = ref();
let saveTimer = ref(0);
let isAutosaving = false;

//auth
const {getAccessTokenSilently} = useAuth0();

//props
const {chapterId, workId} = defineProps(['chapterId', 'workId']);
let oldContent:string;
watchEffect(() => {
  refreshContent();
})
const emit = defineEmits(['doToast', 'toggleFocus']);
defineExpose({saveContent});

/**
 * Refresh our content from the server's version of the chapter.
 */
async function refreshContent() {
  if (chapterId === -1) {
    return;
  }
  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/chapters/get/content", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({
      work_id: workId,
      chapter_id: chapterId
    })
  });
  if (!response.ok) {
    emit("doToast", "Failed to retrieve chapter content.");
    return;
  }
  let resultingContent = await response.text();
  quillEditor.value.setHTML(resultingContent);
  oldContent = resultingContent;
}

/**
 * Save the content to the chapter.
 */
async function saveContent() {
  if (chapterId == -1) return;

  let content = quillEditor.value.getHTML();

  let patches = stringifyPatches(makePatches(oldContent, content));

  const accessToken = await getAccessTokenSilently();
  const response = await fetch(API_URL + "/chapters/save/patch", {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken
    },
    body: JSON.stringify({
      work_id: workId,
      chapter_id: chapterId,
      patches: patches
    })
  });
  if (!response.ok) {
    emit("doToast", "Failed to save chapter content.");
  } else {
    oldContent = content;
    saveTimer.value = 1000;
    setTimeout(() => {
      saveTimer.value = 0;
    }, saveTimer.value);
  }
}

/**
 * When focus is toggled, save content before swapping focus.
 */
async function toggleFocus() {
  await saveContent();
  emit("toggleFocus", "chapter");
}

/**
 * Prepare an attempt to autosave content later, buffering for multiple quick attempts.
 */
function tryAutosave() {
  if (!isAutosaving) {
    isAutosaving = true;
    setTimeout(() => {
      saveContent();
      isAutosaving = false;
    }, secondsBetweenAutosave * 1000);
  }
}

</script>

<template>
  <QuillEditor theme="snow" toolbar="#toolbar" :options="quillOptions"
               ref="quillEditor" @update:content="tryAutosave">
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
            <button class="ql-blockquote"></button>
            <select class="ql-header"></select>
            <select class="ql-align"></select>
            <select class="ql-color"></select>
          </span>
          <span class="ql-formats">
            <button class="ql-list" value="ordered"></button>
            <button class="ql-list" value="bullet"></button>
            <button class="ql-indent" value="-1"></button>
            <button class="ql-indent" value="+1"></button>
          </span>
          <span class="ql-formats">
            <button class="ql-link"></button>
            <button class="ql-image"></button>
            <button class="ql-video"></button>
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
            <ion-icon v-if="saveTimer == 0" slot="icon-only" :icon="save"></ion-icon>
            <ion-icon v-if="saveTimer != 0" slot="icon-only" :icon="checkmarkCircle"></ion-icon>
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