<template>
  <div>
    <Breadcrumbs :path-sections="pwd"/>
    <Folder
      :key="getKeyFromName(folder.name)"
      v-for="folder in folders"
      :folder="folder"
      @change="folderChange"
    ></Folder>
    <File :key="getKeyFromName(file.name)" v-for="file in files" :file="file"></File>
  </div>
</template>

<script>
import Api from '../../service/Api.js';
import Breadcrumbs from './Breadcrumbs.vue';
import Folder from './Folder.vue';
import File from './File.vue';
import md5 from 'md5';

const api = new Api();

export default {
  name: 'items',
  created() {
    this.refreshFiles();
  },
  data() {
    return {
      pwd: [],
      folders: [],
      files: []
    };
  },
  methods: {
    folderChange(args) {
      this.pwd.push(args.name);
      this.folders = [];
      this.files = [];
      this.refreshFiles();
    },
    refreshFiles() {
      const compare = (a, b) => (a > b) ? 1 : (a === b) ? 0 : -1;
      const nameCompare = (a, b) => compare(a.name.toLowerCase(), b.name.toLowerCase());
      api
        .getItems(this.pwd)
        .then(it => {
          this.files = it.files.sort(nameCompare);
          this.folders = it.folders.sort(nameCompare);
        });
    },
    getKeyFromName: md5
  },
  components: {
    Breadcrumbs,
    Folder,
    File
  }
}
</script>
