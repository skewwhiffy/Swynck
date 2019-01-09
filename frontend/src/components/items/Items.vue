<template>
  <div>
    <Folder :key="folder.name" v-for="folder in folders" :folder="folder"></Folder>
    <File :key="file.name" v-for="file in files" :file="file"></File>
  </div>
</template>

<script>
import Api from '../../service/Api.js';
import Folder from './Folder.vue';
import File from './File.vue';

const api = new Api();

export default {
  name: 'items',
  created() {
    this.refreshFiles();
  },
  data() {
    return {
      folders: [],
      files: []
    };
  },
  methods: {
    refreshFiles() {
      const compare = (a, b) => (a > b) ? 1 : (a === b) ? 0 : -1;
      const nameCompare = (a, b) => compare(a.name.toLowerCase(), b.name.toLowerCase());
      api
        .getItems()
        .then(it => {
          this.files = it.files.sort(nameCompare);
          this.folders = it.folders.sort(nameCompare);
        });
    }
  },
  components: {
    Folder,
    File
  }
}
</script>
